package com.lidcap.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lidcap.R;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.BookAppointmentInputEntity;
import com.lidcap.model.input.IssuesInputEntity;
import com.lidcap.model.input.LocationUpdateInputEntity;
import com.lidcap.model.input.PendingAppointmentInputEntity;
import com.lidcap.model.input.UserCancelEntity;
import com.lidcap.model.input.UserRatingInputEntity;
import com.lidcap.model.output.AppointmentAcceptResponse;
import com.lidcap.model.output.AppointmentDetailsEntity;
import com.lidcap.model.output.CommonResponse;
import com.lidcap.model.output.PendingDetailsResponse;
import com.lidcap.model.output.ProviderDetailsResponse;
import com.lidcap.model.output.SelectIssuesTypeResponse;
import com.lidcap.model.output.UserCancelResponse;
import com.lidcap.model.output.UserDetailsEntity;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.ui.customer.CustomerHome;
import com.lidcap.utils.AddressUtil;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DateUtil;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.DirectionsJSONParser;
import com.lidcap.utils.InterfaceBtnCallback;
import com.lidcap.utils.InterfaceEdtBtnCallback;
import com.lidcap.utils.InterfaceTwoBtnCallback;
import com.lidcap.utils.NetworkUtil;
import com.lidcap.utils.PreferenceUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomerMapFragment extends BaseFragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final int REQUEST_CHECK_SETTINGS_INT = 300;
    @BindView(R.id.appointment_card_view)
    CardView mAppointmentCardView;
    @BindView(R.id.appointment_accepted_txt)
    TextView mAppointmentAcceptedTxt;
    @BindView(R.id.book_appointment_btn)
    Button mBookAppointmentBtn;

    @BindView(R.id.provider_name_txt)
    TextView mProviderNameTxt;

    @BindView(R.id.provider_rating_txt)
    TextView mProviderRatingTxt;

    @BindView(R.id.arrival_time_txt)
    TextView mArrivalTimeTxt;

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsFirstAPIBool = true, mIsProviderSearchingBool = false, mIsPendingAppointmentBool = false;
    private Timer mProviderListAPICallTimer, mBookAppointmentAPITimer, mCheckPendingAppointmentTimer;
    private Location mCurrentLocation;
    private ArrayList<UserDetailsEntity> mProviderArrList = new ArrayList<>();
    private int mProviderListInt = -1, mOldPEndingStatusInt = -1;
    private Dialog mProviderSearchDialog, mCommentsDialog;
    private String mIssueIdStr = "0";
    private UserDetailsEntity mUserDetailsRes = new UserDetailsEntity();
    private String mUserPhoneNumStr = "";
    private double mProviderLastLat, mProviderLastLng, mCurrentUserLastLat, mCurrentUserLastLng;

    private AppointmentDetailsEntity mAppointmentDetails = new AppointmentDetailsEntity();

    private BitmapDescriptor mCurrentLocMarkerBitmapDescriptor, mVanMarkerBitmapDescriptor, mPointMarkerBitmapDescriptor;

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_cutomer_map, container, false);
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, rootView);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(rootView);

        /*For focus current fragment*/
        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    default:
                        v.performClick();
                }
                return true;
            }
        });


        return rootView;
    }

    /*Fragment manual onResume*/
    @Override
    public void onFragmentResume() {
        /* If the value of visibleInt is zero,  the view will set gone. Or if the value of visibleInt is one,  the view will set visible. Or else, the view will set gone*/
        if (getActivity() != null) {
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && !mIsFirstAPIBool) {
            checkPendingAppointmentAPICall();
            if (!mIsPendingAppointmentBool) {
                if (mIsProviderSearchingBool) {
                    mProviderSearchDialog = DialogManager.getInstance().showSearchPopup(getActivity());
                    bookAppointmentAPICall();
                } else {
                    getProviderListAPICall();
                }
            }

        }
    }

    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();
        mIsFirstAPIBool = true;

        mOldPEndingStatusInt = -1;
        mIsPendingAppointmentBool = false;
        mUserDetailsRes = PreferenceUtil.getUserDetailsRes(getActivity());


        /*Current loc Marker Init*/
        BitmapDrawable currentLocBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.current_loc);
        Bitmap currentLocBitmap = currentLocBitmapDrawable.getBitmap();

        int size30Int = getResources().getDimensionPixelSize(R.dimen.size30);
        int size15Int = getResources().getDimensionPixelSize(R.dimen.size15);
        mCurrentLocMarkerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(currentLocBitmap, size15Int, size30Int, false));


        /*Van Marker Init*/
        BitmapDrawable vanBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.van);
        Bitmap vanBitmap = vanBitmapDrawable.getBitmap();

        mVanMarkerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(vanBitmap, size30Int, size30Int, false));


        /*Direction point Marker Init*/
