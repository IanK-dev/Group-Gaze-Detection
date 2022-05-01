package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class VideoAnalysisActivity extends AppCompatActivity {
    long[] frames = new long[50];
    ArrayList<Mat> convertedFrames = new ArrayList<Mat>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_analysis);
        Bundle extras = getIntent().getExtras();
        frames = extras.getLongArray("listframes");
        int frameIndex = 0;
        while(frames[frameIndex] != 0){
            Mat tempMat = new Mat(frames[frameIndex]);
            convertedFrames.add(tempMat.clone());
            Log.d("LiveDetectionActivity", "Current Index: " + frameIndex);
            Log.d("LiveDetectionActivity", "Current Val: " + frames[frameIndex]);
            frameIndex = frameIndex + 1;
        }

        Log.d("LiveDetectionActivity", "Number of frames: " + convertedFrames.size());
    }
}