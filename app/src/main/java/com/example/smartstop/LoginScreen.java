package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class LoginScreen extends AppCompatActivity {

    private static int WAIT = 5000;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        progressBar = findViewById(R.id.progressLogin);

    }

    public void back(View view) {
        finish();
    }

    public void login(View view) {

        progressBar.setVisibility(view.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(view.GONE);
                Intent intent = new Intent(LoginScreen.this, MapScreen.class);
                startActivity(intent);
                finish();
            }
        }, WAIT);

    }
}