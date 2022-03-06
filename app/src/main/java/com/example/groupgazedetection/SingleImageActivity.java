package com.example.groupgazedetection;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class SingleImageActivity extends AppCompatActivity {

    Button select_sin_image_button;
    ImageView preview_sin_image;
    private CascadeClassifier facedetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);
        preview_sin_image = findViewById(R.id.single_image_preview);
    }

    ActivityResultLauncher<Intent> imageSelection = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            preview_sin_image.setImageURI(selectedImageUri);
                        }
                    }
                }
            });

    public void selectImage(View view){
        Intent select_image = new Intent();
        select_image.setType("image/*");
        select_image.setAction(Intent.ACTION_GET_CONTENT);
        imageSelection.launch(Intent.createChooser(select_image, "Select an Image"));
    }

    public void processImage(View view){
        System.out.print("Begin image processing here");
    }
}