package com.lipcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lipcap.R;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.output.CommonResponse;
import com.lipcap.model.output.UserDetailsEntity;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.PreferenceUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomerProfileFragment extends BaseFragment {

    @BindView(R.id.name_edt)
    EditText mNameEdt;

    @BindView(R.id.phone_num_edt)
    EditText mPhoneNumEdt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_cutomer_profile, container, false);
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

        mNameEdt.setText(userDetailsEntity.getName());
        mPhoneNumEdt.setText(userDetailsEntity.getMobileNo());

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
        } else   {
            APIRequestHandler.getInstance().updateProfileAPICall(mPhoneNumEdt.getText().toString().trim(), nameStr, this);
        }
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof CommonResponse) {
            if (getActivity() != null) {
                DialogManager.getInstance().showToast(getActivity(),getString(R.string.profile_updated));
                    getActivity().onBackPressed();

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

