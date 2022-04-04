package com.example.social;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LaunchScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        getSupportActionBar().hide();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LaunchScreen.this, Login.class));
            }
        }, 5000);    }
}