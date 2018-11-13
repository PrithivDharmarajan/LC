package com.lipcap.ui;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.lipcap.R;
import com.lipcap.main.BaseActivity;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.PreferenceUtil;

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
                            mLoginViewGroup.setPadding(0, getStatusBarHeight( Login.this), 0, 0);
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
    @OnClick({R.id.header_start_img_lay,R.id.login_btn, R.id.sign_up_txt})
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
        }  else {
            PreferenceUtil.storeBoolPreferenceValue(this,AppConstants.LOGIN_STATUS_BOOL,true);
            DialogManager.getInstance().showToast(this,"Successfully logged in");
        }
    }

}

