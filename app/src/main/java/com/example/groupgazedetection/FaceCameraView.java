package com.example.groupgazedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.List;

public class FaceCameraView extends JavaCameraView implements Camera.PictureCallback {
    String mFileName;
    public FaceCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public String getFocusMode() {
        return mCamera.getParameters().getFocusMode();
    }

    public List<String> getFocusModes() {
        return mCamera.getParameters().getSupportedFocusModes();
    }

    public String getFlashMode() {
        return mCamera.getParameters().getFlashMode();
    }
    public List<String> getFlashModes() {
        return mCamera.getParameters().getSupportedFlashModes();
    }

    public void setFlashMode(String type) {
        Camera.Parameters params = mCamera.getParameters();
        Log.i("PictureDemo", "Parameters path: " + params.toString());
        params.setFlashMode(type);
        mCamera.setParameters(params);
    }

    public void takePicture(final String fileName) {
        Log.i("PictureDemo", "Picture path: " + fileName);
        this.mFileName = fileName;
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i("PictureDemo", "Saving a bitmap to file");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            FileOutputStream fos = new FileOutputStream(mFileName);
            picture.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            picture.recycle();
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

    }

}
