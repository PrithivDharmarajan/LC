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


    public static ArrayList<String> getAddressFromLatLng(Context context, Double latitude, Double longitude) throws IOException {

        ArrayList<String> addressFromLatLonList = new ArrayList<>();
        String addressStr = "", cityStr = "", stateStr = "", countryStr = "", addressLineStr = "";

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0 && addresses.get(0).getAddressLine(0) != null) {
                addressStr = addresses.get(0).getAddressLine(0);

                if (!addressStr.isEmpty() && addresses.get(0).getAdminArea() != null
                        && addresses.get(0).getCountryName() != null && addresses.get(0).getLocality() != null
                        && !addresses.get(0).getLocality().isEmpty() && !addresses.get(0).getAdminArea().isEmpty() &&
                        !addresses.get(0).getCountryName().isEmpty()) {

                    addressLineStr = addresses.get(0).getSubLocality();
                    cityStr = addresses.get(0).getLocality();
                    stateStr = addresses.get(0).getAdminArea();
                    countryStr = addresses.get(0).getCountryName();
                    addressStr = cityStr + ", " + stateStr + ", " + countryStr;

                    addressFromLatLonList.add(addressStr);
                    addressFromLatLonList.add(cityStr);
                    addressFromLatLonList.add(countryStr);
                    addressFromLatLonList.add(stateStr);


                }
                if (addressFromLatLonList.size() == 0 && !addressStr.isEmpty()) {
                    addressFromLatLonList.add(addressStr);
                }

                return addressFromLatLonList;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return addressFromLatLonList;
        } finally {
            return addressFromLatLonList;
        }

    }
}
