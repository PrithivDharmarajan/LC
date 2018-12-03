package com.lipcap.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lipcap.R;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.output.PendingDetailsEntity;
import com.lipcap.model.output.PendingDetailsResponse;
import com.lipcap.model.output.ProviderDetailsEntity;
import com.lipcap.model.output.ProviderDetailsResponse;
import com.lipcap.model.output.SelectIssuesTypeResponse;
import com.lipcap.model.output.UserDetailsEntity;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.utils.AddressUtil;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DateUtil;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.InterfaceEdtBtnCallback;
import com.lipcap.utils.InterfaceTwoBtnCallback;
import com.lipcap.utils.NetworkUtil;
import com.lipcap.utils.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean isDialogShowing = false, mIsFirstAPIBool = true, mIsProviderSearchingBool = false, mIsPendingAppointmentBool = false;
    private Timer mAPICallTimer, mProviderAPITimer, mCheckAppointmentTimer;
    private Location mCurrentLocation;
    private ArrayList<ProviderDetailsEntity> mProviderArrList = new ArrayList<>();
    private int mProviderListInt = -1;
    private Dialog mProviderSearchDialog;
    private String mIssueIdStr = "0";
    private UserDetailsEntity mUserDetailsRes = new UserDetailsEntity();
    private String mUserPhoneNumStr = "";
    private PendingDetailsEntity mPendingUserDetails = new PendingDetailsEntity();

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
        if (getActivity() != null && !mIsFirstAPIBool && mIsPendingAppointmentBool) {
            if (mIsProviderSearchingBool) {
                searchProviderAPICall();
            } else {
                getProviderListAPICall();
            }
        }
    }

    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();
        mIsFirstAPIBool = true;

        mUserDetailsRes = PreferenceUtil.getUserDetailsRes(getActivity());
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
    @OnClick({R.id.show_current_location_img, R.id.book_appointment_btn, R.id.cancel_appointment_lay,R.id.call_shoe_repairer_lay})
    public void onClick(View v) {
        if (getActivity() != null) {
            switch (v.getId()) {
                case R.id.show_current_location_img:
                    setCurrentLocation();
                    break;
                case R.id.book_appointment_btn:
                    issueListAPICall();
                    break;
                case R.id.cancel_appointment_lay:
//                    DialogManager.getInstance().showReasonForCancelPopup(getActivity(), new InterfaceEdtBtnCallback() {
//                        @Override
//                        public void onPositiveClick(String editStr) {
//                            isDialogShowing = false;
//                        }
//
//                        @Override
//                        public void onNegativeClick() {
//                            isDialogShowing = false;
//                            commentDialog();
//                        }
//                    });

                    APIRequestHandler.getInstance().postAppointmentStatusAPICall(String.valueOf(mPendingUserDetails.getId()), String.valueOf(mUserDetailsRes.getId()), String.valueOf(mPendingUserDetails.getIssueId()), DateUtil.getCurrentDate(), "5", String.valueOf(mPendingUserDetails.getAmount()), mPendingUserDetails.getDuration(), CustomerMapFragment.this);

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
                                mCurrentLocation = location;
                                if (mIsFirstAPIBool) {
                                    mIsFirstAPIBool = false;
                                    setCurrentLocMarker();
                                    getProviderListAPICall();
                                    getCheckPendingAppointmentAPICall();
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
        if (mGoogleMap != null && mCurrentLocation != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.curren_loc);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            int heightSizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
            int widthSizeInt = getResources().getDimensionPixelSize(R.dimen.size15);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, widthSizeInt, heightSizeInt, false);
            MarkerOptions marker = new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mGoogleMap.addMarker(marker);

        }
    }


    private void commentDialog() {
        if (!isDialogShowing) {
            DialogManager.getInstance().showCommentsPopup(getActivity(), new InterfaceEdtBtnCallback() {
                @Override
                public void onPositiveClick(String editStr) {
                    mAppointmentCardView.setVisibility(View.GONE);
                    mBookAppointmentBtn.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNegativeClick() {

                    mAppointmentCardView.setVisibility(View.GONE);
                    mBookAppointmentBtn.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void issueListAPICall() {
        APIRequestHandler.getInstance().selectIssueTypeAPICall(this);
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
                    if (NetworkUtil.isNetworkAvailable(getActivity())) {
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

    private void searchProviderAPICall() {
        /*Check for internet connection*/
        if (getActivity() != null) {
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
                cancelProviderAPICallTimer();
                mProviderAPITimer = new Timer();
                mProviderAPITimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mProviderListInt += 1;
                        if (mProviderArrList.size() > mProviderListInt && mIsProviderSearchingBool) {

                            APIRequestHandler.getInstance().bookAppointmentAPICall(String.valueOf(mUserDetailsRes.getId()), String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude()), DateUtil.getCurrentDate(), mIssueIdStr, mProviderArrList.get(mProviderListInt).getDeviceId(), CustomerMapFragment.this);


                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogManager.getInstance().showToast(getActivity(), getString(R.string.provider_not_found));
                                    cancelProviderSearch();
                                }
                            });

                        }

                    }
                }, 0, 10000);


            } else {
                DialogManager.getInstance().showNetworkErrorPopup(getActivity(), getString(R.string.no_internet), new InterfaceBtnCallback() {
                    @Override
                    public void onPositiveClick() {

                        cancelProviderAPICallTimer();
                    }
                });
            }
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
            APIRequestHandler.getInstance().latAndLongUpdateAPICall(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), 1 + "", this);
        }
    }

    private void cancelProviderSearch() {
        mProviderListInt = -1;
        mIsProviderSearchingBool = false;
        mIsFirstAPIBool = true;
        alertDismiss(mProviderSearchDialog);
        cancelProviderAPICallTimer();
        screenAPICall();
    }

    private void getProviderListAPICall() {
        cancelAPICallTimer();
        mAPICallTimer = new Timer();
        mAPICallTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mIsProviderSearchingBool || mIsPendingAppointmentBool) {
                    cancelAPICallTimer();
                } else {
                    APIRequestHandler.getInstance().getProviderLocAPICall(String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude()), CustomerMapFragment.this);
                }
            }
        }, 0, 50000);
    }

    private void getCheckPendingAppointmentAPICall() {
        cancelCheckPendingAppointmentAPICallTimer();
        mCheckAppointmentTimer = new Timer();
        mCheckAppointmentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                APIRequestHandler.getInstance().getUserPendingAppointmentAPICall(String.valueOf(mUserDetailsRes.getId()), "1", CustomerMapFragment.this);
            }
        }, 0, 5000);
    }

    private void cancelCheckPendingAppointmentAPICallTimer() {
        if (mCheckAppointmentTimer != null) {
            mCheckAppointmentTimer.cancel();
            mCheckAppointmentTimer.purge();
        }
    }

    private void cancelAPICallTimer() {
        if (mAPICallTimer != null) {
            mAPICallTimer.cancel();
            mAPICallTimer.purge();
        }
    }

    private void cancelProviderAPICallTimer() {
        if (mProviderAPITimer != null) {
            mProviderAPITimer.cancel();
            mProviderAPITimer.purge();
        }
    }
    private void makePhoneCall() {
        if (getActivity() != null && !mUserPhoneNumStr.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL);

            intent.setData(Uri.parse("tel:" + mUserPhoneNumStr));
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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
        if (resObj instanceof SelectIssuesTypeResponse) {
            SelectIssuesTypeResponse issuesListResponse = (SelectIssuesTypeResponse) resObj;
            if (issuesListResponse.getMsgCode().equals(AppConstants.SUCCESS_CODE)) {

                if (issuesListResponse.getIssueType().size() > 0) {
                    DialogManager.getInstance().showIssuesListPopup(getActivity(), issuesListResponse.getIssueType(), new InterfaceEdtBtnCallback() {
                        @Override
                        public void onPositiveClick(String issueIdStr) {
//                            mAppointmentCardView.setVisibility(View.VISIBLE);
                            mBookAppointmentBtn.setVisibility(View.GONE);
                            if (mGoogleMap != null) {
                                mGoogleMap.clear();
                                setCurrentLocation();
                            }
                            mProviderSearchDialog = DialogManager.getInstance().showSearchPopup(getActivity());
                            mIssueIdStr = issueIdStr;
                            searchProviderAPICall();
                            mIsProviderSearchingBool = true;
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
            if (getActivity() != null && !mIsPendingAppointmentBool) {
                PendingDetailsResponse pendingDetailsRes = (PendingDetailsResponse) resObj;
                ArrayList<PendingDetailsEntity> userDetailArrListRes = pendingDetailsRes.getUserDetail();
                if (userDetailArrListRes.size() > 0) {
                    PendingDetailsEntity userDetail = new PendingDetailsEntity();
                    for (int i = 0; i < userDetailArrListRes.size(); i++) {
                        if (!String.valueOf(mUserDetailsRes.getId()).equalsIgnoreCase(String.valueOf(userDetailArrListRes.get(i).getId()))) {
                            userDetail = userDetailArrListRes.get(i);
                            break;
                        }
                    }

                    mPendingUserDetails = userDetail;
                    mUserPhoneNumStr = userDetail.getMobileNo();
                    if (!mIsPendingAppointmentBool && (
                            userDetailArrListRes.get(0).getStatus() != 4 || userDetailArrListRes.get(0).getStatus() != 5)) {
                        mIsPendingAppointmentBool = true;

                        final PendingDetailsEntity finalUserDetail = userDetail;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAppointmentCardView.setVisibility(View.VISIBLE);
                                mProviderNameTxt.setText(finalUserDetail.getName());
                                mapDirection(finalUserDetail.getLatitude(), finalUserDetail.getLongitude());


                            }
                        });

                    } else if (userDetailArrListRes.get(0).getStatus() == 2) {
                        DialogManager.getInstance().showCommentsPopup(getActivity(), new InterfaceEdtBtnCallback() {
                            @Override
                            public void onPositiveClick(String editStr) {

                                APIRequestHandler.getInstance().postAppointmentStatusAPICall(String.valueOf(mPendingUserDetails.getId()), String.valueOf(mUserDetailsRes.getId()), String.valueOf(mPendingUserDetails.getIssueId()), DateUtil.getCurrentDate(), "3", String.valueOf(mPendingUserDetails.getAmount()), mPendingUserDetails.getDuration(), CustomerMapFragment.this);
                            }

                            @Override
                            public void onNegativeClick() {
                                APIRequestHandler.getInstance().postAppointmentStatusAPICall(String.valueOf(mPendingUserDetails.getId()), String.valueOf(mUserDetailsRes.getId()), String.valueOf(mPendingUserDetails.getIssueId()), DateUtil.getCurrentDate(), "3", String.valueOf(mPendingUserDetails.getAmount()), mPendingUserDetails.getDuration(), CustomerMapFragment.this);

                            }
                        });

                    } else if (userDetailArrListRes.get(0).getStatus() == 4 || userDetailArrListRes.get(0).getStatus() == 5) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAppointmentCardView.setVisibility(View.GONE);
                                mBookAppointmentBtn.setVisibility(View.VISIBLE);
                                mIsFirstAPIBool = true;
                                screenAPICall();
                                mIsPendingAppointmentBool = false;
                            }
                        });

                    }
                } else {
                    mIsPendingAppointmentBool = false;
                    mAppointmentCardView.setVisibility(View.GONE);
                }
            }
        }

        if (resObj instanceof ProviderDetailsResponse) {
            ProviderDetailsResponse providerResponse = (ProviderDetailsResponse) resObj;
            if (providerResponse.getUserDetail().size() > 0) {
                providerMarkerDetails(providerResponse.getUserDetail());
            }
        }


    }


    private void mapDirection(double userLat, double userLong) {
        if (mGoogleMap != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.van);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            int heightSizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
            int widthSizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, widthSizeInt, heightSizeInt, false);
            MarkerOptions marker = new MarkerOptions().position(new LatLng(userLat, userLong)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mGoogleMap.addMarker(marker);
            setCurrentLocMarker();
        }

    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        if (resObj instanceof String) {
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

    private void providerMarkerDetails(ArrayList<ProviderDetailsEntity> providerDetails) {

        if (providerDetails.size() > 0) {
            mProviderArrList = new ArrayList<>();
            mProviderArrList = providerDetails;
            mGoogleMap.clear();
            setCurrentLocMarker();
            for (int i = 0; i < providerDetails.size(); i++) {
                if (mGoogleMap != null) {
                    if (mIsProviderSearchingBool) {
                        break;
                    } else {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.van);
                        Bitmap bitmap = bitmapDrawable.getBitmap();

                        int sizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
                        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, sizeInt, sizeInt, false);
                        MarkerOptions marker = new MarkerOptions().position(new LatLng(providerDetails.get(i).getLatitude(), providerDetails.get(i).getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                        mGoogleMap.addMarker(marker);
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
        cancelAPICallTimer();
        cancelProviderAPICallTimer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        cancelAPICallTimer();
        cancelProviderAPICallTimer();
        super.onDestroy();
    }

}

