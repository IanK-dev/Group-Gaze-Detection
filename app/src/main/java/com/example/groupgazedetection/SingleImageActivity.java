package com.example.groupgazedetection;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Context;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import java.io.IOException;

public class  SingleImageActivity extends AppCompatActivity {
    //Visual Components
    ImageView preview_sin_image;
    ContentResolver contentResolver;
    //Internal Variables
    Bitmap selected_image;
    Context currentAppContext;
    cvManager openManager;

    //User content contract resolver
    ActivityResultLauncher<Intent> imageSelection = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            try {
                                selected_image = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri);
                                preview_sin_image.setImageURI(selectedImageUri);
                                Log.d("Debug", "Successfully converted to bitmap");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    //BaseLoader to load OpenCV library
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    try {
                        openManager = new cvManager(currentAppContext, "haar");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);
        preview_sin_image = findViewById(R.id.single_image_preview);
        currentAppContext = this;
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

    public void selectImage(View view){
        Intent select_image = new Intent();
        select_image.setType("image/*");
        select_image.setAction(Intent.ACTION_GET_CONTENT);
        imageSelection.launch(Intent.createChooser(select_image, "Select an Image"));
    }

    public void processImage(View view){
        System.out.print("Attempting to process image");
        Log.d("SingleImage", "Attempting to Process Image");
        Bitmap fixBit = selected_image.copy(Bitmap.Config.ARGB_8888, true);
        openManager.detect(fixBit);
        preview_sin_image.setImageBitmap(fixBit);
    }

    public void gotoSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}