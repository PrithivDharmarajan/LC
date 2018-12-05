package com.lipcap.services;

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

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APICommonInterface {

    /*Login API*/
    @POST("user/login")
    Call<LoginResponse> loginAPI(@Body LoginInputEntity loginInputEntity);

    /*Registration API*/
    @POST("user/register")
    Call<LoginResponse> registrationAPI(@Body RegInputEntity regInputEntity);

    /*Select Issue Type API*/
    @POST("user/issues")
    Call<SelectIssuesTypeResponse> selectIssueTypeAPI(@Body IssuesInputEntity IssuesInputEntity);

    /*Issue Type API*/
    @POST("user/getbookings")
    Call<IssuesListResponse> issueListAPI(@Body IssuesInputEntity IssuesInputEntity);

    /*Lat and Long Update API*/
    @POST("user/updatelocation")
    Call<LocationUpdateInputEntity> latAndLongUpdateAPI(@Body LocationUpdateInputEntity locationUpdateInputEntity);

    /*Location API*/
    @POST("user/getproviders")
    Call<ProviderDetailsResponse> getProviderLocAPI(@Body LocationUpdateInputEntity locationUpdateInputEntity);

    /*Location API*/
    @POST("user/updateprofile")
    Call<LoginResponse> updateProfileAPI(@Body IssuesInputEntity profileInputEntity);

    /*Lat and Long Update API*/
    @POST("user/book")
    Call<BookAppointmentInputEntity> bookAppointmentAPI(@Body BookAppointmentInputEntity bookAppointmentInputEntity);


    /*Lat and Long Update API*/
    @POST("user/provideraccept")
    Call<AppointmentAcceptResponse> acceptAppointmentAPI(@Body AppointmentAcceptEntity appointmentAcceptEntity);

    /*Lat and Long Update API*/
    @POST("user/providercomplete")
    Call<AppointmentAcceptResponse> completeAppointmentAPI(@Body AppointmentAcceptEntity appointmentAcceptEntity);


    /*Lat and Long Update API*/
    @POST("user/usercancel")
    Call<UserCancelResponse> userCancelAppointmentAPI(@Body UserCancelEntity userCancelEntity);

    /*Lat and Long Update API*/
    @POST("user/providercancel")
    Call<UserCancelResponse> providerCancelAppointmentAPI(@Body UserCancelEntity userCancelEntity);

    /*Location API*/
    @POST("user/background")
    Call<PendingDetailsResponse> getUserPendingAppointmentAPI(@Body PendingAppointmentInputEntity pendingAppointmentInputEntity);


    /*Lat and Long Update API*/
    @POST("user/rating")
    Call<CommonResponse> userRatingAppointmentAPI(@Body UserRatingInputEntity userCancelEntity);


}
