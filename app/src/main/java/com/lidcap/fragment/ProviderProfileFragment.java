package com.lidcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lidcap.R;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.IssuesInputEntity;
import com.lidcap.model.output.LoginResponse;
import com.lidcap.model.output.UserDetailsEntity;
import com.lidcap.services.APIRequestHandler;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DialogManager;
import com.lidcap.utils.InterfaceBtnCallback;
import com.lidcap.utils.PreferenceUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProviderProfileFragment extends BaseFragment {

    @BindView(R.id.name_edt)
    EditText mNameEdt;

    @BindView(R.id.phone_num_edt)
    EditText mPhoneNumEdt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_provider_profile, container, false);
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
        if (getActivity() != null) {
            initView();
        }
    }


    /*InitViews*/
    private void initView() {
        AppConstants.TAG = this.getClass().getSimpleName();
        setData();


    }

    private void setData() {
        UserDetailsEntity userDetailsEntity = PreferenceUtil.getUserDetailsRes(getActivity());

        mNameEdt.setText(userDetailsEntity.getUserName());
        mPhoneNumEdt.setText(userDetailsEntity.getPhoneNumber());

        mNameEdt.setSelection(mNameEdt.getText().toString().trim().length());
        mPhoneNumEdt.setSelected(false);
        mPhoneNumEdt.setFocusableInTouchMode(false);
    }

    /*View onClick*/
    @OnClick({R.id.submit_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn:
                validateFields();
                break;

        }
    }

    /*validate fields*/
    private void validateFields() {
        hideSoftKeyboard();
        String nameStr = mNameEdt.getText().toString().trim();

        if (nameStr.isEmpty()) {
            mNameEdt.requestFocus();
            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.plz_enter_name), this);
        }  else {
            IssuesInputEntity profileEntity=new IssuesInputEntity();
            profileEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
            profileEntity.setUserName(nameStr);
            APIRequestHandler.getInstance().updateProfileAPICall(profileEntity, this);
        }
    }

    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof LoginResponse) {
            if (getActivity() != null) {
                LoginResponse loginResponse = (LoginResponse) resObj;
                PreferenceUtil.storeUserDetails(getActivity(), loginResponse.getResult().get(0));
                if (loginResponse.getStatusCode().equals(AppConstants.SUCCESS_CODE)) {
                    if (loginResponse.getResult().size() > 0) {
                        PreferenceUtil.storeUserDetails(getActivity(), loginResponse.getResult().get(0));
                        DialogManager.getInstance().showToast(getActivity(),getString(R.string.profile_updated));
                        getActivity().onBackPressed();
                    }
                } else {
                    DialogManager.getInstance().showAlertPopup(getActivity(), loginResponse.getMessage(), this);
                }
            }
        }
    }


    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        super.onRequestFailure(resObj, t);
        if (t instanceof IOException) {
            DialogManager.getInstance().showAlertPopup(getActivity(),
                    (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException ? getString(R.string.no_internet) : getString(R.string
                            .connect_time_out)), new InterfaceBtnCallback() {
                        @Override
                        public void onPositiveClick() {
                        }
                    });
        }
    }

}

