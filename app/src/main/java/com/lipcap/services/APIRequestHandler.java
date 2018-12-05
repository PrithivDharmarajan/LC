package com.lipcap.services;


import android.support.annotation.NonNull;

import com.lipcap.main.BaseActivity;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.input.AppointmentAcceptEntity;
import com.lipcap.model.input.BookAppointmentInputEntity;
import com.lipcap.model.input.IssuesInputEntity;
import com.lipcap.model.input.LocationUpdateInputEntity;
import com.lipcap.model.input.LoginInputEntity;
import com.lipcap.model.input.PendingAppointmentInputEntity;
import com.lipcap.model.input.RegInputEntity;
import com.lipcap.model.input.UserCancelEntity;
import com.lipcap.model.input.UserRatingInputEntity;
import com.lipcap.model.output.AppointmentAcceptResponse;
import com.lipcap.model.output.CommonResponse;
import com.lipcap.model.output.IssuesListResponse;
import com.lipcap.model.output.LoginResponse;
import com.lipcap.model.output.PendingDetailsResponse;
import com.lipcap.model.output.ProviderDetailsResponse;
import com.lipcap.model.output.SelectIssuesTypeResponse;
import com.lipcap.model.output.UserCancelResponse;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIRequestHandler {

    private static APIRequestHandler sInstance = new APIRequestHandler();

    /*Init retrofit for API call_provider*/
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
    public void loginAPICall(LoginInputEntity  loginInputEntity, final BaseActivity baseActivity) {
        DialogManager.getInstance().showProgress(baseActivity);
        mServiceInterface.loginAPI(loginInputEntity).enqueue(new Callback<LoginResponse>() {
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
    public void registrationAPICall(RegInputEntity regInputEntity, final BaseActivity baseActivity) {
        DialogManager.getInstance().showProgress(baseActivity);
        mServiceInterface.registrationAPI(regInputEntity).enqueue(new Callback<LoginResponse>() {
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
    public void selectIssueTypeAPICall(IssuesInputEntity issuesInputEntity, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.selectIssueTypeAPI(issuesInputEntity).enqueue(new Callback<SelectIssuesTypeResponse>() {
            @Override
            public void onResponse(@NonNull Call<SelectIssuesTypeResponse> call, @NonNull Response<SelectIssuesTypeResponse> response) {
                DialogManager.getInstance().hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    baseFragment.onRequestSuccess(response.body());
                } else {
                    baseFragment.onRequestFailure(new SelectIssuesTypeResponse(), new Throwable(response.raw().message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<SelectIssuesTypeResponse> call, @NonNull Throwable t) {
                DialogManager.getInstance().hideProgress();
                baseFragment.onRequestFailure(new SelectIssuesTypeResponse(), t);
            }
        });
    }

    /*Issues API*/
    public void issueListAPICall(IssuesInputEntity issuesInputEntity,final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.issueListAPI(issuesInputEntity)
                .enqueue(new Callback<IssuesListResponse>() {
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

    /*Update Lat and Long API*/
    public void latAndLongUpdateAPICall(LocationUpdateInputEntity locationUpdateInputEntity, final BaseFragment baseFragment) {
        mServiceInterface.latAndLongUpdateAPI(locationUpdateInputEntity)
                .enqueue(new Callback<LocationUpdateInputEntity>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationUpdateInputEntity> call, @NonNull Response<LocationUpdateInputEntity> response) {
                         if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LocationUpdateInputEntity> call, @NonNull Throwable t) {
                    }
                });
    }

    /*update provider location API*/
    public void getProviderLocAPICall(LocationUpdateInputEntity locationUpdateInputEntity, final BaseFragment baseFragment) {
        mServiceInterface.getProviderLocAPI(locationUpdateInputEntity)
                .enqueue(new Callback<ProviderDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProviderDetailsResponse> call, @NonNull Response<ProviderDetailsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProviderDetailsResponse> call, @NonNull Throwable t) {
                    }
                });
    }

    /*Update user profile API*/
    public void updateProfileAPICall(IssuesInputEntity profileInputEntity, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.updateProfileAPI(profileInputEntity)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        DialogManager.getInstance().hideProgress();

                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new CommonResponse(), t);
                    }
                });
    }

    /*Book appointment API*/
    public void bookAppointmentAPICall(BookAppointmentInputEntity bookAppointmentInputEntity, final BaseFragment baseFragment) {
        mServiceInterface.bookAppointmentAPI(  bookAppointmentInputEntity)
                .enqueue(new Callback<BookAppointmentInputEntity>() {
                    @Override
                    public void onResponse(@NonNull Call<BookAppointmentInputEntity> call, @NonNull Response<BookAppointmentInputEntity> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookAppointmentInputEntity> call, @NonNull Throwable t) {
                        baseFragment.onRequestFailure(new BookAppointmentInputEntity(), t);
                    }
                });
    }

    /*Get pending appointment details API*/
    public void getUserPendingAppointmentAPICall(PendingAppointmentInputEntity pendingAppointmentInputEntity, final BaseFragment baseFragment) {
        mServiceInterface.getUserPendingAppointmentAPI(pendingAppointmentInputEntity)
                .enqueue(new Callback<PendingDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PendingDetailsResponse> call, @NonNull Response<PendingDetailsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PendingDetailsResponse> call, @NonNull Throwable t) {
                    }
                });
    }

    /*accept Appointment API*/
    public void acceptAppointmentAPICall(AppointmentAcceptEntity appointmentAcceptEntity, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.acceptAppointmentAPI(appointmentAcceptEntity)
                .enqueue(new Callback<AppointmentAcceptResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AppointmentAcceptResponse> call, @NonNull Response<AppointmentAcceptResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new AppointmentAcceptResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AppointmentAcceptResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new AppointmentAcceptResponse(), t);
                    }
                });
    }

    /*accept Appointment API*/
    public void completeAppointmentAPICall(AppointmentAcceptEntity appointmentAcceptEntity, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.completeAppointmentAPI(appointmentAcceptEntity)
                .enqueue(new Callback<AppointmentAcceptResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AppointmentAcceptResponse> call, @NonNull Response<AppointmentAcceptResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new AppointmentAcceptResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AppointmentAcceptResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new AppointmentAcceptResponse(), t);
                    }
                });
    }


    /*User Cancel API*/
    public void userCancelAppointmentAPICall(UserCancelEntity userCancelEntity,final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.userCancelAppointmentAPI(userCancelEntity)
                .enqueue(new Callback<UserCancelResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserCancelResponse> call, @NonNull Response<UserCancelResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new UserCancelResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserCancelResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new UserCancelResponse(), t);
                    }
                });
    }

    /*Provider Cancel API*/
    public void providerCancelAppointmentAPICall(UserCancelEntity userCancelEntity,final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.providerCancelAppointmentAPI(userCancelEntity)
                .enqueue(new Callback<UserCancelResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserCancelResponse> call, @NonNull Response<UserCancelResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new UserCancelResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserCancelResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new UserCancelResponse(), t);
                    }
                });
    }
    /*Issues API*/
    public void userRateAppointmentAPICall(UserRatingInputEntity userRatingInputEntity, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.userRatingAppointmentAPI(userRatingInputEntity)
                .enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new CommonResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new CommonResponse(), t);
                    }
                });
    }

}


