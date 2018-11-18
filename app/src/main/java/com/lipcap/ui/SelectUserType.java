package com.lipcap.ui;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.lipcap.R;
import com.lipcap.main.BaseActivity;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SelectUserType extends BaseActivity {

    @BindView(R.id.select_user_parent_lay)
    ViewGroup mSelectUserViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_select_user_type);
        initView();
    }


    /*View initialization*/
    private void initView() {
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(mSelectUserViewGroup);

        setHeaderAdjustmentView();
    }

    /*Set header view*/
    private void setHeaderAdjustmentView() {
        /*Set header adjustment - status bar we applied transparent color so header tack full view*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSelectUserViewGroup.post(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSelectUserViewGroup.setPadding(0, getStatusBarHeight( SelectUserType.this), 0, 0);
                        }
                    });
                }
            });
        }
    }

    /*Screen orientation Changes*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setHeaderAdjustmentView();
    }

    /*View onClick*/
    @OnClick({R.id.header_start_img_lay,R.id.service_provider_btn, R.id.customer_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_start_img_lay:
                onBackPressed();
                break;
            case R.id.service_provider_btn:
            case R.id.customer_btn:
                PreferenceUtil.storeBoolPreferenceValue(this,AppConstants.CURRENT_USER_IS_PROVIDER,v.getId()==R.id.service_provider_btn);
                nextScreen(v.getId()==R.id.service_provider_btn?ProviderRegistration.class:CustomerRegistration.class);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        backScreen();
    }
}

