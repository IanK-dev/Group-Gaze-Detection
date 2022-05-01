package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;

public class VideoAnalysisActivity extends AppCompatActivity {
    long[] frames = new long[50];
    ArrayList<Bitmap> convertedFrames = new ArrayList<Bitmap>();
    ImageView frameDisplay;
    cvManager openManager;
    Context currentAppContext;
    private int currentImageIndex;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                try {
                    openManager = new cvManager(currentAppContext, "haar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                super.onManagerConnected(status);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_analysis);
        currentAppContext = this;
        Bundle extras = getIntent().getExtras();
        frames = extras.getLongArray("listframes");
        int frameIndex = 0;
        currentImageIndex = 0;
        Mat refMat = new Mat(frames[0]);
        Bitmap referenceMap = Bitmap.createBitmap(refMat.width(), refMat.height(), Bitmap.Config.ARGB_8888);
        while(frames[frameIndex] != 0){
            Mat tempMat = new Mat(frames[frameIndex]);
            Bitmap iterMap = referenceMap.copy(referenceMap.getConfig(), true);
            Utils.matToBitmap(tempMat, iterMap);
            convertedFrames.add(iterMap);
            Log.d("LiveDetectionActivity", "Current Index: " + frameIndex);
            Log.d("LiveDetectionActivity", "Current Val: " + frames[frameIndex]);
            frameIndex = frameIndex + 1;
        }
        Log.d("LiveDetectionActivity", "Number of frames: " + convertedFrames.size());
        frameDisplay = findViewById(R.id.capturedFrameView);
        frameDisplay.setImageBitmap(convertedFrames.get(currentImageIndex));
        frameDisplay.setOnTouchListener(new swipeListener(this) {
            public void onSwipeRight() {
                if(currentImageIndex < convertedFrames.size() - 1){
                    currentImageIndex = currentImageIndex + 1;
                    frameDisplay.setImageBitmap(convertedFrames.get(currentImageIndex));
                }

                Log.d("LiveDetectionActivity", "Swiped right");
            }
            public void onSwipeLeft() {
                if(currentImageIndex > 0){
                    currentImageIndex = currentImageIndex - 1;
                    frameDisplay.setImageBitmap(convertedFrames.get(currentImageIndex));
                }
                Log.d("LiveDetectionActivity", "Swiped left");
            }
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    public void returnToLive(View view) {
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }
}