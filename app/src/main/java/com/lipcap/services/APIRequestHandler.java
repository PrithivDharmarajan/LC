package com.lipcap.services;



public class APIRequestHandler {

    private static APIRequestHandler sInstance = new APIRequestHandler();

    /*Init retrofit for API call*/
//    private APICommonInterface mServiceInterface = serviceInterface();

    public static APIRequestHandler getInstance() {
        return sInstance;
    }

//    private APICommonInterface serviceInterface() {
//        return new Retrofit.Builder().baseUrl(AppConstants.BASE_URL).client(getUnsafeOkHttpClient())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build().create(APICommonInterface.class);
//    }



}


