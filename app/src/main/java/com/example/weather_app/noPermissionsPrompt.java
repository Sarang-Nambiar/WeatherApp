package com.example.weather_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class noPermissionsPrompt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_permissions_prompt);
    }
}