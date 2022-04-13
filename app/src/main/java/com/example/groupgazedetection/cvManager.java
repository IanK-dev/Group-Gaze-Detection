package com.example.groupgazedetection;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.content.Context;
import android.util.Log;

import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class cvManager extends AppCompatActivity {
    private static String classifierType;
    //Global Variables
    //OpenCV and Classifiers
    public static Mat faceMat;
    public static Mat eyeMat;
    public static Mat outputDNN;
    public static MatOfRect faceDetections;
    public static MatOfRect eyeDetections;
    private File rawFaceFile;
    private File rawEyeFile;
    private File rawCaffeFile;
    private File rawProtoFile;
    private static CascadeClassifier cvFaceClassifier;
    private static CascadeClassifier cvEyeClassifier;
    private static Net dnnClassifier;
    public Mat overlay;
    Rect[] theseFaces;
    Rect[] theseEyes;

    public cvManager(Context appContext, String... classParams) throws InvalidParameterException, IOException {
        //Initialize CV dependent components
        faceMat = new Mat();
        eyeMat = new Mat();
        faceDetections = new MatOfRect();
        eyeDetections = new MatOfRect();
        classifierType = classParams[0];
        Rect[][] results;
        //Haars Cascade
        if (classParams[0] == "haar") {
            /*
            if(classParams.length < 3 || classParams.length > 3){
                throw new InvalidParameterException("Invalid Classifier Parameters Entered");
            }*/
            //Begin face cascade generation
            InputStream readFaceClassifier = appContext.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = appContext.getDir("cascade", Context.MODE_PRIVATE);
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
            Arrays.fill(buffer, (byte) 0);
            InputStream readEyeClassifier = appContext.getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
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
        }
        //DNN
        else if (classParams[0] == "dnn") {
            InputStream readDNNCaffe = appContext.getResources().openRawResource(R.raw.itracker_iter_92000);
            File caffeDir = appContext.getDir("cascade", Context.MODE_PRIVATE);
            rawCaffeFile = new File(caffeDir, "itracker_iter_92000.caffemodel");
            FileOutputStream writeCaffeClassifier = new FileOutputStream(rawCaffeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = readDNNCaffe.read(buffer)) != -1) {
                writeCaffeClassifier.write(buffer, 0, bytesRead);
            }
            Arrays.fill(buffer, (byte) 0);
            InputStream readProto = appContext.getResources().openRawResource(R.raw.itracker_deploy);
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
        }
        //Error Case
        else {
            throw new InvalidParameterException("Invalid Classifier Parameters Entered");
        }
    }

    public Bitmap detect(Bitmap inputImage) {
        Log.d("OpenCV", "Attempting Detection");
        if (classifierType == "haar") {
            Utils.bitmapToMat(inputImage, cvManager.faceMat);
            cvFaceClassifier.detectMultiScale(faceMat, faceDetections);
            Rect[] theseFaces = faceDetections.toArray();
            for (Rect face : theseFaces) {
                Log.d("OpenCV", "Face Detected");
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
                Log.d("OpenCV", "Eye Detected");
                Imgproc.rectangle(
                        faceMat,
                        new Point(eye.x, eye.y),
                        new Point(eye.x + eye.width, eye.y + eye.height),
                        new Scalar(0, 255, 0, 255),
                        5
                );
            }
            Utils.matToBitmap(cvManager.faceMat, inputImage);
            return inputImage;
        } else if (classifierType == "dnn") {
            System.out.print("Attempting to process image by DNN");
            Utils.bitmapToMat(inputImage, faceMat);
            Imgproc.resize(faceMat, faceMat, new Size(224, 224));
            blobFromImage(faceMat, 1.0, new Size(224, 224), new Scalar(104.0, 177.0, 123.0, 0), false, false, CvType.CV_32F);
            dnnClassifier.setInput(faceMat);
            outputDNN = dnnClassifier.forward();
            return null;
        } else {
            return null;
        }
    }
}