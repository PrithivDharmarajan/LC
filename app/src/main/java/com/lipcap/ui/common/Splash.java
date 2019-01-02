package com.lipcap.ui.common;

import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;

import com.lipcap.R;
import com.lipcap.main.BaseActivity;
import com.lipcap.ui.customer.CustomerHome;
import com.lipcap.ui.provider.ProviderHome;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Splash extends BaseActivity {

    /*Variable initialization using bind view*/

    @BindView(R.id.parent_lay)
    ViewGroup mSplashViewGroup;

    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ui_splash);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*View initialization*/
    private void initView() {
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this);

        /*Keypad to be hidden when a Click/touch made outside the edit text*/
        setupUI(mSplashViewGroup);

        /*next screen move*/
        nextScreenCheck();

    }


    private void nextScreenCheck() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                removeHandler();
                Class<?> nextScreenClass = Login.class;
                if (PreferenceUtil.getBoolPreferenceValue(Splash.this, AppConstants.LOGIN_STATUS)) {
                    nextScreenClass = PreferenceUtil.getBoolPreferenceValue(Splash.this, AppConstants.CURRENT_USER_IS_PROVIDER) ? ProviderHome.class : CustomerHome.class;
                }

                AppConstants.IS_FROM_PUSH = false;
                sysOut("Push-Id " + PreferenceUtil.getStringPreferenceValue(Splash.this, AppConstants.PUSH_DEVICE_ID));
                nextScreen(nextScreenClass);
            }
        };

        sysOut("USer ID---"+PreferenceUtil.getUserId(this));
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 3000);
    }

    private void removeHandler() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeHandler();
    }


}
