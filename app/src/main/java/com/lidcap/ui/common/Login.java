package com.lidcap.ui.common;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.lidcap.R;
import com.lidcap.main.BaseActivity;
import com.lidcap.model.input.LoginInputEntity;
import com.lidcap.model.output.LoginResponse;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.ui.customer.CustomerHome;
import com.lidcap.ui.provider.ProviderHome;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DateUtil;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.InterfaceBtnCallback;
import com.lidcap.utils.PreferenceUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Login extends BaseActivity {

    @BindView(R.id.login_parent_lay)
    ViewGroup mLoginViewGroup;

    @BindView(R.id.phone_num_edt)
    EditText mPhoneNumEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_login);
        initView();
    }


    /*View initialization*/
    private void initView() {
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(mLoginViewGroup);

        /*Keypad button action*/
        mPhoneNumEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 100 || actionId == EditorInfo.IME_ACTION_DONE) {
                    validateFields();
                }
                return true;
            }
        });

        setHeaderAdjustmentView();
    }

    /*Set header view*/
    private void setHeaderAdjustmentView() {
        /*Set header adjustment - status bar we applied transparent color so header tack full view*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mLoginViewGroup.post(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoginViewGroup.setPadding(0, getStatusBarHeight(Login.this), 0, 0);
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
    @OnClick({R.id.header_start_img_lay, R.id.login_btn, R.id.sign_up_txt})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_start_img_lay:
                onBackPressed();
                break;
            case R.id.login_btn:
                validateFields();
                break;
            case R.id.sign_up_txt:
                nextScreen(SelectUserType.class);
                break;
        }
    }

    /*validate fields*/
    private void validateFields() {
        hideSoftKeyboard(this);
        String phoneNumStr = mPhoneNumEdt.getText().toString().trim();

        if (phoneNumStr.isEmpty()) {
            mPhoneNumEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.plz_enter_phone_num), this);
        } else {
            LoginInputEntity loginInputEntity=new LoginInputEntity();
            loginInputEntity.setPhoneNumber(phoneNumStr);
            loginInputEntity.setDeviceType(AppConstants.ANDROID);
            loginInputEntity.setUpdateDate(DateUtil.getCurrentDate());
            loginInputEntity.setDeviceToken(PreferenceUtil.getStringPreferenceValue(this,AppConstants.PUSH_DEVICE_ID));
            APIRequestHandler.getInstance().loginAPICall(loginInputEntity, this);
        }
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof LoginResponse) {
            LoginResponse loginResponse = (LoginResponse) resObj;
            if (loginResponse.getStatusCode().equals(AppConstants.SUCCESS_CODE)) {
                if (loginResponse.getResult().size() > 0) {
                    PreferenceUtil.storeBoolPreferenceValue(this, AppConstants.LOGIN_STATUS, true);
                    PreferenceUtil.storeUserDetails(this, loginResponse.getResult().get(0));
                    DialogManager.getInstance().showToast(this, getString(R.string.logged_in_success));
                    nextScreen(PreferenceUtil.getBoolPreferenceValue(Login.this, AppConstants.CURRENT_USER_IS_PROVIDER) ? ProviderHome.class : CustomerHome.class);
                }
            } else {
                DialogManager.getInstance().showAlertPopup(this, loginResponse.getMessage(), this);
            }

        }

    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        super.onRequestFailure(resObj, t);
        if (t instanceof IOException) {
            DialogManager.getInstance().showAlertPopup(this,
                    (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException ? getString(R.string.no_internet) : getString(R.string
                            .connect_time_out)), new InterfaceBtnCallback() {
                        @Override
                        public void onPositiveClick() {
                        }
                    });
        }
    }

}

