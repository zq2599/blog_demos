package com.bolingcavalry.consumer.service;

import com.bolingcavalry.client.HelloResponse;
import org.springframework.cloud.square.retrofit.core.RetrofitClient;
import reactor.core.publisher.Mono;
import retrofit2.http.GET;
import retrofit2.http.Query;

@RetrofitClient("provider")
public interface HelloService {

    @GET("/hello-obj")
    Mono<HelloResponse> hello(@Query("name") String name);
}