//        BitmapDrawable pointBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.map_circle_bg);
//        Bitmap pointBitmap = pointBitmapDrawable.getBitmap();

//        Bitmap pointBitmap= ((BitmapDrawable) mPointerImg.getDrawable()).getBitmap();

        Bitmap pointBitmap = drawableToBitmap(getResources().getDrawable(R.drawable.map_circle_bg));

        mPointMarkerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(pointBitmap, size30Int, size30Int, false));

        if (permissionsAccessLocation(true)) {
            initGoogleAPIClient();
            SupportMapFragment fragment = (SupportMapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map);
            /* Map synchronization */
            if (fragment != null)
                fragment.getMapAsync(this);

        }

    }

    /*Init Google API clients*/
    private void initGoogleAPIClient() {

        if (getActivity() != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    /* Location update */
    private void startLocationUpdate() {
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*Ask for permission on customer_location access*/
                permissionsAccessLocation(false);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, AddressUtil.createLocationRequest(), CustomerMapFragment.this);
        }
    }

    /* to stop the customer_location updates */
    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, CustomerMapFragment.this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mCurrentLocation = location;
            if (mGoogleMap != null && !mIsPendingAppointmentBool) {
                mGoogleMap.clear();
                setCurrentLocMarker();
            }
            LocationUpdateInputEntity locationUpdateInputEntity = new LocationUpdateInputEntity();
            locationUpdateInputEntity.setUserId(mUserDetailsRes.getUserId());
            locationUpdateInputEntity.setLatitude(String.valueOf(location.getLatitude()));
            locationUpdateInputEntity.setLongitude(String.valueOf(location.getLongitude()));
            APIRequestHandler.getInstance().latAndLongUpdateAPICall(locationUpdateInputEntity, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (getActivity() != null) {
            LocationManager mLocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (mLocManager != null && mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                    initGoogleAPIClient();
                } else {
                    screenAPICall();
                }
            } else {
                LocationSettingsRequest.Builder locSettingsReqBuilder = new LocationSettingsRequest.Builder().
                        addLocationRequest(AddressUtil.createLocationRequest());
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locSettingsReqBuilder.build());

                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        final Status status = locationSettingsResult.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                                    initGoogleAPIClient();
                                } else {
                                    screenAPICall();
                                }
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                try {
                                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS_INT);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                    Log.e(AppConstants.TAG, e.toString());
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                break;
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (getActivity() != null) {
            mGoogleMap = googleMap;
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsAccessLocation(false);
                return;
            }
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
        }
    }


    /*Click Event*/
    @OnClick({R.id.show_current_location_img, R.id.book_appointment_btn, R.id.cancel_appointment_lay, R.id.call_shoe_repairer_lay})
    public void onClick(View v) {
        if (getActivity() != null) {
            switch (v.getId()) {
                case R.id.show_current_location_img:
                    setCurrentLocation();
                    break;
                case R.id.book_appointment_btn:
                    mOldPEndingStatusInt = -1;
                    issueListAPICall();
                    break;
                case R.id.cancel_appointment_lay:

                    DialogManager.getInstance().showOptionPopup(getActivity(), getString(R.string.cancel_appointment), getString(R.string.yes), getString(R.string.no), new InterfaceTwoBtnCallback() {
                        @Override
                        public void onNegativeClick() {

                        }

                        @Override
                        public void onPositiveClick() {
                            UserCancelEntity userCancelEntity = new UserCancelEntity();
                            userCancelEntity.setAppointmentId(mAppointmentDetails.getAppointmentId());
                            userCancelEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
                            userCancelEntity.setDateTime(DateUtil.getCurrentDate());
                            userCancelEntity.setCancelReason(AppConstants.CUSTOMER_CANCEL_REASON);
                            APIRequestHandler.getInstance().userCancelAppointmentAPICall(userCancelEntity, CustomerMapFragment.this);

                        }
                    });
                    break;
                case R.id.call_shoe_repairer_lay:
                    if (askPermissionsPhone())
                        makePhoneCall();
                    break;
            }
        }
    }


    private void setCurrentLocation() {

        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsAccessLocation(false);
                return;
            }
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
                if (mGoogleMap != null) {

                    FusedLocationProviderClient mLastLocation = LocationServices.getFusedLocationProviderClient(getActivity());

                    mLastLocation.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mCurrentUserLastLat = location.getLatitude();
                                mCurrentUserLastLng = location.getLongitude();
                                mCurrentLocation = location;
                                if (mIsFirstAPIBool) {
                                    mIsFirstAPIBool = false;
                                    setCurrentLocMarker();
                                    getProviderListAPICall();
                                    checkPendingAppointmentAPICall();
                                    startLocationUpdate();
                                }
                                if (ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(getActivity(),
                                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    /*Ask for permission on locatio access
                                     * Set flag for call back to continue this process*/
                                    permissionsAccessLocation(false);
                                }
                                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));

                            }
                        }
                    });


                }
            } else {
                /*Alert message will be appeared if there is no internet connection*/
                DialogManager.getInstance().showNetworkErrorPopup(getActivity(), getString(R.string.no_internet), new InterfaceBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        setCurrentLocation();
                    }
                });
            }
        }
    }

    /*Set current loc custom marker*/
    private void setCurrentLocMarker() {
        if (getActivity() != null && mGoogleMap != null && mCurrentLocation != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MarkerOptions marker = new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).icon(mCurrentLocMarkerBitmapDescriptor);
                    mGoogleMap.addMarker(marker);
                }
            });

        }
    }


    private void issueListAPICall() {
        IssuesInputEntity issuesInputEntity = new IssuesInputEntity();
        issuesInputEntity.setUserId(mUserDetailsRes.getUserId());
        APIRequestHandler.getInstance().selectIssueTypeAPICall(issuesInputEntity, this);
    }


    private void screenAPICall() {
        /*Check for internet connection*/
        if (getActivity() != null) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBookAppointmentBtn.getVisibility() == View.GONE) {
                        mAppointmentCardView.setVisibility(View.GONE);
                        mBookAppointmentBtn.setVisibility(View.VISIBLE);
                    }
                    if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                        initGoogleAPIClient();
                    } else if (NetworkUtil.isNetworkAvailable(getActivity())) {
                        setCurrentLocation();
                    } else {
                        /*Alert message will be appeared if there is no internet connection*/
                        DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.no_internet), new InterfaceBtnCallback() {
                            @Override
                            public void onPositiveClick() {
                                screenAPICall();
                            }
                        });

                    }
                }
            });

        }
    }

    private void bookAppointmentAPICall() {
        /*Check for internet connection*/
        if (getActivity() != null) {
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
                cancelBookAppointmentAPICallTimer();
                mBookAppointmentAPITimer = new Timer();
                mBookAppointmentAPITimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (mIsPendingAppointmentBool) {
                            alertDismiss(mProviderSearchDialog);
                            cancelBookAppointmentAPICallTimer();
                        } else {
                            mProviderListInt += 1;
//                            if (mProviderArrList.size() > mProviderListInt && mProviderListInt < 1) {
                            if (mProviderArrList.size() > mProviderListInt && mProviderListInt < 15) {
                                BookAppointmentInputEntity bookAppointmentInputEntity = new BookAppointmentInputEntity();
                                bookAppointmentInputEntity.setIssueId(mIssueIdStr);
                                bookAppointmentInputEntity.setProviderId(mProviderArrList.get(mProviderListInt).getUserId());
                                bookAppointmentInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
                                bookAppointmentInputEntity.setDateTime(DateUtil.getCurrentDate());
                                APIRequestHandler.getInstance().bookAppointmentAPICall(bookAppointmentInputEntity, CustomerMapFragment.this);
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogManager.getInstance().showToast(getActivity(), getString(R.string.provider_not_found));
                                        cancelProviderSearch();
                                        mIsFirstAPIBool = true;
                                        screenAPICall();
                                    }
                                });
                            }
                        }
                    }
                }, 0, 10000);

            } else {
                DialogManager.getInstance().showNetworkErrorPopup(getActivity(), getString(R.string.no_internet), new InterfaceBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        cancelBookAppointmentAPICallTimer();
                    }
                });
            }
        }
    }


    private void getProviderListAPICall() {
        cancelProviderListAPICallTimer();
        mProviderListAPICallTimer = new Timer();
        mProviderListAPICallTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mIsProviderSearchingBool || mIsPendingAppointmentBool) {
                    cancelProviderListAPICallTimer();
                } else {
                    LocationUpdateInputEntity locationUpdateInputEntity = new LocationUpdateInputEntity();
                    locationUpdateInputEntity.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
                    locationUpdateInputEntity.setLatitude(String.valueOf(mCurrentLocation.getLatitude()));
                    locationUpdateInputEntity.setUserId(mUserDetailsRes.getUserId());
                    APIRequestHandler.getInstance().getProviderLocAPICall(locationUpdateInputEntity, CustomerMapFragment.this);
                }
            }
        }, 0, 80000);
    }

    private void checkPendingAppointmentAPICall() {
        cancelCheckPendingAppointmentAPICallTimer();
        mCheckPendingAppointmentTimer = new Timer();
        final PendingAppointmentInputEntity pendingAppointmentInputEntity = new PendingAppointmentInputEntity();
        pendingAppointmentInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
        pendingAppointmentInputEntity.setDateTime(DateUtil.getCurrentDate());
        mCheckPendingAppointmentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                APIRequestHandler.getInstance().getUserPendingAppointmentAPICall(pendingAppointmentInputEntity, CustomerMapFragment.this);
            }
        }, 0, 5000);
    }


    private void cancelBookAppointmentAPICallTimer() {
        if (mBookAppointmentAPITimer != null) {
            mBookAppointmentAPITimer.cancel();
            mBookAppointmentAPITimer.purge();
        }
    }

    private void cancelProviderSearch() {
        mProviderListInt = -1;
        mIsProviderSearchingBool = false;
        alertDismiss(mProviderSearchDialog);
        cancelBookAppointmentAPICallTimer();
    }

    private void cancelCheckPendingAppointmentAPICallTimer() {
        if (mCheckPendingAppointmentTimer != null) {
            mCheckPendingAppointmentTimer.cancel();
            mCheckPendingAppointmentTimer.purge();
        }
    }

    private void cancelProviderListAPICallTimer() {
        if (mProviderListAPICallTimer != null) {
            mProviderListAPICallTimer.cancel();
            mProviderListAPICallTimer.purge();
        }
    }


    private void makePhoneCall() {
        if (getActivity() != null && !mUserPhoneNumStr.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL);

            intent.setData(Uri.parse("tel:" + mUserPhoneNumStr));
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (!askPermissionsPhone())
                    return;
            }
            startActivity(intent);
        }
    }

    /*To get permission for access image camera and storage*/
    private boolean askPermissionsPhone() {
        boolean addPermission = true;
        if (getActivity() != null) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                int cameraPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);
                if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                addPermission = askAccessPermission(listPermissionsNeeded, 1, new InterfaceTwoBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        makePhoneCall();
                    }

                    public void onNegativeClick() {
                    }
                });
            }
        } else {
            addPermission = false;
        }

        return addPermission;
    }

    /*Ask permission on Location access*/
    private boolean permissionsAccessLocation(final boolean initFlowBool) {
        boolean addPermission = true;
        if (getActivity() != null) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= 23) {
                int permissionLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                int permissionCoarseLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

                if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                addPermission = askAccessPermission(listPermissionsNeeded, 1, new InterfaceTwoBtnCallback() {
                    @Override
                    public void onNegativeClick() {
                        if (initFlowBool) {
                            ActivityCompat.finishAffinity(getActivity());
                        }
                    }

                    @Override
                    public void onPositiveClick() {
                        if (initFlowBool) {
                            initView();
                        } else {
                            setCurrentLocation();
                        }

                    }
                });
            }
        } else {
            addPermission = false;
        }
        return addPermission;
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof ProviderDetailsResponse) {
            ProviderDetailsResponse providerResponse = (ProviderDetailsResponse) resObj;
            if (providerResponse.getResult().size() > 0) {
                providerMarkerDetails(providerResponse.getResult());
            }
        }
        if (resObj instanceof SelectIssuesTypeResponse) {
            SelectIssuesTypeResponse issuesListResponse = (SelectIssuesTypeResponse) resObj;
            if (issuesListResponse.getStatusCode().equals(AppConstants.SUCCESS_CODE)) {

                if (issuesListResponse.getResult().size() > 0) {
                    DialogManager.getInstance().showIssuesListPopup(getActivity(), issuesListResponse.getResult(), new InterfaceEdtBtnCallback() {
                        @Override
                        public void onPositiveClick(String issueIdStr) {
                            if (mGoogleMap != null) {
                                mGoogleMap.clear();
                                setCurrentLocation();
                            }
                            mProviderSearchDialog = DialogManager.getInstance().showSearchPopup(getActivity());
                            mIssueIdStr = issueIdStr;
                            mIsProviderSearchingBool = true;
                            bookAppointmentAPICall();
                        }

                        @Override
                        public void onNegativeClick() {

                        }
                    });
                }
            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), issuesListResponse.getMessage(), this);
            }
        }

        if (resObj instanceof PendingDetailsResponse) {
            PendingDetailsResponse pendingDetailsRes = (PendingDetailsResponse) resObj;
            if (getActivity() != null) {

                sysOut("getAnotheruser---" + pendingDetailsRes.getResult().getAnotheruser().size());
                sysOut("getAppointments---" + pendingDetailsRes.getResult().getAppointments().size());
                sysOut("getProfile---" + pendingDetailsRes.getResult().getProfile().size());
                if (pendingDetailsRes.getResult().getAnotheruser().size() > 0 && pendingDetailsRes.getResult().getAppointments().size() > 0
                        && !pendingDetailsRes.getResult().getAppointments().get(0).getStatus().equalsIgnoreCase("1")) {
                    mIsPendingAppointmentBool = true;
                    cancelProviderListAPICallTimer();
                    cancelBookAppointmentAPICallTimer();
                    alertDismiss(mProviderSearchDialog);
                    final UserDetailsEntity userDetails = pendingDetailsRes.getResult().getAnotheruser().get(0);
                    final AppointmentDetailsEntity appointmentDetails = pendingDetailsRes.getResult().getAppointments().get(0);
                    mAppointmentDetails = appointmentDetails;

                    sysOut("getStatus---" + mAppointmentDetails.getStatus());
                    mUserPhoneNumStr = userDetails.getPhoneNumber();
                    if (mAppointmentDetails.getStatus().equalsIgnoreCase("2")) {

                        mOldPEndingStatusInt = 2;

                        if (mBookAppointmentBtn.getVisibility() == View.VISIBLE) {
                            mBookAppointmentBtn.setVisibility(View.GONE);
                        }

                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mAppointmentCardView.getVisibility() == View.GONE) {
                                        mAppointmentCardView.setVisibility(View.VISIBLE);
                                        mProviderNameTxt.setText(userDetails.getUserName());
                                        mProviderRatingTxt.setText(userDetails.getRating());
                                    }
                                    if (!userDetails.getLatitude().isEmpty() && !userDetails.getLongitude().isEmpty()) {
                                        mapDirection(Double.valueOf(userDetails.getLatitude()), Double.valueOf(userDetails.getLongitude()));
                                    }

                                }
                            });

                    } else if (mOldPEndingStatusInt != 3 && (mAppointmentDetails.getStatus().equalsIgnoreCase("3"))) {

                        sysOut("if---3");
                        mOldPEndingStatusInt = 3;
                        mIsPendingAppointmentBool = true;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mBookAppointmentBtn.getVisibility() == View.VISIBLE) {
                                    mBookAppointmentBtn.setVisibility(View.GONE);
                                }
                                mCommentsDialog = DialogManager.getInstance().showCommentsPopup(getActivity(), new InterfaceEdtBtnCallback() {
                                    @Override
                                    public void onPositiveClick(String editStr) {

                                        PreferenceUtil.storeStringPreferenceValue(getActivity(),AppConstants.PROVIDER_ID,userDetails.getUserId());
                                        UserRatingInputEntity userRatingInputEntity = new UserRatingInputEntity();
                                        userRatingInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
                                        userRatingInputEntity.setAppointmentId(appointmentDetails.getAppointmentId());
                                        userRatingInputEntity.setComments(AppConstants.CUSTOMER_REVIEW);
                                        userRatingInputEntity.setDescription(AppConstants.CUSTOMER_COMMENTS);
                                        userRatingInputEntity.setRate(AppConstants.CUSTOMER_RATING);
                                        userRatingInputEntity.setDateTime(DateUtil.getCurrentDate());
                                        userRatingInputEntity.setProviderId(userDetails.getUserId());
                                        APIRequestHandler.getInstance().userRateAppointmentAPICall(userRatingInputEntity, CustomerMapFragment.this);
                                    }

                                    @Override
                                    public void onNegativeClick() {

                                    }
                                });
                            }
                        });


                    } else if (getActivity() != null && (mAppointmentDetails.getStatus().equalsIgnoreCase("0") || mAppointmentDetails.getStatus().equalsIgnoreCase("4") || (mAppointmentDetails.getStatus().equalsIgnoreCase("5")))) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                sysOut("if---4");
                                mAppointmentCardView.setVisibility(View.GONE);
                                mBookAppointmentBtn.setVisibility(View.VISIBLE);
                                alertDismiss(mCommentsDialog);
                                mIsPendingAppointmentBool = false;
                                mIsFirstAPIBool = true;
                                screenAPICall();
                            }
                        });

                    }
                } else if (pendingDetailsRes.getResult().getAnotheruser().size() == 0 && (
                        mIsPendingAppointmentBool || mAppointmentCardView.getVisibility() == View.VISIBLE ||
                                mBookAppointmentBtn.getVisibility() == View.GONE)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            sysOut("if---5");
                            sysOut("mIsPendingAppointmentBool---" + mIsPendingAppointmentBool);
                            sysOut("mAppointmentCardView---" + (mAppointmentCardView.getVisibility() == View.VISIBLE));
                            sysOut("mBookAppointmentBtn---" + (mBookAppointmentBtn.getVisibility() == View.GONE));
                            mIsPendingAppointmentBool = false;
                            mAppointmentCardView.setVisibility(View.GONE);
                            mBookAppointmentBtn.setVisibility(View.VISIBLE);
                            mIsFirstAPIBool = true;
                            screenAPICall();
                        }
                    });
                }
            }
        }


        if (mIsPendingAppointmentBool && (resObj instanceof UserCancelResponse || resObj instanceof AppointmentAcceptResponse)) {
            mAppointmentCardView.setVisibility(View.GONE);
            mBookAppointmentBtn.setVisibility(View.VISIBLE);
            mIsFirstAPIBool = true;
            alertDismiss(mCommentsDialog);
            screenAPICall();

            sysOut("if---UserCancelResponse");
            mIsPendingAppointmentBool = false;
        }

        if (resObj instanceof CommonResponse) {
            sysOut("if---CommonResponse");
            if (getActivity() != null) {
                ((CustomerHome) getActivity()).addFragment(new UserAdvListFragment());
            }
        }
    }


    private void mapDirection(final double providerLat, final double providerLng) {
        if (getActivity() != null && mGoogleMap != null && (mProviderLastLat != providerLat || mProviderLastLng != providerLng)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGoogleMap.clear();
                    mProviderLastLat = providerLat;
                    mProviderLastLng = providerLng;
                    MarkerOptions marker = new MarkerOptions().position(new LatLng(providerLat, providerLng)).icon(mVanMarkerBitmapDescriptor);
                    mGoogleMap.addMarker(marker);
                    setCurrentLocMarker();
                    directionPoint();
                }
            });
        }
    }


    private void directionPoint() {
        LatLng origin = new LatLng(mProviderLastLat, mProviderLastLng);
        LatLng dest = new LatLng(mCurrentUserLastLat, mCurrentUserLastLng);
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_map_id);

    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(AppConstants.TAG, e.toString());
        } finally {
            if (iStream != null)
                iStream.close();
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        if (resObj instanceof BookAppointmentInputEntity) {
            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogManager.getInstance().showNetworkErrorPopup(getActivity(), getString(R.string.no_internet), CustomerMapFragment.this);
                        cancelProviderSearch();
                    }
                });

            return;
        }
        super.onRequestFailure(resObj, t);
        if (t instanceof IOException) {
            DialogManager.getInstance().showAlertPopup(getActivity(),
                    (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException ? getString(R.string.no_internet) : getString(R.string
                            .connect_time_out)), new InterfaceBtnCallback() {
                        @Override
                        public void onPositiveClick() {
                        }
                    });
        }
    }

    private void providerMarkerDetails(final ArrayList<UserDetailsEntity> providerDetails) {

        if (providerDetails.size() > 0) {
            mProviderArrList = new ArrayList<>();
            mProviderArrList = providerDetails;
            mGoogleMap.clear();
            setCurrentLocMarker();
            for (int detailsPosInt = 0; detailsPosInt < providerDetails.size(); detailsPosInt++) {
                if (mGoogleMap != null) {
                    if (mIsProviderSearchingBool) {
                        mOldPEndingStatusInt = -1;
                        break;
                    } else if (getActivity() != null) {
                        final int posInt = detailsPosInt;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MarkerOptions marker = new MarkerOptions().position(new LatLng(Double.valueOf(providerDetails.get(posInt).getLatitude()), Double.valueOf(providerDetails.get(posInt).getLongitude()))).icon(mVanMarkerBitmapDescriptor);
                                mGoogleMap.addMarker(marker);
                            }
                        });

                    }
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_INT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                            initGoogleAPIClient();
                        } else {
                            setCurrentLocation();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // user does not want to update setting. Handle it in a way that it will to affect your app functionality
                        DialogManager.getInstance().showToast(getActivity(), getString(R.string.user_not_update));
                        break;
                }
                break;

        }

    }

    @Override
    public void onPause() {
        cancelProviderListAPICallTimer();
        cancelBookAppointmentAPICallTimer();
        cancelCheckPendingAppointmentAPICallTimer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        cancelProviderListAPICallTimer();
        cancelBookAppointmentAPICallTimer();
        super.onDestroy();
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                return;
            }
            MarkerOptions startPointMarkerOptions = new MarkerOptions(), endPointMarkerOptions = new MarkerOptions();
            boolean startPointBoolean = false, endPointBoolean = false;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                if (result.size() == 1) {
                    startPointBoolean = true;
                    endPointBoolean = true;
                } else if (i == 0) {
                    startPointBoolean = true;
                } else if (i == result.size() - 1) {
                    startPointBoolean = false;
                    endPointBoolean = true;
                } else {
                    startPointBoolean = false;
                    endPointBoolean = false;
                }

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

