package com.lipcap.fragment;

import android.Manifest;
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
import android.os.Handler;
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
import com.lipcap.model.input.AppointmentAcceptEntity;
import com.lipcap.model.input.LocationUpdateInputEntity;
import com.lipcap.model.input.PendingAppointmentInputEntity;
import com.lipcap.model.input.UserCancelEntity;
import com.lipcap.model.output.AppointmentDetailsEntity;
import com.lipcap.model.output.PendingDetailsResponse;
import com.lipcap.model.output.UserCancelResponse;
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

public class ProviderMapFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {


    private final int REQUEST_CHECK_SETTINGS_INT = 300;
    @BindView(R.id.customer_name_txt)
    TextView mCustomerNameTxt;

    @BindView(R.id.appointment_card_view)
    CardView mAppointmentCardView;

    @BindView(R.id.accept_appointment_txt)
    TextView mAcceptAppointmentTxt;

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsFirstAPIBool = true, mIsPendingAppointmentBool = false;
    private Location mCurrentLocation;
    private Timer mCheckAppointmentTimer;
    private String mUserIdStr = "", mUserPhoneNumStr = "";
    private Dialog mNotificationDialog;
    private UserDetailsEntity mPendingUserDetails = new UserDetailsEntity();
    private AppointmentDetailsEntity mAppointmentDetails = new AppointmentDetailsEntity();
    private Bitmap mCurrentLocMarkerBitmap, mVanMarkerBitmap;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_provider_map, container, false);
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
        if (getActivity() != null) {
            initView();
        }
    }


    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();
        mIsFirstAPIBool = true;
        mUserIdStr = PreferenceUtil.getUserId(getActivity());

        /*Current loc Marker Init*/
        BitmapDrawable currentLocBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.current_loc);
        Bitmap currentLocBitmap = currentLocBitmapDrawable.getBitmap();

        int heightSizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
        int widthSizeInt = getResources().getDimensionPixelSize(R.dimen.size15);
        mCurrentLocMarkerBitmap = Bitmap.createScaledBitmap(currentLocBitmap, widthSizeInt, heightSizeInt, false);


        /*Van Marker Init*/
        BitmapDrawable vanBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.van);
        Bitmap vanBitmap = vanBitmapDrawable.getBitmap();

        int sizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
        mVanMarkerBitmap = Bitmap.createScaledBitmap(vanBitmap, sizeInt, sizeInt, false);


        if (permissionsAccessLocation(true)) {
            initGoogleAPIClient();
            SupportMapFragment fragment = (SupportMapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map);
            /* Map synchronization */
            if (fragment != null)
                fragment.getMapAsync(this);

        }

    }


    /*Click Event*/
    @OnClick({R.id.show_current_location_img, R.id.call_customer_lay, R.id.accept_appointment_lay, R.id.cancel_appointment_lay})
    public void onClick(View v) {
        if (getActivity() != null) {
            switch (v.getId()) {
                case R.id.show_current_location_img:
                    setCurrentLocation();
                    break;
                case R.id.call_customer_lay:
                    if (askPermissionsPhone())
                        makePhoneCall();
                    break;
                case R.id.accept_appointment_lay:

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAcceptAppointmentTxt.getText().toString().equalsIgnoreCase(getString(R.string.accept_appointment))) {
                                DialogManager.getInstance().showProviderAmtDialogPopup(getActivity(), mAppointmentDetails.getIssueName(), new InterfaceEdtBtnCallback() {
                                    @Override
                                    public void onPositiveClick(String editStr) {
                                        mAcceptAppointmentTxt.setText(getString(R.string.work_done));

                                        AppointmentAcceptEntity appointmentAcceptEntity = new AppointmentAcceptEntity();
                                        appointmentAcceptEntity.setAppointmentId(mAppointmentDetails.getAppointmentId());
                                        appointmentAcceptEntity.setDateTime(DateUtil.getCurrentDate());
                                        appointmentAcceptEntity.setDuration(editStr);
                                        appointmentAcceptEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
                                        APIRequestHandler.getInstance().acceptAppointmentAPICall(appointmentAcceptEntity, ProviderMapFragment.this);
                                    }

                                    @Override
                                    public void onNegativeClick() {

                                    }
                                });
                            } else {
                                DialogManager.getInstance().showProviderCompletedDialogPopup(getActivity(), mAppointmentDetails.getIssueName(), new InterfaceEdtBtnCallback() {
                                    @Override
                                    public void onPositiveClick(String amountStr) {
                                        AppointmentAcceptEntity appointmentAcceptEntity = new AppointmentAcceptEntity();
                                        appointmentAcceptEntity.setAppointmentId(mAppointmentDetails.getAppointmentId());
                                        appointmentAcceptEntity.setDateTime(DateUtil.getCurrentDate());
                                        appointmentAcceptEntity.setDuration(mAppointmentDetails.getDuration());
                                        appointmentAcceptEntity.setAmount(amountStr);
                                        appointmentAcceptEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
                                        APIRequestHandler.getInstance().completeAppointmentAPICall(appointmentAcceptEntity, ProviderMapFragment.this);

                                    }

                                    @Override
                                    public void onNegativeClick() {

                                    }
                                });
                            }
                        }
                    });

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
                            APIRequestHandler.getInstance().providerCancelAppointmentAPICall(userCancelEntity, ProviderMapFragment.this);

                        }
                    });
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
                                    final Handler handler=new Handler();
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            handler.removeCallbacks(this);
                                            startLocationUpdate();
                                        }
                                    };
                                    handler.postDelayed(runnable, 10000);
                                    getCheckPendingAppointmentAPICall();
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
        if (getActivity() != null && mGoogleMap != null && mCurrentLocation != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                       MarkerOptions marker = new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mVanMarkerBitmap));
                    mGoogleMap.addMarker(marker);
                }
            });


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


    /* Location update */
    private void startLocationUpdate() {
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*Ask for permission on customer_location access*/
                permissionsAccessLocation(false);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, AddressUtil.createLocationRequest(), ProviderMapFragment.this);
        }
    }


    private void screenAPICall() {
        /*Check for internet connection*/
        if (getActivity() != null) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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

    private void getCheckPendingAppointmentAPICall() {
        cancelCheckPendingAppointmentAPICallTimer();
        mCheckAppointmentTimer = new Timer();
        final PendingAppointmentInputEntity pendingAppointmentInputEntity = new PendingAppointmentInputEntity();
        pendingAppointmentInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
        pendingAppointmentInputEntity.setDateTime(DateUtil.getCurrentDate());

        mCheckAppointmentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                APIRequestHandler.getInstance().getUserPendingAppointmentAPICall(pendingAppointmentInputEntity, ProviderMapFragment.this);
            }
        }, 0, 5000);
    }

    private void cancelCheckPendingAppointmentAPICallTimer() {
        if (mCheckAppointmentTimer != null) {
            mCheckAppointmentTimer.cancel();
            mCheckAppointmentTimer.purge();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        if (getActivity() != null) {
            DialogManager.getInstance().showToast(getActivity(), "onConnectionSuspended");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (getActivity() != null) {
            DialogManager.getInstance().showToast(getActivity(), connectionResult.getErrorMessage());
        }
    }

    /* to stop the customer_location updates */
    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, ProviderMapFragment.this);
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
            locationUpdateInputEntity.setUserId(mUserIdStr);
            locationUpdateInputEntity.setLatitude(String.valueOf(location.getLatitude()));
            locationUpdateInputEntity.setLongitude(String.valueOf(location.getLongitude()));
            APIRequestHandler.getInstance().latAndLongUpdateAPICall(locationUpdateInputEntity, this);
        }
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


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);

        if (resObj instanceof PendingDetailsResponse) {
            PendingDetailsResponse pendingDetailsRes = (PendingDetailsResponse) resObj;

            if (getActivity() != null) {
                sysOut("Provider---" + pendingDetailsRes.getResult().getAnotheruser().size());

                if (pendingDetailsRes.getResult().getAnotheruser().size() > 0 && pendingDetailsRes.getResult().getAppointments().size() > 0) {

                    final UserDetailsEntity userDetails = pendingDetailsRes.getResult().getAnotheruser().get(0);
                    final AppointmentDetailsEntity appointmentDetails = pendingDetailsRes.getResult().getAppointments().get(0);
                    mAppointmentDetails = appointmentDetails;
                    mPendingUserDetails = userDetails;
                    mUserPhoneNumStr = userDetails.getPhoneNumber();
                    if (!mIsPendingAppointmentBool && (appointmentDetails.getStatus().equalsIgnoreCase("1"))) {
                        mIsPendingAppointmentBool = true;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNotificationDialog = DialogManager.getInstance().showNotificationPopup(getActivity(), new InterfaceTwoBtnCallback() {
                                    @Override
                                    public void onNegativeClick() {
                                        UserCancelEntity userCancelEntity = new UserCancelEntity();
                                        userCancelEntity.setAppointmentId(mAppointmentDetails.getAppointmentId());
                                        userCancelEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
                                        userCancelEntity.setDateTime(DateUtil.getCurrentDate());
                                        userCancelEntity.setCancelReason("");
                                        APIRequestHandler.getInstance().providerCancelAppointmentAPICall(userCancelEntity, ProviderMapFragment.this);


                                    }

                                    @Override
                                    public void onPositiveClick() {
                                        mAppointmentCardView.setVisibility(View.VISIBLE);
                                        mCustomerNameTxt.setText(userDetails.getUserName());
                                        mapDirection(Double.valueOf(userDetails.getLatitude()), Double.valueOf(userDetails.getLongitude()));

                                    }
                                });
                            }
                        });

                    } else if (appointmentDetails.getStatus().equalsIgnoreCase("2")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!mIsPendingAppointmentBool) {
                                    mIsPendingAppointmentBool = true;
                                    mAppointmentCardView.setVisibility(View.VISIBLE);
                                    mCustomerNameTxt.setText(userDetails.getUserName());
                                    mAcceptAppointmentTxt.setText(getString(R.string.work_done));
                                }
                                mapDirection(Double.valueOf(userDetails.getLatitude()), Double.valueOf(userDetails.getLongitude()));
                            }
                        });

                    } else if (mIsPendingAppointmentBool && (appointmentDetails.getStatus().equalsIgnoreCase("0") || appointmentDetails.getStatus().equalsIgnoreCase("3")
                            || appointmentDetails.getStatus().equalsIgnoreCase("4") || appointmentDetails.getStatus().equalsIgnoreCase("5"))) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDismiss(mNotificationDialog);
                                    mAppointmentCardView.setVisibility(View.GONE);
                                    mIsPendingAppointmentBool = false;
                                    mIsFirstAPIBool = true;
                                    screenAPICall();
                                }
                            });

                    }
                } else  if (pendingDetailsRes.getResult().getAnotheruser().size() == 0 && (
                        mIsPendingAppointmentBool || mAppointmentCardView.getVisibility() == View.VISIBLE)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            alertDismiss(mNotificationDialog);
                            mIsPendingAppointmentBool = false;
                            mAcceptAppointmentTxt.setText(getString(R.string.accept_appointment));
                            mAppointmentCardView.setVisibility(View.GONE);
                            mIsFirstAPIBool = true;
                            screenAPICall();
                        }
                    });
                }
            }
        }
        if (resObj instanceof UserCancelResponse) {
            if (mIsPendingAppointmentBool) {
                mAppointmentCardView.setVisibility(View.GONE);
                mIsFirstAPIBool = true;
                alertDismiss(mNotificationDialog);
                screenAPICall();
                mIsPendingAppointmentBool = false;
            }
        }
    }

    private void mapDirection(double userLat, double userLong) {
        if (mGoogleMap != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.user_loc);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            int heightSizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
            int widthSizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, widthSizeInt, heightSizeInt, false);
            MarkerOptions marker = new MarkerOptions().position(new LatLng(userLat, userLong)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mGoogleMap.addMarker(marker);
            setCurrentLocMarker();
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


    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        if (resObj instanceof String) {


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

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsFirstAPIBool) {
            getCheckPendingAppointmentAPICall();
        }
    }

    @Override
    public void onPause() {
        cancelCheckPendingAppointmentAPICallTimer();
        super.onPause();

    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        cancelCheckPendingAppointmentAPICallTimer();
        super.onDestroy();
    }


}

