package com.lipcap.services;

import com.lipcap.model.output.IssuesListResponse;
import com.lipcap.model.output.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APICommonInterface {

    /*Login API*/
    @GET("User/getUser?")
    Call<LoginResponse> loginAPI(@Query("MobileNo") String mobileNoStr, @Query("DeviceId") String deviceIdStr);

    /*Registration API*/
    @FormUrlEncoded
    @POST("User/postUser")
    Call<LoginResponse> registrationAPI(@Field("Name")String nameStr,@Field("MobileNo")String mobileNoStr,@Field("DeviceId")String deviceIdStr,@Field("CreatedDT")String createdDTStr,@Field("UserType")String userTypeStr);

    /*Registration API*/
    @GET("Issue/GetIssueType")
    Call<IssuesListResponse> issueListAPI();
}
