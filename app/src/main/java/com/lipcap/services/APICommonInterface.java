package com.lipcap.services;

import com.lipcap.model.output.IssuesListResponse;
import com.lipcap.model.output.LoginResponse;
import com.lipcap.model.output.UserDetailsEntity;

import retrofit2.Call;
import retrofit2.http.Body;
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
    Call<String> registrationAPI(@Body UserDetailsEntity userDetailsEntity);

    /*Registration API*/
    @GET("Issue/GetIssueType")
    Call<IssuesListResponse> issueListAPI();
}
