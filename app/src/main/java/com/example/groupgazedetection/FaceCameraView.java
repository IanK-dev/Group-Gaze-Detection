package com.example.groupgazedetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.opencv.android.JavaCameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class FaceCameraView extends JavaCameraView implements Camera.PictureCallback {
    String mFileName;
    Context thisContext;
    public FaceCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thisContext = context;
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
        Intent intent = new Intent(thisContext, PictureTakenActivity.class);
        Log.d("PictureDemo", "Size of Picture Byte Array:" + data.length);
        /*
        File cachePath = new File(thisContext.getApplicationContext().getCacheDir(), "images");
        cachePath.mkdirs();
        Bitmap tempPic = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            FileOutputStream cacheStream = new FileOutputStream(cachePath + "/" + mFileName);
            tempPic.compress(Bitmap.CompressFormat.JPEG, 90, cacheStream);
            tempPic.recycle();
            cacheStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File sendLocal = new File(cachePath, mFileName);
        Log.i("PictureDemo", "Context: " + thisContext.toString());
        Log.i("PictureDemo", "Co: " + thisContext.toString());
        */
        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            FileOutputStream fos = new FileOutputStream(mFileName);
            picture.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            picture.recycle();
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
        Uri sendURI = FileProvider.getUriForFile(thisContext, thisContext.getPackageName() + ".provider", new File(mFileName));
        intent.putExtra("picture", sendURI.toString());
        intent.putExtra("filename", mFileName);
        thisContext.startActivity(intent);
        // Write the image in a file (in jpeg format)
        /*
        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            FileOutputStream fos = new FileOutputStream(mFileName);
            picture.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            picture.recycle();
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
        */

    }

}
