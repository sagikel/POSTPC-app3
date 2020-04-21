package com.example.postpcapp3.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.postpcapp3.RetrofitServerInterface;
import com.example.postpcapp3.ServerSingleton;
import com.example.postpcapp3.dataclass.SetUserPrettyNameRequest;
import com.example.postpcapp3.dataclass.TokenResponse;
import com.example.postpcapp3.dataclass.UserResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;

public class PostPrettyName extends Worker {
    public PostPrettyName(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        RetrofitServerInterface retrofitServerInterface = ServerSingleton.getInstance().retrofitServerInterface;
        String newPrettyName = getInputData().getString("NAME");
        String token = "token " + getInputData().getString("TOKEN");
        SetUserPrettyNameRequest setUserPrettyNameRequest = new SetUserPrettyNameRequest();
        setUserPrettyNameRequest.pretty_name = newPrettyName;
        try {
            Response<UserResponse> response = retrofitServerInterface
                    .postPrettyName(setUserPrettyNameRequest, token).execute();

            UserResponse userResponse = response.body();
            String toJson = new Gson().toJson(userResponse);

            Data outputData = new Data.Builder()
                    .putString("UserResponse", toJson)
                    .build();

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
