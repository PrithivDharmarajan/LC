package com.lidcap.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AddressUtil {


    /*create location request*/
    @SuppressLint("RestrictedApi")
    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);              // milli sec
        locationRequest.setFastestInterval(1000);      // milli sec
        locationRequest.setSmallestDisplacement(25f);  // in fet
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;


    }


  }
