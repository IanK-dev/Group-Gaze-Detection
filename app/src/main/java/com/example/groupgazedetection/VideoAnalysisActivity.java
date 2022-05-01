package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;

public class VideoAnalysisActivity extends AppCompatActivity {
    long[] frames = new long[50];
    ArrayList<Mat> convertedFrames = new ArrayList<Mat>();
    ImageView frameDisplay;
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
        frameDisplay = findViewById(R.id.capturedFrameView);
        Bitmap thisMap = Bitmap.createBitmap(convertedFrames.get(0).width(), convertedFrames.get(0).height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(convertedFrames.get(0), thisMap);
        frameDisplay.setImageBitmap(thisMap);
        frameDisplay.setOnTouchListener(new swipeListener(this) {
            public void onSwipeRight() {
                Log.d("LiveDetectionActivity", "Swiped right");
            }
            public void onSwipeLeft() {
                Log.d("LiveDetectionActivity", "Swiped left");
            }
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });


    }
}