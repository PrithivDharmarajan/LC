package com.lidcap.fragment;

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
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lidcap.R;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.AppointmentAcceptEntity;
import com.lidcap.model.input.LocationUpdateInputEntity;
import com.lidcap.model.input.PendingAppointmentInputEntity;
import com.lidcap.model.input.UserCancelEntity;
import com.lidcap.model.output.AppointmentDetailsEntity;
import com.lidcap.model.output.PendingDetailsResponse;
import com.lidcap.model.output.UserCancelResponse;
import com.lidcap.model.output.UserDetailsEntity;
import com.lidcap.services.APIRequestHandler;
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

public class ProviderMapFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {


    private final int REQUEST_CHECK_SETTINGS_INT = 300;
    @BindView(R.id.customer_name_txt)
    TextView mCustomerNameTxt;
    @BindView(R.id.appointment_card_view)
    CardView mAppointmentCardView;
    @BindView(R.id.accept_appointment_txt)
    TextView mAcceptAppointmentTxt;
    @BindView(R.id.reach_time_txt)
    TextView mReachlTimeTxt;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsFirstAPIBool = true, mIsPendingAppointmentBool = false;
    private Location mCurrentLocation;
    private Timer mCheckAppointmentTimer;
    private String mUserIdStr = "", mUserPhoneNumStr = "";
    private int mOldPendingStatusInt = -1;
    private Dialog mNotificationDialog;
    private AppointmentDetailsEntity mAppointmentDetails = new AppointmentDetailsEntity();
    private BitmapDescriptor mVanMarkerBitmapDescriptor, mUserLocMarkerBitmapDescriptor;
    private double mUserLastLat, mUserLastLng, mCurrentUserLastLat, mCurrentUserLastLng;


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
        mOldPendingStatusInt = -1;
        mUserIdStr = PreferenceUtil.getUserId(getActivity());


