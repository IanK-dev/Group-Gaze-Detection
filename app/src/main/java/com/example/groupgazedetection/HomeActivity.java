package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);
        //ActionBar thisAction = getActionBar();
        //thisAction.hide();
    }

    public void gotoLiveDetection(View view){
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }

    public void gotoSingleImage(View view){
        Intent intent = new Intent(this, SingleImageActivity.class);
        startActivity(intent);
    }

    public void gotoMultiImage(View view){
        Intent intent = new Intent(this, MultiImageActivity.class);
        startActivity(intent);
    }

    public void gotoSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}