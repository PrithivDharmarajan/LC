package com.lipcap.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.lipcap.R;
import com.lipcap.main.BaseFragment;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.InterfaceTwoBtnCallback;
import com.lipcap.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProviderMapFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener
        , GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mGoogleMap;

    private GoogleApiClient mGoogleApiClient;
    private final int REQUEST_CHECK_SETTINGS = 300;

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
        if(fragment!=null)
            fragment.getMapAsync(this);



    }



    /*Click Event*/
    @OnClick({R.id.book_appointment_btn,R.id.show_current_location_img})
    public void onClick(View v) {
        if (getActivity() != null) {
            switch (v.getId()) {
                case R.id.book_appointment_btn:
                    break;
                case R.id.show_current_location_img:
                    setCurrentLocation();
                    break;


            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
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

        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Cleaning all the markers.


            }
        });


    }



    /*Init Google API clients*/
    private void initGoogleAPIClient() {
        if (getActivity() != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(ProviderMapFragment.this)
                    .addOnConnectionFailedListener(ProviderMapFragment.this)
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
            /* location Request */
            locationRequest.setSmallestDisplacement(25f);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*Ask for permission on location access*/
                permissionsAccessLocation(1);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, ProviderMapFragment.this);
        }
    }

    /*Set current location to map view*/
    private void setCurrentLocation() {
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsAccessLocation(4);
                return;
            }
            if (mGoogleMap != null) {
                /* Enable current location */
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                Location currentLoc = getCurrentLatLong();
                LatLng coordinate = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
                hideSoftKeyboard();

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));

            }
        }
    }


    /* Get current location */
    private Location getCurrentLatLong() {

        Location location = new Location("");
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*Ask for permission on locatio access
                 * Set flag for call back to continue this process*/
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
            if (mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
                                    // API call.

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

    /* to stop the location updates */
    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, ProviderMapFragment.this);
        }

    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
//            AppConstants.CURRENT_LATITUDE = location.getLatitude();
//            AppConstants.CURRENT_LONGITUDE = location.getLongitude();
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


}

