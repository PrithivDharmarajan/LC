package com.lidcap.ui.provider;

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
import com.lidcap.model.input.RegInputEntity;
import com.lidcap.model.output.LoginResponse;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.ui.common.Login;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DateUtil;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.InterfaceBtnCallback;
import com.lidcap.utils.PreferenceUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ProviderRegistration extends BaseActivity {

    @BindView(R.id.prov_reg_parent_lay)
    ViewGroup mProvRegViewGroup;

    @BindView(R.id.name_edt)
    EditText mNameEdt;

    @BindView(R.id.phone_num_edt)
    EditText mPhoneNumEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_provider_registration);
        initView();
    }


    /*View initialization*/
    private void initView() {
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(mProvRegViewGroup);

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
            mProvRegViewGroup.post(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProvRegViewGroup.setPadding(0, getStatusBarHeight(ProviderRegistration.this), 0, 0);
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
    @OnClick({R.id.header_start_img_lay, R.id.submit_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_start_img_lay:
                onBackPressed();
                break;
            case R.id.submit_btn:
                validateFields();
                break;

        }
    }

    /*validate fields*/
    private void validateFields() {
        hideSoftKeyboard(this);
        String nameStr = mNameEdt.getText().toString().trim();
        String phoneNumStr = mPhoneNumEdt.getText().toString().trim();

        if (nameStr.isEmpty()) {
            mNameEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.plz_enter_name), this);
        } else if (phoneNumStr.isEmpty()) {
            mPhoneNumEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.plz_enter_phone_num), this);
        } else if (phoneNumStr.length() < 10) {
            mPhoneNumEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.plz_enter_valid_phone_num), this);
        } else {
            RegInputEntity regInputEntity=new RegInputEntity();
            regInputEntity.setUserName(nameStr);
            regInputEntity.setPhoneNumber(phoneNumStr);
            regInputEntity.setDeviceToken(PreferenceUtil.getStringPreferenceValue(this,AppConstants.PUSH_DEVICE_ID));
            regInputEntity.setUserType("2");
            regInputEntity.setDeviceType(AppConstants.ANDROID);
            regInputEntity.setCreateDate(DateUtil.getCurrentDate());

            APIRequestHandler.getInstance().registrationAPICall( regInputEntity,this);
        }
    }

    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof LoginResponse) {
            LoginResponse registrationResponse = (LoginResponse) resObj;
            if(registrationResponse.getStatusCode().equals(AppConstants.SUCCESS_CODE)){
                DialogManager.getInstance().showAlertPopup(this,  getString(R.string.your_acc_activated), new InterfaceBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        AppConstants.IS_FROM_PUSH = false;
                        previousScreen(Login.class);
                    }
                });
            }else{
                DialogManager.getInstance().showAlertPopup(this, registrationResponse.getMessage(),this);
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

    @Override
    public void onBackPressed() {
        backScreen();
    }
}

