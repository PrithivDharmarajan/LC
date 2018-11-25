package com.lipcap.services;

import com.lipcap.model.output.CommonResponse;
import com.lipcap.model.output.IssuesListResponse;
import com.lipcap.model.output.LoginResponse;
import com.lipcap.model.output.SelectIssuesTypeResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface APICommonInterface {

    /*Login API*/
    @GET("User/getUser?")
    Call<LoginResponse> loginAPI(@Query("MobileNo") String mobileNoStr, @Query("DeviceId") String deviceIdStr);

    /*Registration API*/
    @FormUrlEncoded
    @POST("User/postUser")
    Call<LoginResponse> registrationAPI(@Field("Name")String nameStr,@Field("MobileNo")String mobileNoStr,@Field("DeviceId")String deviceIdStr,@Field("CreatedDT")String createdDTStr,@Field("UserType")String userTypeStr);

    /*Select Issue Type API*/
    @GET("Issue/GetIssueType")
    Call<SelectIssuesTypeResponse> selectIssueTypeAPI();

    /*Issue Type API*/
    @GET("Appointment/getIssueByUserId?")
    Call<IssuesListResponse> issueListAPI(@Query("UserId") String userIdStr);

    /*Lat and Long Update API*/
    @FormUrlEncoded
    @PUT("latilongi/putLatiLongibyId")
    Call<CommonResponse> latAndLongUpdateAPI(@Field("userId")String userIdStr, @Field("Latitude")String latitudeStr, @Field("Longitude")String longitudeStr);

    /*Location API*/
    @GET("latilongi/getLatiLongi?")
    Call<CommonResponse> getProviderLocAPI(@Query("Latitude") String latitudeStr, @Query("Longitude") String longitudeStr);


}
