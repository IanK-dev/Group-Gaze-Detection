package com.example.groupgazedetection;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Build;
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
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.ToggleButton;
import androidx.annotation.RequiresApi;

public class LiveDetectionActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "LiveDetection";
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
    private FaceCameraView faceCameraView;
    Context currentAppContext;
    liveManager liveManager;
    private Mat grayscaleImage;

    private ToggleButton toggleFlashLightOnOff;
    private CameraManager cameraManager;
    private String getCameraID;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    faceCameraView.enableView();
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
        faceCameraView = (FaceCameraView) findViewById(R.id.livedetection_surface_view);
        faceCameraView.setVisibility(FaceCameraView.VISIBLE);
        faceCameraView.setCameraIndex(1);
        faceCameraView.setCvCameraViewListener(this);

        toggleFlashLightOnOff = findViewById(R.id.toggleFlashButton);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (faceCameraView != null)
            faceCameraView.disableView();
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
        return Collections.singletonList(faceCameraView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onDestroy() {
        super.onDestroy();
        faceCameraView.disableView();
        try {
            cameraManager.setTorchMode(getCameraID, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void onCameraViewStarted(int width, int height) {
        //faceCameraView.setFocusMode("FOCUS_MODE_AUTO");
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

    public void gotoSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void activateFlash(View view){

        if(toggleFlashLightOnOff.isChecked()) {
            try {
                cameraManager.setTorchMode(getCameraID, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                cameraManager.setTorchMode(getCameraID, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        //Log.d(TAG, faceCameraView.getFlashMode());
        //faceCameraView.setFlashMode("on");
        /*for(String res : faceCameraView.getFocusModes()){
            Log.d(TAG, res);
        }*/
        //Log.d(TAG, faceCameraView.getFlashMode());

    }

    public void takePicture(View v) {
        //.i(TAG,"onTouch event");
        String fileName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            //Later add code to make a new directory if one does not exist.
            //Log.d(TAG, getExternalFilesDir(Environment.DIRECTORY_DCIM).toString());
            //fileName = this.getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() + "/sample_picture_" + currentDateandTime + ".jpg";
            fileName = "/storage/emulated/0/DCIM/sample_picture_" + currentDateandTime + ".jpg";
        }
        else
        {
            fileName = "/storage/emulated/0/DCIM/sample_picture_" + currentDateandTime + ".jpg";
        }
        faceCameraView.takePicture(fileName);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void finish() {
        super.finish();
        try {
            cameraManager.setTorchMode(getCameraID, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}