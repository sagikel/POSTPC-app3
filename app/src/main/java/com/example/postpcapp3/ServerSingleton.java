package com.example.postpcapp3;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerSingleton {
    private static ServerSingleton instance = null;
    public final RetrofitServerInterface retrofitServerInterface;

    private ServerSingleton(RetrofitServerInterface retrofitServerInterface) {
        this.retrofitServerInterface = retrofitServerInterface;
    }

    public synchronized static ServerSingleton getInstance() {
        if (instance != null)
            return instance;

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://hujipostpc2019.pythonanywhere.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitServerInterface serverInterface = retrofit.create(RetrofitServerInterface.class);
        instance = new ServerSingleton(serverInterface);
        return instance;
    }
}
