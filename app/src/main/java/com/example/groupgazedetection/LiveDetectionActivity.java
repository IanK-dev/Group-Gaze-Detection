package com.example.groupgazedetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class LiveDetectionActivity extends CameraActivity implements CvCameraViewListener2 {

    public static final int JAVA_DETECTOR       = 0;
    public static final int NATIVE_DETECTOR     = 1;
    private Mat mRgba;
    private Mat mGray;
    private boolean firstLoop;
    private Mat overlaySize;
    private Bitmap presentOverlay;
    private asyncFaces findFaces;
    public ImageView overlay;
    private float mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize   = 0;
    int frameLimiter = 0;
    private CameraBridgeViewBase mOpenCvCameraView;
    Context currentAppContext;
    liveManager liveManager;
    private Mat grayscaleImage;
    public Rect[] facesArray;
    Rect[][] boxResults = new Rect[2][];

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                    try {
                        liveManager = new liveManager(currentAppContext, "haar");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public LiveDetectionActivity() {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        currentAppContext = this;
        setContentView(R.layout.activity_live_detection);
        firstLoop = true;
        overlay = findViewById(R.id.liveOverlay);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.livedetection_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mGray = inputFrame.gray();
        mRgba = inputFrame.rgba();
        Core.flip(mRgba, mRgba, 1);
        Core.flip(mGray, mGray, 1);
        if (firstLoop == true){
            overlaySize = Mat.zeros(mGray.height(), mGray.width(), CvType.CV_8UC4);
            //overlaySize = Mat.zeros(mGray.size(), CvType.CV_8UC4);
            liveManager.overlay = overlaySize.clone();
            //liveManager.overlaySize = mGray.size();
            presentOverlay = Bitmap.createBitmap(overlaySize.width(), overlaySize.height(), Bitmap.Config.ARGB_8888);
            firstLoop = false;
        }
        if (frameLimiter == 1 ){
            //Detecting face in the frame
            frameLimiter = 0;
            findFaces = new asyncFaces();
            findFaces.execute(mGray);
            return mRgba;
        }
        else{
            frameLimiter++;
        }

        return mRgba;
    }
    private class asyncFaces extends AsyncTask<Mat, String, Bitmap>{
        @Override
        protected Bitmap doInBackground(Mat... mats) {
            overlaySize = liveManager.detect(mats[0]);
            Utils.matToBitmap(overlaySize, presentOverlay);
            return presentOverlay;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            overlay.setImageBitmap(bitmap);
        }
    }
    public void gotoHome(View view){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
    }
}