//                    if (j == 0) {    // Get distance from the list
//                        distance = (String) point.get("distance");
//                        continue;
//                    } else
                        if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    if (point.get("lat") != null && point.get("lng") != null) {

                        double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                        double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                        LatLng position = new LatLng(lat, lng);

                        if (startPointBoolean) {
                            startPointBoolean=false;
                            startPointMarkerOptions = new MarkerOptions().position(position).icon(mPointMarkerBitmapDescriptor);
                        } else if (j == path.size() - 1 && endPointBoolean) {
                            endPointMarkerOptions = new MarkerOptions().position(position).icon(mPointMarkerBitmapDescriptor);
                        }

                        points.add(position);
                    }
                }

                // Adding all the points in the route to LineOptions
                if (getActivity() != null) {
                    lineOptions.addAll(points);
                    lineOptions.width(6);
                    lineOptions.color(ContextCompat.getColor(getActivity(), R.color.blue));
                }
            }

//            tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);

            if (getActivity() != null) {
                final PolylineOptions finalLineOptions = lineOptions;
                final String finalDuration = duration;
                final MarkerOptions finalStartPointMarkerOptions = startPointMarkerOptions;
                final MarkerOptions finalEndPointMarkerOptions = endPointMarkerOptions;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mArrivalTimeTxt.setText(String.format(getString(R.string.arrival_time), finalDuration));
                        // Drawing polyline in the Google Map for the i-th route
                        if (mGoogleMap != null) {
                            mGoogleMap.addPolyline(finalLineOptions);
                            mGoogleMap.addMarker(finalStartPointMarkerOptions);
                            mGoogleMap.addMarker(finalEndPointMarkerOptions);
                        }
                    }
                });
            }

        }
    }

    @Override
    public void onDestroyView() {
        mOldPEndingStatusInt =-1;
        super.onDestroyView();

    }
}

