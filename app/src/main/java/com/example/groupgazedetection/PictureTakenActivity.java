package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

public class PictureTakenActivity extends AppCompatActivity {
    ImageView preview_Image;
    Uri pictureFrame;
    ContentResolver contentResolver;
    cvManager openManager;
    Context currentAppContext;

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
        setContentView(R.layout.activity_picture_taken);
        currentAppContext = this;
        preview_Image = findViewById(R.id.demoPicture);
        Bundle extras = getIntent().getExtras();
        pictureFrame = Uri.parse(extras.getString("picture"));
        preview_Image.setImageURI(pictureFrame);
        contentResolver = this.getContentResolver();
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

    public void returnToLive(View view){
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }

    public void deleteImage(View view){
        File deleteCache = new File(pictureFrame.getPath());
        if(deleteCache.exists()){
            if(deleteCache.delete()){
                Log.d("PictureTaken", "Image Successfully Deleted");
            }else{
                Log.d("PictureTaken", "Image Deletion Failed");
            }
        }
        else{
            Log.d("PictureTaken", "Image DNE");
        }
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }

    public void saveImage(View view){
        Log.d("PictureTaken", "Image Successfully Saved");
    }

    public void previewGaze(View view) throws IOException {
        Bitmap bitmapPhoto = MediaStore.Images.Media.getBitmap(contentResolver, pictureFrame);

    }
}