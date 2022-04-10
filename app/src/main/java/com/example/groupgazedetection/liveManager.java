package com.example.groupgazedetection;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.content.Context;

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

public class liveManager extends AppCompatActivity {
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
    public Size overlaySize;
    Rect[] theseFaces;
    Rect[] theseEyes;

    public liveManager(Context appContext, String... classParams) throws InvalidParameterException, IOException {
        //Initialize CV dependent components
        faceMat = new Mat();
        eyeMat = new Mat();
        overlay = new Mat();
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
        //Error Case
        else {
            throw new InvalidParameterException("Invalid Classifier Parameters Entered");
        }
    }
    public Mat detect(Mat gImg, Mat rgbImg) {
        if (classifierType == "haar") {
            cvFaceClassifier.detectMultiScale(gImg, faceDetections);
            theseFaces = faceDetections.toArray();
            for (Rect face : theseFaces) {
                Imgproc.rectangle(
                        rgbImg,
                        new Point(face.x, face.y),
                        new Point(face.x + face.width, face.y + face.height),
                        new Scalar(0, 0, 255, 255),
                        5
                );
            }
            cvEyeClassifier.detectMultiScale(gImg, eyeDetections);
            theseEyes = eyeDetections.toArray();
            for (Rect eye : theseEyes) {
                Imgproc.rectangle(
                        rgbImg,
                        new Point(eye.x, eye.y),
                        new Point(eye.x + eye.width, eye.y + eye.height),
                        new Scalar(0, 255, 0, 255),
                        5
                );
            }
            return rgbImg;
        } else {
            return null;
        }
    }

    //Good One
    public Mat detect(Mat gImg) {
        if (classifierType == "haar") {
            overlay = Mat.zeros(gImg.height(), gImg.width(), CvType.CV_8UC4);
            cvFaceClassifier.detectMultiScale(gImg, faceDetections);
            theseFaces = faceDetections.toArray();
            for (Rect face : theseFaces) {
                Imgproc.rectangle(
                        overlay,
                        new Point(face.x, face.y),
                        new Point(face.x + face.width, face.y + face.height),
                        new Scalar(0, 0, 255, 255),
                        5
                );
            }
            cvEyeClassifier.detectMultiScale(gImg, eyeDetections);
            theseEyes = eyeDetections.toArray();
            for (Rect eye : theseEyes) {
                Imgproc.rectangle(
                        overlay,
                        new Point(eye.x, eye.y),
                        new Point(eye.x + eye.width, eye.y + eye.height),
                        new Scalar(0, 255, 0, 255),
                        5
                );
            }
            return overlay;
        }  else {
            return null;
        }
    }
}