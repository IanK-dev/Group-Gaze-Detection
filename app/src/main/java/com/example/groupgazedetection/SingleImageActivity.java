package com.example.groupgazedetection;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

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
import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgproc.*;
import org.opencv.core.CvType;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SingleImageActivity extends AppCompatActivity {
    //Visual Components
    ImageView preview_sin_image;
    ContentResolver contentResolver;
    //Internal Variables
    Bitmap selected_image;
    //OpenCV and Classifiers
    Mat faceMat;
    Mat eyeMat;
    Mat outputDNN;
    MatOfRect faceDetections;
    MatOfRect eyeDetections;
    private File rawFaceFile;
    private File rawEyeFile;
    private File rawCaffeFile;
    private File rawProtoFile;
    private CascadeClassifier cvFaceClassifier;
    private CascadeClassifier cvEyeClassifier;
    private Net dnnClassifier;
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
                        //Initialize CV dependent components
                        faceMat = new Mat();
                        eyeMat = new Mat();
                        faceDetections = new MatOfRect();
                        eyeDetections = new MatOfRect();
                        //Load raw classifiers
                        //Begin DNN Classifier
                        InputStream readDNNCaffe = getResources().openRawResource(R.raw.itracker_iter_92000);
                        File caffeDir = getDir("cascade", Context.MODE_PRIVATE);
                        rawCaffeFile = new File(caffeDir, "itracker_iter_92000.caffemodel");
                        FileOutputStream writeCaffeClassifier = new FileOutputStream(rawCaffeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = readDNNCaffe.read(buffer)) != -1) {
                            writeCaffeClassifier.write(buffer, 0, bytesRead);
                        }
                        Arrays.fill(buffer, (byte)0);
                        InputStream  readProto = getResources().openRawResource(R.raw.itracker_deploy);
                        rawProtoFile = new File(caffeDir, "itracker_deploy.prototxt");
                        FileOutputStream writeProto = new FileOutputStream(rawProtoFile);
                        while ((bytesRead = readProto.read(buffer)) != -1) {
                            writeProto.write(buffer, 0, bytesRead);
                        }
                        dnnClassifier = readNetFromCaffe(rawProtoFile.getAbsolutePath(), rawCaffeFile.getAbsolutePath());
                        if (dnnClassifier.empty()) {
                            dnnClassifier = null;
                        }
                        //Close and clear temporary files
                        readDNNCaffe.close();
                        writeCaffeClassifier.close();
                        readProto.close();
                        writeProto.close();
                        caffeDir.delete();
                        //Begin face cascade generation
                        /*
                        InputStream readFaceClassifier = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        rawFaceFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
                        FileOutputStream writeFaceClassifier = new FileOutputStream(rawFaceFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = readFaceClassifier.read(buffer)) != -1) {
                            writeFaceClassifier.write(buffer, 0, bytesRead);
                        }
                        cvFaceClassifier = new CascadeClassifier(rawFaceFile.getAbsolutePath());
                        if (cvFaceClassifier.empty()) {
                            cvFaceClassifier = null;
                        }
                        readFaceClassifier.close();
                        writeFaceClassifier.close();
                        //Begin eye cascade generation
                        Arrays.fill(buffer, (byte)0);
                        InputStream  readEyeClassifier = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
                        rawEyeFile = new File(cascadeDir, "haarcascade_eye_tree_eyeglasses.xml");
                        FileOutputStream writeEyeClassifier = new FileOutputStream(rawEyeFile);
                        while ((bytesRead = readEyeClassifier.read(buffer)) != -1) {
                            writeEyeClassifier.write(buffer, 0, bytesRead);
                        }
                        cvEyeClassifier = new CascadeClassifier(rawEyeFile.getAbsolutePath());
                        if (cvEyeClassifier.empty()) {
                            cvEyeClassifier = null;
                        }
                        //Close and clear temporary files
                        readEyeClassifier.close();
                        writeEyeClassifier.close();
                        cascadeDir.delete();
                         */

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
        Bitmap fixBit = selected_image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(fixBit, faceMat);
        cvFaceClassifier.detectMultiScale(faceMat, faceDetections);
        Rect[] theseFaces = faceDetections.toArray();
        for (Rect face : theseFaces) {
                    Imgproc.rectangle(
                            faceMat,
                        new Point(face.x, face.y),
                        new Point(face.x + face.width, face.y + face.height),
                        new Scalar(0, 0, 255, 255),
                    5
                    );
            }
        cvEyeClassifier.detectMultiScale(faceMat, eyeDetections);
        Rect[] theseEyes = eyeDetections.toArray();
        for (Rect eye : theseEyes) {
            Imgproc.rectangle(
                    faceMat,
                    new Point(eye.x, eye.y),
                    new Point(eye.x + eye.width, eye.y + eye.height),
                    new Scalar(0, 255, 0, 255),
                    5
            );
        }
        Utils.matToBitmap(faceMat, fixBit);
        preview_sin_image.setImageBitmap(fixBit);
    }

    public void processDNN(View view){
        System.out.print("Attempting to process image by DNN");
        Bitmap fixBit = selected_image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(fixBit, faceMat);
        Imgproc.resize(faceMat, faceMat, new Size(224, 224));
        blobFromImage(faceMat,  1.0, new Size(224, 224), new Scalar(104.0, 177.0, 123.0, 0), false, false, CvType.CV_32F);
        dnnClassifier.setInput(faceMat);
        outputDNN = dnnClassifier.forward();
    }
}