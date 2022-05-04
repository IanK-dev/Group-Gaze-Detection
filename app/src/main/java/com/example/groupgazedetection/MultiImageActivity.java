package com.example.groupgazedetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
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

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public class MultiImageActivity extends AppCompatActivity {

    Button selectImages;
    Button selectDir;
    ImageView imageDisplay;
    TextView totalFrames;
    TextView currentFrame;
    ArrayList<Uri> imageUris;
    ArrayList<Bitmap> processedImages;
    ArrayList<File> dirFiles;
    cvManager multiManager;
    Context currentAppContext;
    private boolean selectType = false;
    boolean processStatus = false;

    int position = 0;
    private int currentIndex = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_image);
        selectImages = findViewById(R.id.selectMultipleImages);
        selectDir = findViewById(R.id.selectDirectory);
        totalFrames = findViewById(R.id.multiTotalFrames);
        currentFrame = findViewById(R.id.multiCurrentFrame);
        imageDisplay = findViewById(R.id.imageDisplay);
        selectDir.setVisibility(View.GONE);
        imageUris = new ArrayList<Uri>();
        dirFiles = new ArrayList<File>();
        processedImages = new ArrayList<Bitmap>();
        currentAppContext = this;
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
        }
        else if(requestCode == 2 && resultCode == RESULT_OK && null != data){
            Log.d("MultiImageActivity", "Successful Directory Choice");
            Log.d("MultiImageActivity", "Result URI " + data.getData());
            try{
                traverseDirectoryEntries(data.getData());
            }catch (Exception e){
                Log.d("MultiImageActivity", "Invalid Directory Selection");
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

    public void toggleSelection(View view){
        if(selectType){
            selectImages.setVisibility(View.VISIBLE);
            selectDir.setVisibility(View.GONE);
            selectType = false;
        }else{
            selectImages.setVisibility(View.GONE);
            selectDir.setVisibility(View.VISIBLE);
            selectType = true;
        }
    }

    public void selectImages(View view){
        Intent intent = new Intent();
        // setting type to select to be image
        intent.setType("image/*");
        // allowing multiple image to be selected
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    public void selectDirectory(View view){
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory"), 2);
    }

    void traverseDirectoryEntries(Uri rootUri) {
        ContentResolver contentResolver = this.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(childrenUri);
        while(!dirNodes.isEmpty()) {
            childrenUri = dirNodes.remove(0);
            Cursor c = contentResolver.query(childrenUri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                while (c.moveToNext()) {
                    final String docId = c.getString(0);
                    final String mime = c.getString(1);

                    if(docId.endsWith(".jpg") || docId.endsWith(".jpeg") || docId.endsWith(".png")){
                        Uri docUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, docId);
                        imageUris.add(docUri);
                    }
                    if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                        final Uri newNode = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, docId);
                        dirNodes.add(newNode);
                    }
                }
            } catch (Exception e){
                Log.d("MultiImageActivity", "Folder Selection Error");
            }
            c.close();
            imageDisplay.setImageURI(imageUris.get(currentIndex));
            //closeQuietly(c);
        }
    }
}
