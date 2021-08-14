package com.bolingcavalry.consumer.service;

import com.bolingcavalry.client.HelloResponse;
import org.springframework.cloud.square.retrofit.core.RetrofitClient;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

@RetrofitClient("provider")
public interface HelloService {

    @GET("/hello-obj")
    Call<HelloResponse> hello(@Query("name") String name);
}
