package com.example.chatapp.Fragments;

import com.example.chatapp.Notifications.MyResponse;
import com.example.chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2G6SGWU:APA91bFs4uzwC3YvK24JAKXqnm41p996aQsf8pEJR6DPTdCp6c5FGYEMOm7gsosCyKhxPr8aUr820qexKJQ2euE0K0c3rPtugfp1kuW90fNxJKPgT3VsGX6spe_QrEXyqLX-L-RKTFk7"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
