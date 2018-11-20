package com.lipcap.services;


import android.support.annotation.NonNull;

import com.lipcap.main.BaseActivity;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.output.IssuesListResponse;
import com.lipcap.model.output.LoginResponse;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.PreferenceUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIRequestHandler {

    private static APIRequestHandler sInstance = new APIRequestHandler();

    /*Init retrofit for API call*/
    private APICommonInterface mServiceInterface = serviceInterface();

    public static APIRequestHandler getInstance() {
        return sInstance;
    }

    private APICommonInterface serviceInterface() {
        return new Retrofit.Builder().baseUrl(AppConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(APICommonInterface.class);
    }

    /*Login API*/
    public void loginAPICall(String mobileNumStr, final BaseActivity baseActivity) {
        DialogManager.getInstance().showProgress(baseActivity);
        mServiceInterface.loginAPI(mobileNumStr,PreferenceUtil.getStringPreferenceValue(baseActivity,AppConstants.PUSH_DEVICE_ID)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                DialogManager.getInstance().hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    baseActivity.onRequestSuccess(response.body());
                } else {
                    baseActivity.onRequestFailure(new LoginResponse(), new Throwable(response.raw().message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                DialogManager.getInstance().hideProgress();
                baseActivity.onRequestFailure(new LoginResponse(), t);
            }
        });
    }


    /*Registration API*/
    public void registrationAPICall(String nameStr,  String mobileNoStr,  String deviceIdStr,  String createdDTStr, String userTypeStr, final BaseActivity baseActivity) {
        DialogManager.getInstance().showProgress(baseActivity);
        mServiceInterface.registrationAPI( nameStr,   mobileNoStr,   deviceIdStr,   createdDTStr,  userTypeStr).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                DialogManager.getInstance().hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    baseActivity.onRequestSuccess(response.body());
                } else {
                    baseActivity.onRequestFailure(new LoginResponse(), new Throwable(response.raw().message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                DialogManager.getInstance().hideProgress();
                baseActivity.onRequestFailure(new LoginResponse(), t);
            }
        });
    }


    /*Select Issues API*/
    public void selectIssueListAPICall(  final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.issueListAPI().enqueue(new Callback<IssuesListResponse>() {
            @Override
            public void onResponse(@NonNull Call<IssuesListResponse> call, @NonNull Response<IssuesListResponse> response) {
                DialogManager.getInstance().hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    baseFragment.onRequestSuccess(response.body());
                } else {
                    baseFragment.onRequestFailure(new IssuesListResponse(), new Throwable(response.raw().message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<IssuesListResponse> call, @NonNull Throwable t) {
                DialogManager.getInstance().hideProgress();
                baseFragment.onRequestFailure(new IssuesListResponse(), t);
            }
        });
    }


}


