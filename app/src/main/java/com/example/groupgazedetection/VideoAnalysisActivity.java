package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoAnalysisActivity extends AppCompatActivity {
    long[] frames = new long[50];
    ArrayList<Mat> recievedMats = new ArrayList<Mat>();
    ArrayList<Bitmap> convertedFrames = new ArrayList<Bitmap>();
    ArrayList<List<detectedFace>> faceInformation = new ArrayList<List<detectedFace>>();
    ImageView frameDisplay;
    TextView frameNumber;
    cvManager openManager;
    Context currentAppContext;
    private int currentImageIndex;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                try {
                    Log.d("VideoAnalysisActivity", "BaseLoaderSuccess");
                    openManager = new cvManager(currentAppContext, "haar");
                    Log.d("VideoAnalysisActivity", "Size of Input: " + convertedFrames.size());
                    new analyzeFrame().execute(convertedFrames);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_analysis);
        currentAppContext = this;
        frameNumber = findViewById(R.id.frameNumber);
        /*
        Bundle extras = getIntent().getExtras();
        frames = extras.getLongArray("listframes");
        int frameIndex = 0;
        currentImageIndex = 0;

        Mat refMat = new Mat(frames[0]);

        while(frames[frameIndex] != 0){
            Mat tempMat = new Mat(frames[frameIndex]);
            Bitmap iterMap = Bitmap.createBitmap(refMat.width(), refMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tempMat, iterMap);
            convertedFrames.add(iterMap);
            Log.d("LiveDetectionActivity", "Current Index: " + frameIndex);
            Log.d("LiveDetectionActivity", "Current Val: " + frames[frameIndex]);
            frameIndex = frameIndex + 1;
            //tempMat.release();
        }
        System.gc();
        */
        globalStorage getFaces = (globalStorage) getApplication();
        recievedMats = getFaces.compareFrames;
        Mat refMat = recievedMats.get(0);
        for(Mat matInstance : recievedMats){
            Bitmap refMap = Bitmap.createBitmap(refMat.width(), refMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matInstance, refMap);
            convertedFrames.add(refMap);
        }
        Log.d("LiveDetectionActivity", "Number of frames: " + convertedFrames.size());
        frameDisplay = findViewById(R.id.capturedFrameView);
        frameDisplay.setImageBitmap(convertedFrames.get(currentImageIndex));
        frameDisplay.setOnTouchListener(new swipeListener(this) {
            public void onSwipeRight() {
                if(currentImageIndex > 0){
                    currentImageIndex = currentImageIndex - 1;
                    frameDisplay.setImageBitmap(convertedFrames.get(currentImageIndex));
                    frameNumber.setText("Frame:" + (currentImageIndex + 1));
                }
                Log.d("LiveDetectionActivity", "Swiped right");
            }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeLeft() {
                if(currentImageIndex < convertedFrames.size() - 1){
                    currentImageIndex = currentImageIndex + 1;
                    frameDisplay.setImageBitmap(convertedFrames.get(currentImageIndex));
                    frameNumber.setText("Frame:" + (currentImageIndex + 1));
                }
                Log.d("LiveDetectionActivity", "Swiped left");
            }
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public class analyzeFrame extends AsyncTask<ArrayList<Bitmap>, Void, ArrayList<Bitmap>> {
        @Override
        protected  void onPreExecute(){
            Log.d("VideoAnalysisActivity", "Beginning Detection");
        }
        @Override
        protected ArrayList<Bitmap> doInBackground(ArrayList<Bitmap>... bitmaps) {
            //ArrayList<Bitmap> returnMaps = new ArrayList<Bitmap>();
            Log.d("VideoAnalysisActivity", "Size of Input: " + bitmaps[0].size());
            ArrayList<Bitmap> inputBitmaps = bitmaps[0];
            for(int i = 0; i < inputBitmaps.size(); i++){
                Log.d("VideoAnalysisActivity", "Looping Detection: " + i);
                inputBitmaps.set(i, openManager.detect(inputBitmaps.get(i)));
                faceInformation.add(openManager.detectedFaces);
            }
            return inputBitmaps;
        }
        @Override
        protected void onPostExecute(ArrayList<Bitmap> inputBitmaps) {
            convertedFrames = inputBitmaps;
            Log.d("LiveDetectionActivity", "Completed Detection");
        }
    }

    public void viewStats(View view){
        frameDisplay.setVisibility(View.GONE);
        frameNumber.setVisibility(View.GONE);

    }

    public void returnToLive(View view) {
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }
}