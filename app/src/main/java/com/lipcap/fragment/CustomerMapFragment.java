package com.lipcap.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lipcap.R;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.output.ProviderDetailsEntity;
import com.lipcap.model.output.ProviderDetailsResponse;
import com.lipcap.model.output.SelectIssuesTypeResponse;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.InterfaceEdtBtnCallback;
import com.lipcap.utils.InterfaceTwoBtnCallback;
import com.lipcap.utils.NetworkUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomerMapFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener
        , GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {


    private final int REQUEST_CHECK_SETTINGS = 300;
    @BindView(R.id.appointment_card_view)
    CardView mAppointmentCardView;
    @BindView(R.id.appointment_accepted_txt)
    TextView mAppointmentAcceptedTxt;
    @BindView(R.id.book_appointment_btn)
    Button mBookAppointmentBtn;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private boolean isDialogShowing = false;
    private String mLatitudeStr = "", mLongitudeStr = "";
    private Timer mAPICallTimer;

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


    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();

        initGoogleAPIClient();

        SupportMapFragment fragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);

        /* Map synchronization */
        if (fragment != null)
            fragment.getMapAsync(this);

    }


    /*Click Event*/
    @OnClick({R.id.show_current_location_img, R.id.book_appointment_btn, R.id.cancel_appointment_lay})
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
                    isDialogShowing = true;
                    DialogManager.getInstance().showReasonForCancelPopup(getActivity(), new InterfaceEdtBtnCallback() {
                        @Override
                        public void onPositiveClick(String editStr) {
                            isDialogShowing = false;
                        }

                        @Override
                        public void onNegativeClick() {
                            isDialogShowing = false;
                            commentDialog();
                        }
                    });
                    break;
            }
        }
    }

    private void commentDialog() {

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                removeDialog();
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
        };

        mHandler.postDelayed(mRunnable, 2000);

    }

    private void removeDialog() {
        if (mHandler != null)
            mHandler.removeCallbacks(mRunnable);

    }

    private void issueListAPICall() {
        APIRequestHandler.getInstance().selectIssueTypeAPICall(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (getActivity() != null && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsAccessLocation(2);
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
            }
        });


    }


    /*Init Google API clients*/
    private void initGoogleAPIClient() {
        if (getActivity() != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(CustomerMapFragment.this)
                    .addOnConnectionFailedListener(CustomerMapFragment.this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }


    /* Location update */
    private void startLocationUpdate() {
        if (getActivity() != null) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            /* customer_location Request */
            locationRequest.setSmallestDisplacement(25f);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*Ask for permission on customer_location access*/
                permissionsAccessLocation(1);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, CustomerMapFragment.this);
        }
    }

    /*Set current customer_location to map view*/
    private void setCurrentLocation() {
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsAccessLocation(4);
                return;
            }
            if (mGoogleMap != null) {
                /* Enable current customer_location */
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                Location currentLoc = getCurrentLatLong();
                LatLng coordinate = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
                hideSoftKeyboard();

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));

            }
        }
    }


    /* Get current customer_location */
    private Location getCurrentLatLong() {

        Location location = new Location("");
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*Ask for permission on locatio access
                 * Set flag for call_provider back to continue this process*/
                permissionsAccessLocation(3);
            }

            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                location.setLatitude(mLastLocation.getLatitude());
                location.setLongitude(mLastLocation.getLongitude());

            }
        }

        return location;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                            initGoogleAPIClient();
                        } else {
                            screenAPICall();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // user does not want to update setting. Handle it in a way that it will to affect your app functionality
                        if (getActivity() != null)
                            DialogManager.getInstance().showToast(getActivity(), getString(R.string.user_not_update));
                        break;
                }
                break;
        }
    }

    private void screenAPICall() {
        /*Check for internet connection*/
        if (getActivity() != null) {
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
                setCurrentLocation();
                startLocationUpdate();
                checkForNewCustomers();
            } else {
                /*Alert message will be appeared if there is no internet connection*/
                DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.no_internet), new InterfaceBtnCallback() {
                    @Override
                    public void onPositiveClick() {

                    }
                });

            }
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
                LocationSettingsRequest.Builder locSettingsReqBuilder = new LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest());
                PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locSettingsReqBuilder.build());
                pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        if (getActivity() != null) {
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // API call_provider.

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
                                        // and check the result in onActivityResult().
                                        status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way
                                    // to fix the settings so we won't show the dialog.
                                    break;
                            }
                        }
                    }
                });
            }
        }
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);              // milli sec
        locationRequest.setFastestInterval(1000);      // milli sec
        locationRequest.setSmallestDisplacement(25f);  // in fet
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
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
                    mGoogleApiClient, CustomerMapFragment.this);
        }

    }

    private void checkForNewCustomers() {
        cancelAPICallTimer();

        mAPICallTimer = new Timer();
        mAPICallTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                APIRequestHandler.getInstance().getProviderLocAPICall(mLatitudeStr, mLongitudeStr, CustomerMapFragment.this);
            }
        }, 0, 60000);
    }

    @Override
    public void onPause() {
        cancelAPICallTimer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    private void cancelAPICallTimer() {
        if (mAPICallTimer != null) {
            mAPICallTimer.cancel();
            mAPICallTimer.purge();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLatitudeStr = String.valueOf(location.getLatitude());
            mLongitudeStr = String.valueOf(location.getLongitude());
            APIRequestHandler.getInstance().latAndLongUpdateAPICall(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), 1 + "", this);

        }
    }

    /* Ask for permission on Location access*/
    private boolean permissionsAccessLocation(final int askPermissionFromIntFlag) {
        if (getActivity() != null) {
            boolean addPermission = true;
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (android.os.Build.VERSION.SDK_INT >= 23) {
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
                        if (askPermissionFromIntFlag == 0) {
                            initView();
                        } else if (askPermissionFromIntFlag == 1) {
                            startLocationUpdate();

                        } else if (askPermissionFromIntFlag == 2 || askPermissionFromIntFlag == 4) {
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mGoogleMap.setMyLocationEnabled(true);
                            if (askPermissionFromIntFlag == 4) {
                                setCurrentLocation();
                            }
                        }
                    }

                    @Override
                    public void onPositiveClick() {

                    }


                });
            }

            return addPermission;
        } else {
            return false;
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    public void moveMapCamera(final String lat, final String lang) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LatLng moveLoc = new LatLng(Double.parseDouble(!lat.isEmpty() ?
                            lat : "0.0"),
                            Double.parseDouble(!lang.isEmpty() ? lang : "0.0"));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(moveLoc, 17));
                }
            });
        }

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
                        public void onPositiveClick(String editStr) {
                            mAppointmentCardView.setVisibility(View.VISIBLE);
                            mBookAppointmentBtn.setVisibility(View.GONE);
                            commentDialog();
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

        if (resObj instanceof ProviderDetailsResponse) {
            ProviderDetailsResponse providerResponse = (ProviderDetailsResponse) resObj;
            if (providerResponse.getUserDetail().size() > 0) {
                providerMarkerDetails(providerResponse.getUserDetail());
            }
        }


    }

    private void providerMarkerDetails(ArrayList<ProviderDetailsEntity> providerDetails) {
        mGoogleMap.clear();

        for (int i = 0; i < providerDetails.size(); i++) {
            if (mGoogleMap != null) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.van);
                Bitmap bitmap = bitmapDrawable.getBitmap();

                int sizeInt = getResources().getDimensionPixelSize(R.dimen.size30);
                Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, sizeInt, sizeInt, false);
                MarkerOptions marker = new MarkerOptions().position(new LatLng(providerDetails.get(i).getLatitude(), providerDetails.get(i).getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                mGoogleMap.addMarker(marker);
            }
        }

    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
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


}

