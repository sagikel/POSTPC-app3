package com.example.postpcapp3;

import com.example.postpcapp3.dataclass.SetUserPrettyNameRequest;
import com.example.postpcapp3.dataclass.TokenResponse;
import com.example.postpcapp3.dataclass.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitServerInterface {

    @GET("/users/{user_name}/token")
    Call<TokenResponse> getUsernameToken(@Path("user_name") String user_name);

    @GET("/user/")
    Call<UserResponse> getUserInfo(@Header("Authorization") String token);

    @Headers({
            "Content-Type:application/json"
    })
    @POST("/user/edit/")
    Call<UserResponse> postPrettyName(@Body SetUserPrettyNameRequest request, @Header("Authorization") String token);
}
