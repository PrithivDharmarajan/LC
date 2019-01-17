package com.lidcap.services;


import android.support.annotation.NonNull;

import com.lidcap.main.BaseActivity;
import com.lidcap.main.BaseFragment;
import com.lidcap.model.input.AddAdvInputEntity;
import com.lidcap.model.input.AdvInputEntity;
import com.lidcap.model.input.AppointmentAcceptEntity;
import com.lidcap.model.input.BookAppointmentInputEntity;
import com.lidcap.model.input.IssuesInputEntity;
import com.lidcap.model.input.LocationUpdateInputEntity;
import com.lidcap.model.input.LoginInputEntity;
import com.lidcap.model.input.PendingAppointmentInputEntity;
import com.lidcap.model.input.RegInputEntity;
import com.lidcap.model.input.UserCancelEntity;
import com.lidcap.model.input.UserRatingInputEntity;
import com.lidcap.model.output.AdvResponse;
import com.lidcap.model.output.AppointmentAcceptResponse;
import com.lidcap.model.output.CommonResponse;
import com.lidcap.model.output.IssuesListResponse;
import com.lidcap.model.output.LoginResponse;
import com.lidcap.model.output.NotificationListResponse;
import com.lidcap.model.output.PendingDetailsResponse;
import com.lidcap.model.output.ProviderDetailsResponse;
import com.lidcap.model.output.SelectIssuesTypeResponse;
import com.lidcap.model.output.UploadedResponse;
import com.lidcap.model.output.UserCancelResponse;
import com.lidcap.utils.AppConstants;
import com.lidcap.utils.DialogManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(240, TimeUnit.SECONDS)
                .connectTimeout(240, TimeUnit.SECONDS)
                .build();
        return new Retrofit.Builder().baseUrl(AppConstants.BASE_URL)
                .client(okHttpClient)
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


    /*Notification List API*/
    public void notificationListAPICall(IssuesInputEntity issuesInputEntity,final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.notificationListAPI(issuesInputEntity)
                .enqueue(new Callback<NotificationListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NotificationListResponse> call, @NonNull Response<NotificationListResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new NotificationListResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NotificationListResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new NotificationListResponse(), t);
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


    /*UserAdvList API*/
    public void getUserAdvListAPICall(AdvInputEntity userRatingInputEntity, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        mServiceInterface.getUserAdvDetailsAPI(userRatingInputEntity)
                .enqueue(new Callback<AdvResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AdvResponse> call, @NonNull Response<AdvResponse> response) {
                        DialogManager.getInstance().hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            baseFragment.onRequestSuccess(response.body());
                        } else {
                            baseFragment.onRequestFailure(new CommonResponse(), new Throwable(response.raw().message()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AdvResponse> call, @NonNull Throwable t) {
                        DialogManager.getInstance().hideProgress();
                        baseFragment.onRequestFailure(new CommonResponse(), t);
                    }
                });
    }

    /*Profile Image Upload Api Call*/
    public void profileImageUploadApiCall(final AddAdvInputEntity addAdvInputEntity, String imageString, final BaseFragment baseFragment) {
        DialogManager.getInstance().showProgress(baseFragment.getActivity());
        File file = new File(imageString);

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), file);

        MultipartBody.Part imageFile = MultipartBody.Part.createFormData("image", file.getName(), imageBody);

        mServiceInterface.advImageUploadAPI ( imageFile).enqueue(new Callback<UploadedResponse>() {
            @Override
            public void onResponse(@NonNull Call<UploadedResponse> call, @NonNull Response<UploadedResponse> response) {
                 if (response.isSuccessful() && response.body() != null) {
                     UploadedResponse advResponse =  response.body();
                     if(advResponse.getStatusCode().equalsIgnoreCase(AppConstants.SUCCESS_CODE)){
                         addAdvInputEntity.setUrl(advResponse.getResult().getUrl());
                         addAdAPICall(addAdvInputEntity,baseFragment);
                     }else{
                         DialogManager.getInstance().hideProgress();
                     }
                } else {
                     DialogManager.getInstance().hideProgress();
                     baseFragment.onRequestFailure(new UploadedResponse(),new Throwable(response.raw().message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UploadedResponse> call, @NonNull Throwable t) {
                DialogManager.getInstance().hideProgress();
                baseFragment.onRequestFailure(new UploadedResponse(), t);
            }
        });
    }


    /*Issues API*/
    private void addAdAPICall(AddAdvInputEntity addAdvInputEntity, final BaseFragment baseFragment) {
        mServiceInterface.addAdvAPI(addAdvInputEntity)
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


