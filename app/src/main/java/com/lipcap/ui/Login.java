package com.lipcap.ui;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lipcap.R;
import com.lipcap.main.BaseActivity;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.PreferenceUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Login extends BaseActivity {

    @BindView(R.id.login_parent_lay)
    ViewGroup mLoginViewGroup;

    @BindView(R.id.login_header_bg_lay)
    RelativeLayout mLoginHeaderBgLay;

    @BindView(R.id.login_header_img)
    ImageView mLoginHeaderImg;

    @BindView(R.id.header_txt)
    TextView mHeaderTxt;

    @BindView(R.id.header_left_img_lay)
    RelativeLayout mHeaderLeftImgLay;

    @BindView(R.id.email_id_edt)
    EditText mEmailAddressEdt;

    @BindView(R.id.pwd_edt)
    EditText mPwdEdt;

    @BindView(R.id.pwd_in_visible_img)
    ImageView mPwdInVisibleImg;

    private String mForgotEmailStr = "";

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
        mPwdEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 100 || actionId == EditorInfo.IME_ACTION_DONE) {
                    validateFields();
                }
                return true;
            }
        });

        setHeaderView();
        mPwdInVisibleImg.setTag(1);
    }

    private void setHeaderView() {

        /*set header changes*/
        mLoginHeaderImg.setVisibility(IsScreenModePortrait() ? View.VISIBLE : View.GONE);
        mHeaderLeftImgLay.setVisibility(View.INVISIBLE);
        mHeaderTxt.setVisibility(View.VISIBLE);
        mHeaderTxt.setText(getString(R.string.login));

        /*Set header adjustment - status bar we applied transparent color so header tack full view*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mLoginHeaderBgLay.post(new Runnable() {
                public void run() {
                    int heightInt = getResources().getDimensionPixelSize(IsScreenModePortrait() ? R.dimen.size190 : R.dimen.size45);
                    mLoginHeaderBgLay.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInt + NumberUtil.getInstance().getStatusBarHeight(Login.this)));
                    mLoginHeaderBgLay.setPadding(0, NumberUtil.getInstance().getStatusBarHeight(Login.this), 0, 0);
                    mLoginHeaderBgLay.setBackground(IsScreenModePortrait() ? getResources().getDrawable(R.drawable.header_bg) : getResources().getDrawable(R.color.blue));
                }

            });
        }

      //  mEmailAddressEdt.setText(PreferenceUtil.getStringValue(this,AppConstants.USER_EMAIL));
    }

    /*Screen orientation Changes*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setHeaderView();
    }

    /*View onClick*/
    @OnClick({R.id.forgot_pwd_txt, R.id.new_user_txt,R.id.pwd_in_visible_img, R.id.submit_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_pwd_txt:
                DialogManager.getInstance().showForgotPwdPopup(this, "", new InterfaceEdtBtnCallback() {
                    @Override
                    public void onPositiveClick(String emailStr) {
                        forgotPwdAPICall(emailStr);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
            case R.id.new_user_txt:
                nextScreen(Registration.class);
                break;
            case R.id.pwd_in_visible_img:
                int selectedInt=mPwdEdt.getText().toString().length();
                mPwdEdt.setTransformationMethod(mPwdInVisibleImg.getTag().equals(1)?null:new PasswordTransformationMethod());
                mPwdEdt.setSelection(selectedInt);
                mPwdInVisibleImg.setImageResource(mPwdInVisibleImg.getTag().equals(1)?R.drawable.visible:R.drawable.in_visible);
                mPwdInVisibleImg.setTag(mPwdInVisibleImg.getTag().equals(1)?0:1);
                break;
            case R.id.submit_btn:
                validateFields();
                break;
        }
    }

    /*validate fields*/
    private void validateFields() {
        hideSoftKeyboard(this);
        String emailAddressStr = mEmailAddressEdt.getText().toString().trim();
        String pwdStr = mPwdEdt.getText().toString().trim();

        if (emailAddressStr.isEmpty()) {
            mEmailAddressEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.enter_email_id), this);
        } else if (!PatternMatcherUtil.isEmailValid(emailAddressStr)) {
            mEmailAddressEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.enter_valid_email_id), this);
        } else if (pwdStr.isEmpty()) {
            mPwdEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(this, getString(R.string.enter_pwd), this);
        } else {
             LoginRegistrationInputModel loginInputModel = new  LoginRegistrationInputModel();
            LoginRegistrationMobileInputModel loginMobileInputModel = new  LoginRegistrationMobileInputModel();

            loginInputModel.setEmail(emailAddressStr);
            loginInputModel.setPassword(pwdStr);

            loginMobileInputModel.setNotificationToken("12345");
//            loginMobileInputModel.setMsisdn(getPhone());
            loginMobileInputModel.setOs(AppConstants.ANDROID);
            loginMobileInputModel.setLocale(getResources().getConfiguration().locale.getCountry());
            loginInputModel.setMobileDevice(loginMobileInputModel);
            loginAPICall(loginInputModel);
        }
    }

    /*API calls*/
    private void loginAPICall( LoginRegistrationInputModel loginInputModel) {
         APIRequestHandler.getInstance().loginAPICall(loginInputModel, this);
    }

    private void forgotPwdAPICall(String emailStr) {
        mForgotEmailStr = emailStr;
         APIRequestHandler.getInstance().resetAPICall(mForgotEmailStr, Login.this);
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof LoginResponse) {
            LoginResponse loginResponse = ( LoginResponse) resObj;
            PreferenceUtil.storeStringValue(this, AppConstants.AUTHORIZATION, loginResponse.getToken());
            PreferenceUtil.storeBoolPreferenceValue(this, AppConstants.LOGIN_STATUS, true);
            PreferenceUtil.storeStringValue(this, AppConstants.USER_ID, loginResponse.getUserId());
            PreferenceUtil.storeStringValue(this, AppConstants.USER_EMAIL, mEmailAddressEdt.getText().toString().trim());
            nextScreen(Dashboard.class); 
        } else if (resObj instanceof CommonResponse) {
            DialogManager.getInstance().showAlertPopup(this, "Success", this);
        }
    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        super.onRequestFailure(resObj, t);
        if (t instanceof IOException) {

            if (resObj instanceof  LoginResponse) {
                DialogManager.getInstance().showAlertPopup(this,
                        (t instanceof java.net.ConnectException ||t instanceof java.net.UnknownHostException? getString(R.string.no_internet) : getString(R.string
                                .connect_time_out)), new InterfaceBtnCallback() {
                            @Override
                            public void onPositiveClick() {

                            }
                        });
            } else if (resObj instanceof  CommonResponse) {
                DialogManager.getInstance().showNetworkErrorPopup(this,
                        (t instanceof java.net.ConnectException||t instanceof java.net.UnknownHostException ? getString(R.string.no_internet) : getString(R.string
                                .connect_time_out)), new InterfaceBtnCallback() {
                            @Override
                            public void onPositiveClick() {

                                DialogManager.getInstance().showForgotPwdPopup(Login.this, mForgotEmailStr, new InterfaceEdtBtnCallback() {
                                    @Override
                                    public void onPositiveClick(String emailStr) {
                                        forgotPwdAPICall(emailStr);
                                    }

                                    @Override
                                    public void onNegativeClick() {

                                    }
                                });
                            }

                        });
            }

        }
    }
}

