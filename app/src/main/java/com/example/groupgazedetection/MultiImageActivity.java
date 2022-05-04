package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MultiImageActivity extends AppCompatActivity {

    Button selectImages;
    ImageView imageDisplay;
    TextView totalFrames;
    TextView currentFrame;
    ArrayList<Uri> imageUris;
    ArrayList<Bitmap> processedImages;
    cvManager multiManager;
    Context currentAppContext;
    boolean processStatus = false;
    int position = 0;
    private int currentIndex = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_image);
        selectImages = findViewById(R.id.select);
        totalFrames = findViewById(R.id.multiTotalFrames);
        currentFrame = findViewById(R.id.multiCurrentFrame);
        imageDisplay = findViewById(R.id.imageDisplay);
        imageUris = new ArrayList<Uri>();
        processedImages = new ArrayList<Bitmap>();
        currentAppContext = this;
        selectImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initialising intent
                Intent intent = new Intent();
                // setting type to select to be image
                intent.setType("image/*");
                // allowing multiple image to be selected
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        imageDisplay.setOnTouchListener(new swipeListener(this) {
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() {
                if(currentIndex > 0){
                    currentIndex = currentIndex - 1;
                    if(processStatus == true){
                        imageDisplay.setImageBitmap(processedImages.get(currentIndex));
                    }else{
                        imageDisplay.setImageURI(imageUris.get(currentIndex));
                    }
                    currentFrame.setText("Current Image: " + (currentIndex + 1));
                }
                Log.d("MultiImageActivity", "Swiped right");
            }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeLeft() {
                if(currentIndex < imageUris.size() - 1){
                    currentIndex = currentIndex + 1;
                    if(processStatus == true){
                        imageDisplay.setImageBitmap(processedImages.get(currentIndex));
                    }else{
                        imageDisplay.setImageURI(imageUris.get(currentIndex));
                    }
                    currentFrame.setText("Current Image: " + (currentIndex + 1));
                }
                Log.d("MultiImageActivity", "Swiped left");
            }
            @SuppressLint("ClickableViewAccessibility")
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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                try {
                    multiManager = new cvManager(currentAppContext, "haar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                int cout = data.getClipData().getItemCount();
                for (int i = 0; i < cout; i++) {
                    Uri imageurl = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageurl);
                }
                // setting 1st selected image into image switcher
                imageDisplay.setImageURI(imageUris.get(0));
                position = 0;
            } else {
                Uri imageurl = data.getData();
                imageUris.add(imageurl);
                imageDisplay.setImageURI(imageUris.get(0));
                position = 0;
            }
        } else {
            // show this if no image is selected
        }
        totalFrames.setText("Total Images: " + imageUris.size());
    }
    public void gotoSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goToHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    public void processAllImages(View view) throws IOException {
        for(Uri image : imageUris){
            //Log.d("MultiImage", "Looping");
            Bitmap tempMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            if(tempMap.getWidth() > 4000 || tempMap.getHeight() > 4000){
                tempMap = Bitmap.createScaledBitmap(tempMap, tempMap.getWidth()/4, tempMap.getHeight()/4, true);
            }
            else if(tempMap.getWidth() > 3000 || tempMap.getHeight() > 3000){
                tempMap = Bitmap.createScaledBitmap(tempMap, tempMap.getWidth()/3, tempMap.getHeight()/3, true);
            }
            else if(tempMap.getWidth() > 2000 || tempMap.getHeight() > 2000){
                tempMap = Bitmap.createScaledBitmap(tempMap, tempMap.getWidth()/2, tempMap.getHeight()/2, true);
            }
            else if(tempMap.getWidth() < 500 || tempMap.getHeight() < 500){
                tempMap = Bitmap.createScaledBitmap(tempMap, tempMap.getWidth()*2, tempMap.getHeight()*2, true);
            }
            processedImages.add(multiManager.detect(tempMap));
        }
        processStatus = true;
        imageDisplay.setImageBitmap(processedImages.get(currentIndex));
    }
}
