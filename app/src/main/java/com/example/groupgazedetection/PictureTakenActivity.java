package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class PictureTakenActivity extends AppCompatActivity {
    ImageView preview_Image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_taken);
        preview_Image = findViewById(R.id.demoPicture);
        Bundle extras = getIntent().getExtras();
        Uri pictureFrame = Uri.parse(extras.getString("picture"));
        preview_Image.setImageURI(pictureFrame);
    }
    public void returnToLive(View view){
        Intent intent = new Intent(this, LiveDetectionActivity.class);
        startActivity(intent);
    }

    public void deleteImage(View view){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    public void saveImage(View view){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    public void previewGaze(View view){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}