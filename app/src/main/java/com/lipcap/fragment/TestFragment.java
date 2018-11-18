package com.lipcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.lipcap.R;
import com.lipcap.main.BaseFragment;
import com.lipcap.ui.CustomerHome;
import com.lipcap.utils.AppConstants;

import java.util.Objects;

import butterknife.ButterKnife;

public class TestFragment extends BaseFragment   {

    private final int REQUEST_CHECK_SETTINGS = 300;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.test_frag, container, false);
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
        ((CustomerHome) Objects.requireNonNull(getActivity())).setHeaderTxt(AppConstants.TEMP_HEADER);

    }



}

