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
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

public class PictureTakenActivity extends AppCompatActivity {
    ImageView preview_Image;
    ImageView preview_Gaze;
    Button button_Gaze;
    Button button_Photo;
    Uri pictureFrame;
    ContentResolver contentResolver;
    cvManager openManager;
    Context currentAppContext;
    boolean alreadyProcessed = false;
    Bitmap bitGaze;

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
        preview_Gaze = findViewById(R.id.demoGaze);
        button_Photo = findViewById(R.id.preview_photo);
        button_Photo.setVisibility(View.GONE);
        button_Gaze = findViewById(R.id.preview_gaze);
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

    private void processGaze() throws IOException {
        Bitmap fixBit = (MediaStore.Images.Media.getBitmap(contentResolver, pictureFrame)).copy(Bitmap.Config.ARGB_8888, true);
        if(fixBit.getWidth() > 4000 || fixBit.getHeight() > 4000){
            fixBit = Bitmap.createScaledBitmap(fixBit, fixBit.getWidth()/4, fixBit.getHeight()/4, true);
        }
        else if(fixBit.getWidth() > 3000 || fixBit.getHeight() > 3000){
            fixBit = Bitmap.createScaledBitmap(fixBit, fixBit.getWidth()/3, fixBit.getHeight()/3, true);
        }
        else if(fixBit.getWidth() > 2000 || fixBit.getHeight() > 2000){
            fixBit = Bitmap.createScaledBitmap(fixBit, fixBit.getWidth()/2, fixBit.getHeight()/2, true);
        }
        else if(fixBit.getWidth() < 500 || fixBit.getHeight() < 500){
            fixBit = Bitmap.createScaledBitmap(fixBit, fixBit.getWidth()*2, fixBit.getHeight()*2, true);
        }
        bitGaze = openManager.detect(fixBit);
        alreadyProcessed = true;
    }

    public void previewGaze(View view) throws IOException {
        if(alreadyProcessed == false){
            processGaze();
        }
        preview_Gaze.setImageBitmap(bitGaze);
        preview_Image.setVisibility(View.INVISIBLE);
        preview_Gaze.setVisibility(View.VISIBLE);
        button_Gaze.setVisibility(View.GONE);
        button_Photo.setVisibility(View.VISIBLE);
    }

    public void previewPhoto(View view){
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        preview_Image.setVisibility(View.VISIBLE);
        preview_Gaze.setVisibility(View.INVISIBLE);
        button_Gaze.setVisibility(View.VISIBLE);
        button_Photo.setVisibility(View.GONE);
    }
}