        /*Van Marker Init*/
        BitmapDrawable vanBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.van);
        Bitmap vanBitmap = vanBitmapDrawable.getBitmap();
        int sizeInt = getResources().getDimensionPixelSize(R.dimen.size30);

        mVanMarkerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(vanBitmap, sizeInt, sizeInt, false));

        /*User loc Marker Init*/
        BitmapDrawable currentLocBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.user_loc);
        Bitmap currentLocBitmap = currentLocBitmapDrawable.getBitmap();

        mUserLocMarkerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(currentLocBitmap, sizeInt, sizeInt, false));

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
                            mOldPendingStatusInt = -1;
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
                                mCurrentUserLastLat = location.getLatitude();
                                mCurrentUserLastLng = location.getLongitude();
                                mCurrentLocation = location;

                                if (mIsFirstAPIBool) {
                                    mIsFirstAPIBool = false;
                                    setCurrentLocMarker();
                                    final Handler handler = new Handler();
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
                                if (getActivity() != null && ActivityCompat.checkSelfPermission(getActivity(),
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
                    MarkerOptions marker = new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).icon(mVanMarkerBitmapDescriptor);
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
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, AddressUtil.createLocationRequest(), ProviderMapFragment.this);
        }
    }


    private void screenAPICall() {
        /*Check for internet connection*/
        if (getActivity() != null) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

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
                if (pendingDetailsRes.getResult().getAnotheruser().size() > 0 && pendingDetailsRes.getResult().getAppointments().size() > 0) {
                    mIsPendingAppointmentBool = true;

                    sysOut("Status : " + pendingDetailsRes.getResult().getAppointments().get(0).getStatus());
                    final UserDetailsEntity userDetails = pendingDetailsRes.getResult().getAnotheruser().get(0);
                    mAppointmentDetails = pendingDetailsRes.getResult().getAppointments().get(0);

                    mUserPhoneNumStr = userDetails.getPhoneNumber();
                    if (mOldPendingStatusInt != 1 && (mAppointmentDetails.getStatus().equalsIgnoreCase("1"))) {
                        mOldPendingStatusInt = 1;
                        mIsPendingAppointmentBool = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNotificationDialog = DialogManager.getInstance().showProviderNotificationPopup(getActivity(), new InterfaceTwoBtnCallback() {
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

                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    mAppointmentCardView.setVisibility(View.VISIBLE);
                                                    mAcceptAppointmentTxt.setText(getString(R.string.accept_appointment));
                                                    mCustomerNameTxt.setText(userDetails.getUserName());
                                                    if (!userDetails.getLatitude().isEmpty() && !userDetails.getLongitude().isEmpty()) {
                                                        mapDirection(Double.valueOf(userDetails.getLatitude()), Double.valueOf(userDetails.getLongitude()));
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                            }
                        });

                    } else if (getActivity() != null && mOldPendingStatusInt != 2 && mAppointmentDetails.getStatus().equalsIgnoreCase("2")) {
                        mOldPendingStatusInt = 2;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mIsPendingAppointmentBool = true;
                                mAppointmentCardView.setVisibility(View.VISIBLE);
                                mCustomerNameTxt.setText(userDetails.getUserName());
                                mAcceptAppointmentTxt.setText(getString(R.string.work_done));

                                mapDirection(Double.valueOf(userDetails.getLatitude()), Double.valueOf(userDetails.getLongitude()));
                            }
                        });

                    } else if (mIsPendingAppointmentBool && (mAppointmentDetails.getStatus().equalsIgnoreCase("0") || mAppointmentDetails.getStatus().equalsIgnoreCase("3")
                            || mAppointmentDetails.getStatus().equalsIgnoreCase("4") || mAppointmentDetails.getStatus().equalsIgnoreCase("5"))) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDismiss(mNotificationDialog);
                                    mOldPendingStatusInt = -1;
                                    mAppointmentCardView.setVisibility(View.GONE);
                                    mIsPendingAppointmentBool = false;
                                    mIsFirstAPIBool = true;
                                    screenAPICall();
                                }
                            });

                    }
                } else if (pendingDetailsRes.getResult().getAnotheruser().size() == 0 && (
                        mIsPendingAppointmentBool || mAppointmentCardView.getVisibility() == View.VISIBLE)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            alertDismiss(mNotificationDialog);
                            mIsPendingAppointmentBool = false;
                            mOldPendingStatusInt = -1;
                            mAcceptAppointmentTxt.setText(getString(R.string.accept_appointment));
                            mAppointmentCardView.setVisibility(View.GONE);
                            mIsFirstAPIBool = true;
                            if (mGoogleMap != null) {
                                mGoogleMap.clear();
                                setCurrentLocMarker();
                            }

                            screenAPICall();
                        }
                    });
                }
            }
        }
        if (resObj instanceof UserCancelResponse && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAppointmentCardView.getVisibility() == View.VISIBLE) {
                        mAppointmentCardView.setVisibility(View.GONE);
                        alertDismiss(mNotificationDialog);
                        mOldPendingStatusInt = -1;
                        mIsPendingAppointmentBool = false;
                        mIsFirstAPIBool = true;
                        screenAPICall();
                    }
                }
            });

        }
    }

    private void mapDirection(double userLat, double userLong) {
        if (mGoogleMap != null && (mUserLastLat != userLat || mUserLastLng != userLong)) {
            mGoogleMap.clear();
            mUserLastLat = userLat;
            mUserLastLng = userLong;
            MarkerOptions marker = new MarkerOptions().position(new LatLng(userLat, userLong)).icon(mUserLocMarkerBitmapDescriptor);
            mGoogleMap.addMarker(marker);
            setCurrentLocMarker();
            directionPoint();
        }

    }

    private void directionPoint() {
        LatLng origin = new LatLng(mUserLastLat, mUserLastLng);
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

    private void makePhoneCall() {
        if (getActivity() != null && !mUserPhoneNumStr.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL);

            intent.setData(Uri.parse("tel:" + mUserPhoneNumStr));
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                if (getActivity() != null) {
                    lineOptions.addAll(points);
                    lineOptions.width(6);
                    lineOptions.color(ContextCompat.getColor(getActivity(), R.color.orange));
                }
            }

//            tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);

            if (getActivity() != null) {
                final PolylineOptions finalLineOptions = lineOptions;
                final String finalDuration = duration;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mReachlTimeTxt.setText(String.format(getString(R.string.reach_time), finalDuration));
                        // Drawing polyline in the Google Map for the i-th route
                        if (mGoogleMap != null)
                            mGoogleMap.addPolyline(finalLineOptions);
                    }
                });
            }

        }
    }

    @Override
    public void onDestroyView() {
        mOldPendingStatusInt = -1;
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        cancelCheckPendingAppointmentAPICallTimer();
        super.onDestroy();
    }
}

