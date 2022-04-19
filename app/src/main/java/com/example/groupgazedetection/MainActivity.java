package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler delayHandler = new Handler();
        Intent goHome = new Intent(this, HomeActivity.class);
        delayHandler.postDelayed(new Runnable(){
                public void run(){
                    startActivity(goHome);
                }
        }, 3000);
    }
}