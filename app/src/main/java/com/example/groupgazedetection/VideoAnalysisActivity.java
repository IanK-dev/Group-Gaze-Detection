package com.example.groupgazedetection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoAnalysisActivity extends AppCompatActivity {
    long[] frames = new long[50];
    ArrayList<Mat> recievedMats = new ArrayList<Mat>();
    ArrayList<Bitmap> convertedFrames = new ArrayList<Bitmap>();
    ArrayList<List<detectedFace>> faceInformation = new ArrayList<List<detectedFace>>();
    ImageView frameDisplay;
    TextView frameNumber; TextView totalFrames;
    TextView leftEyeX; TextView leftEyeY;
    TextView leftEyeXRes; TextView leftEyeYRes;
    TextView rightEyeX; TextView rightEyeY;
    TextView rightEyeXRes; TextView rightEyeYRes;
    TextView processingText; CircularProgressIndicator progInd;
    ImageView resultBackground; ImageView framesBackground;
    Button gazeStats; Button frameInspector;
    //private int[] rightXEyeQuantity; private int[] leftXEyeQuantity;
    //private int[] rightYEyeQuantity; private int[] leftYEyeQuantity;
    double[] rightXEyeSums; double[] rightYEyeSums;
    double[] leftXEyeSums; double[] leftYEyeSums;
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
        //ID initialization
        frameNumber = findViewById(R.id.frameNumber); totalFrames = findViewById(R.id.totalFrames);
        leftEyeX = findViewById(R.id.leftEyeX); leftEyeY = findViewById(R.id.leftEyeY);
        leftEyeXRes = findViewById(R.id.leftEyeXRes); leftEyeYRes = findViewById(R.id.leftEyeYRes);
        rightEyeX = findViewById(R.id.rightEyeX); rightEyeY = findViewById(R.id.rightEyeY);
        rightEyeXRes = findViewById(R.id.rightEyeXRes); rightEyeYRes = findViewById(R.id.rightEyeYRes);
        gazeStats = findViewById(R.id.gazeDirectionStats); frameInspector = findViewById(R.id.returnToImages);
        processingText = findViewById(R.id.processingText); progInd = findViewById(R.id.processingLivePhoto);
        resultBackground = findViewById(R.id.videoResults); framesBackground = findViewById(R.id.videoAnalFramesGUI);
        //Visibility Declaration
        totalFrames.setVisibility(View.GONE); frameInspector.setVisibility(View.GONE);
        leftEyeX.setVisibility(View.GONE); leftEyeY.setVisibility(View.GONE);
        leftEyeXRes.setVisibility(View.GONE); leftEyeYRes.setVisibility(View.GONE);
        rightEyeX.setVisibility(View.GONE); rightEyeY.setVisibility(View.GONE);
        rightEyeXRes.setVisibility(View.GONE); rightEyeYRes.setVisibility(View.GONE);
        processingText.setVisibility(View.GONE); progInd.setVisibility(View.GONE);
        resultBackground.setVisibility(View.GONE);
        rightXEyeSums = new double[3]; rightYEyeSums = new double[3];
        leftXEyeSums= new double[3]; leftYEyeSums= new double[3];
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
            processingText.setVisibility(View.VISIBLE); progInd.setVisibility(View.VISIBLE);
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
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(ArrayList<Bitmap> inputBitmaps) {
            convertedFrames = inputBitmaps;
            for(List<detectedFace> faceList : faceInformation){
                for(detectedFace face : faceList){
                    //LEFT EYE HORIZONTAL RESULTS
                    for(int i = 0; i < 3; i++){
                        rightXEyeSums[i] = rightXEyeSums[i] + face.rightEyeVals[i];
                        leftXEyeSums[i] = leftXEyeSums[i] + face.leftEyeVals[i];
                    }
                    for(int i = 3; i < 6; i++){
                        rightYEyeSums[i - 3] = rightYEyeSums[i - 3]+ face.rightEyeVals[i];
                        leftYEyeSums[i - 3] = leftYEyeSums[i - 3] + face.leftEyeVals[i];
                    }
                }
            }
            Log.d("VideoAnalysisActivity", "Left Eye Direction Vals: " + leftXEyeSums[1] + ", " + leftXEyeSums[2] + ", " + leftYEyeSums[1] + ", " + leftYEyeSums[2]);
            Log.d("VideoAnalysisActivity", "Right Eye Direction Vals: " + rightXEyeSums[1] + ", " + rightXEyeSums[2] + ", " + rightYEyeSums[1] + ", " + rightYEyeSums[2]);
            double labelSum = Arrays.stream(rightXEyeSums).sum();
            String rightXLabel = "Left: " + (rightXEyeSums[0]/labelSum)*100 + "%\nRight: " + (rightXEyeSums[1]/labelSum) + "%\nForward: " + (rightXEyeSums[2]/labelSum)*100 + "%";
            rightEyeXRes.setText(rightXLabel);
            labelSum = Arrays.stream(rightYEyeSums).sum();
            String rightYLabel = "Left: " + (rightYEyeSums[0]/labelSum)*100 + "%\nRight: " + (rightYEyeSums[1]/labelSum) + "%\nCenter: " + (rightYEyeSums[2]/labelSum)*100 + "%";
            rightEyeYRes.setText(rightYLabel);
            labelSum = Arrays.stream(leftXEyeSums).sum();
            String leftXLabel = "Left: " + (leftXEyeSums[0]/labelSum)*100 + "%\nRight: " + (leftXEyeSums[1]/labelSum) + "%\nForward: " + (leftXEyeSums[2]/labelSum)*100 + "%";
            leftEyeXRes.setText(leftXLabel);
            String leftYLabel = "Left: " + (leftYEyeSums[0]/labelSum)*100 + "%\nRight: " + (leftYEyeSums[1]/labelSum) + "%\nCenter: " + (leftYEyeSums[2]/labelSum)*100 + "%";
            leftEyeYRes.setText(leftYLabel);
            processingText.setText("Processing Complete"); progInd.setVisibility(View.GONE);
            totalFrames.setText("Total Frames: " + recievedMats.size());
            Log.d("LiveDetectionActivity", "Completed Detection");
        }
    }

    public void viewStats(View view){
        frameDisplay.setVisibility(View.GONE); frameNumber.setVisibility(View.GONE); gazeStats.setVisibility(View.GONE);
        totalFrames.setVisibility(View.VISIBLE); frameInspector.setVisibility(View.VISIBLE);
        leftEyeX.setVisibility(View.VISIBLE); leftEyeY.setVisibility(View.VISIBLE);
        leftEyeXRes.setVisibility(View.VISIBLE); leftEyeYRes.setVisibility(View.VISIBLE);
        rightEyeX.setVisibility(View.VISIBLE); rightEyeY.setVisibility(View.VISIBLE);
        rightEyeXRes.setVisibility(View.VISIBLE); rightEyeYRes.setVisibility(View.VISIBLE);
        resultBackground.setVisibility(View.VISIBLE); framesBackground.setVisibility(View.GONE);
    }

    public void viewFrames(View view){
        frameDisplay.setVisibility(View.VISIBLE); frameNumber.setVisibility(View.VISIBLE); gazeStats.setVisibility(View.VISIBLE);
        totalFrames.setVisibility(View.GONE); frameInspector.setVisibility(View.GONE);
        leftEyeX.setVisibility(View.GONE); leftEyeY.setVisibility(View.GONE);
        leftEyeXRes.setVisibility(View.GONE); leftEyeYRes.setVisibility(View.GONE);
        rightEyeX.setVisibility(View.GONE); rightEyeY.setVisibility(View.GONE);
        rightEyeXRes.setVisibility(View.GONE); rightEyeYRes.setVisibility(View.GONE);
        resultBackground.setVisibility(View.GONE); framesBackground.setVisibility(View.VISIBLE);
    }

    public void returnToLive(View view) {
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }
}