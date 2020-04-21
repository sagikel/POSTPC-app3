package com.example.postpcapp3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.postpcapp3.dataclass.TokenResponse;
import com.example.postpcapp3.dataclass.UserResponse;
import com.example.postpcapp3.workers.GetInfoWorker;
import com.example.postpcapp3.workers.GetTokenWorker;
import com.example.postpcapp3.workers.PostPrettyName;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText editTextUsername;
    Button buttonEnter;
    String UserName;
    TokenResponse tokenResponse;
    ProgressBar progressBar;
    UserResponse userResponse;
    TextView textView;
    Button buttonEdit;
    ImageView imageView;

    String newPrettyName;
    SharedPreferences sharedPreferences;
    Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        buttonEnter = findViewById(R.id.buttonEnter);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textViewWelcome);
        buttonEdit = findViewById(R.id.buttonEdit);
        imageView = findViewById(R.id.imageView);
        sharedPreferences = getSharedPreferences("SP", MODE_PRIVATE);
        gson = new Gson();

        if (loudToken()) {
            editTextUsername.setVisibility(View.INVISIBLE);
            buttonEnter.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            getUserInfo();
        }

        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectTheUser();
            }
        });
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prettyNameDialog();
            }
        });
    }

    private void connectTheUser() {

        progressBar.setVisibility(View.VISIBLE);
        buttonEnter.setVisibility(View.INVISIBLE);
        UserName = editTextUsername.getText().toString();

        if(TextUtils.isEmpty(UserName)) {
            editTextUsername.setError("Username can't be empty!");
            return;
        }

        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(GetTokenWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("UserName", UserName).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfoList) {
                if (workInfoList == null || workInfoList.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    return;
                }
                if (workInfoList.get(0).getState() == WorkInfo.State.FAILED) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong\ntry again",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (workInfoList.get(0).getState() != WorkInfo.State.SUCCEEDED) {
                    return;
                }
                WorkInfo info = workInfoList.get(0);
                String AsJson = info.getOutputData().getString("TokenResponse");
                tokenResponse = new Gson().fromJson(AsJson, TokenResponse.class);
                saveToken();
                getUserInfo();
            }
        });
    }

    private void getUserInfo() {

        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(GetInfoWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("TOKEN", tokenResponse.data).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onChanged(List<WorkInfo> workInfoList) {
                        if (workInfoList == null || workInfoList.isEmpty()) {
                            return;
                        }
                        if (workInfoList.get(0).getState() == WorkInfo.State.FAILED) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong\ntry again",
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            return;
                        }
                        if (workInfoList.get(0).getState() != WorkInfo.State.SUCCEEDED) {
                            return;
                        }
                        WorkInfo info = workInfoList.get(0);
                        String AsJson = info.getOutputData().getString("UserResponse");
                        userResponse = new Gson().fromJson(AsJson, UserResponse.class);

                        String usernameToShow = userResponse.getData().pretty_name;
                        if (usernameToShow == null || usernameToShow.equals(""))
                            usernameToShow = userResponse.getData().username;

                        editTextUsername.setVisibility(View.INVISIBLE);
                        textView.setText("Welcome\n" + usernameToShow + "!");
                        Picasso.get().load("https://hujipostpc2019.pythonanywhere.com" +
                                userResponse.getData().image_url).into(imageView);

                        progressBar.setVisibility(View.INVISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        buttonEdit.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void prettyNameDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setText(userResponse.getData().pretty_name);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setTitle("Edit pretty name")
                .setView(input)
                .setMessage("Enter name, for deletion leave blank")
                .setIcon(R.drawable.edit_icon)
                .setPositiveButton("set new name", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newPrettyName = input.getText().toString();
                        setPrettyName();
                    }
                })
                .setNegativeButton("back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {} })
                .create()
                .show();
    }

    private void setPrettyName() {
        progressBar.setVisibility(View.VISIBLE);
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(PostPrettyName.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("NAME", newPrettyName)
                        .putString("TOKEN", tokenResponse.data).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onChanged(List<WorkInfo> workInfoList) {
                        if (workInfoList == null || workInfoList.isEmpty()) {
                            return;
                        }
                        if (workInfoList.get(0).getState() == WorkInfo.State.FAILED) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong\ntry again",
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            return;
                        }
                        if (workInfoList.get(0).getState() != WorkInfo.State.SUCCEEDED) {
                            return;
                        }
                        WorkInfo info = workInfoList.get(0);
                        String AsJson = info.getOutputData().getString("UserResponse");
                        userResponse = new Gson().fromJson(AsJson, UserResponse.class);

                        String usernameToShow = userResponse.getData().pretty_name;
                        if (usernameToShow == null || usernameToShow.equals(""))
                            usernameToShow = userResponse.getData().username;

                        textView.setText("Welcome\n" + usernameToShow + "!");
                        progressBar.setVisibility(View.INVISIBLE);
                        Picasso.get().load("https://hujipostpc2019.pythonanywhere.com" +
                                userResponse.getData().image_url).into(imageView);
                    }
                });
    }

    private void saveToken(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(tokenResponse);
        editor.putString("TokenResponse", json);
        editor.apply();
    }

    private boolean loudToken(){
        String json = sharedPreferences.getString("TokenResponse", null);
        if (json == null)
            return false;
        tokenResponse = gson.fromJson(json, TokenResponse.class);
        return true;
    }
}
