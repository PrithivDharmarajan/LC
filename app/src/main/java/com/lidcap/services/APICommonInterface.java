package com.lidcap.services;

import com.lidcap.model.input.AddAdvInputEntity;
import com.lidcap.model.input.AdvDeleteInputEntity;
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
import com.lidcap.model.output.CommonModelResponse;
import com.lidcap.model.output.CommonResponse;
import com.lidcap.model.output.IssuesListResponse;
import com.lidcap.model.output.LoginResponse;
import com.lidcap.model.output.NotificationListResponse;
import com.lidcap.model.output.PendingDetailsResponse;
import com.lidcap.model.output.ProviderDetailsResponse;
import com.lidcap.model.output.SelectIssuesTypeResponse;
import com.lidcap.model.output.UploadedResponse;
import com.lidcap.model.output.UserCancelResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APICommonInterface {

    /*Login API*/
    @POST("user/login.php")
    Call<LoginResponse> loginAPI(@Body LoginInputEntity loginInputEntity);

    /*Registration API*/
    @POST("user/register.php")
    Call<LoginResponse> registrationAPI(@Body RegInputEntity regInputEntity);

    /*Select Issue Type API*/
    @POST("user/issues.php")
    Call<SelectIssuesTypeResponse> selectIssueTypeAPI(@Body IssuesInputEntity IssuesInputEntity);

    /*Issue Type API*/
    @POST("user/getbookings.php")
    Call<IssuesListResponse> issueListAPI(@Body IssuesInputEntity IssuesInputEntity);


    /*Issue Type API*/
    @POST("user/notification.php")
    Call<NotificationListResponse> notificationListAPI(@Body IssuesInputEntity IssuesInputEntity);

    /*Lat and Long Update API*/
    @POST("user/updatelocation.php")
    Call<LocationUpdateInputEntity> latAndLongUpdateAPI(@Body LocationUpdateInputEntity locationUpdateInputEntity);

    /*Location API*/
    @POST("user/getproviders.php")
    Call<ProviderDetailsResponse> getProviderLocAPI(@Body LocationUpdateInputEntity locationUpdateInputEntity);

    /*Location API*/
    @POST("user/updateprofile.php")
    Call<LoginResponse> updateProfileAPI(@Body IssuesInputEntity profileInputEntity);

    /*Lat and Long Update API*/
    @POST("user/book.php")
    Call<BookAppointmentInputEntity> bookAppointmentAPI(@Body BookAppointmentInputEntity bookAppointmentInputEntity);


    /*Lat and Long Update API*/
    @POST("user/provideraccept.php")
    Call<AppointmentAcceptResponse> acceptAppointmentAPI(@Body AppointmentAcceptEntity appointmentAcceptEntity);

    /*Lat and Long Update API*/
    @POST("user/providercomplete.php")
    Call<AppointmentAcceptResponse> completeAppointmentAPI(@Body AppointmentAcceptEntity appointmentAcceptEntity);


    /*Lat and Long Update API*/
    @POST("user/usercancel.php")
    Call<UserCancelResponse> userCancelAppointmentAPI(@Body UserCancelEntity userCancelEntity);

    /*Lat and Long Update API*/
    @POST("user/providercancel.php")
    Call<UserCancelResponse> providerCancelAppointmentAPI(@Body UserCancelEntity userCancelEntity);

    /*Location API*/
    @POST("user/background.php")
    Call<PendingDetailsResponse> getUserPendingAppointmentAPI(@Body PendingAppointmentInputEntity pendingAppointmentInputEntity);


    /*Lat and Long Update API*/
    @POST("user/rating.php")
    Call<CommonResponse> userRatingAppointmentAPI(@Body UserRatingInputEntity userCancelEntity);

    /*get Adv Details*/
    @POST("user/adv.php")
    Call<AdvResponse> getUserAdvDetailsAPI(@Body AdvInputEntity advInputEntity);

    @Multipart
    @POST("user/upload.php")
    Call<UploadedResponse> advImageUploadAPI(@Part MultipartBody.Part image);

    /*get Adv Details*/
    @POST("user/addadv.php")
    Call<CommonResponse> addAdvAPI(@Body AddAdvInputEntity addAdvInputEntity);

    /*get Adv Details*/
    @POST("user/deleteadv.php")
    Call<CommonResponse> advDeleteAPI(@Body AdvDeleteInputEntity addAdvInputEntity);


}
