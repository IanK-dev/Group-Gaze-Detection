package com.example.groupgazedetection;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.content.Context;
import android.util.Log;

import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.sql.Blob;
import java.util.Arrays;

public class cvManager extends AppCompatActivity {
    private static String classifierType;
    //Global Variables
    //OpenCV and Classifiers
    public static Mat faceMat;
    public static Mat greyMat;
    private Mat subFace;
    private MatOfKeyPoint pupilLeftKeys;
    private MatOfKeyPoint pupilRightKeys;
    //Turn true if testing
    public boolean testGaze = false;
    //public static Mat outputDNN;
    public static MatOfRect faceDetections;
    public List<detectedFace> detectedFaces;
    private File rawFaceFile;
    private File rawEyeFile;
    private File rawCaffeFile;
    private File rawProtoFile;
    private static CascadeClassifier cvFaceClassifier;
    private static CascadeClassifier cvEyeClassifier;
    private static Net dnnClassifier;
    private SimpleBlobDetector blobDetector;
    private SimpleBlobDetector_Params blobParams;
    public Mat overlay;
    KeyPoint[] keyLeftAr;
    KeyPoint[] keyRightAr;
    Rect[] theseFaces;

    public cvManager(Context appContext, String... classParams) throws InvalidParameterException, IOException {
        //Initialize CV dependent components
        faceMat = new Mat();
        greyMat = new Mat();
        subFace = new Mat();
        detectedFaces = new ArrayList<>();
        faceDetections = new MatOfRect();
        classifierType = classParams[0];
        //Create color blob detection
        blobParams = new SimpleBlobDetector_Params();
        blobParams.set_filterByArea(true);
        blobParams.set_minArea(100);
        blobParams.set_maxArea(1000);
        blobParams.set_filterByColor(false);
        blobParams.set_filterByConvexity(false);
        blobParams.set_filterByCircularity(false);
        //blobParams.set_minCircularity(0.2f);
        //blobParams.set_maxCircularity(3.4f);
        blobParams.set_filterByInertia(true);
        //blobParams.set_minThreshold(28);
        //blobParams.set_maxThreshold(68);
        //blobParams.set_minRepeatability(2);
        //blobParams.set_thresholdStep(20);
        //blobParams.get_filterByCircularity();
        Log.d("cvManager", "Circularity: " + blobParams.get_filterByCircularity());
        Log.d("cvManager", "Color: " + blobParams.get_filterByColor());
        Log.d("cvManager", "Convexity: " + blobParams.get_filterByConvexity());
        Log.d("cvManager", "Inertia: " + blobParams.get_filterByInertia());
        Log.d("cvManager", "Min Circularity: " + blobParams.get_minCircularity());
        Log.d("cvManager", "Max Circularity: " + blobParams.get_maxCircularity());
        blobDetector = SimpleBlobDetector.create(blobParams);
        pupilLeftKeys = new MatOfKeyPoint();
        pupilRightKeys = new MatOfKeyPoint();
        //Receive and update settings preferences from menu
        SharedPreferences thesePreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        Rect[][] results;
        Log.d("cvManager", "Preferences Result: " + thesePreferences.getString("signature", "0"));
        //Haars Cascade
        if (classParams[0] == "haar") {
            /*
            if(classParams.length < 3 || classParams.length > 3){
                throw new InvalidParameterException("Invalid Classifier Parameters Entered");
            }*/
            //Begin face cascade generation
            String faceSelection = "haarcascade_frontalface_alt2";
            int faceID = appContext.getResources().getIdentifier(faceSelection, "raw", appContext.getPackageName());
            InputStream readFaceClassifier = appContext.getResources().openRawResource(faceID);
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
    }

    public Bitmap detect(Bitmap inputImage) {
        Log.d("OpenCV", "Attempting Detection");
        if (classifierType == "haar") {
            Utils.bitmapToMat(inputImage, cvManager.faceMat);
            Imgproc.cvtColor(cvManager.faceMat, greyMat, Imgproc.COLOR_BGR2GRAY);
            cvFaceClassifier.detectMultiScale(greyMat, faceDetections);
            Rect[] theseFaces = faceDetections.toArray();
            int i = 0;
            for (Rect face : theseFaces) {
                //Mat inputFace = new Mat();
                //Mat croppedFace = inputFace(Range(1, 1), Range(1, 1));
                Mat inputFace = new Mat(greyMat, face);
                //greyMat.submat(face).copyTo(inputFace);
                detectedFace newFace = new detectedFace(inputFace);
                newFace.faceCords = face;
                detectedFaces.add(newFace);
                Log.d("OpenCV", "Face Detected");
                Imgproc.rectangle(
                        faceMat,
                        new Point(face.x, face.y),
                        new Point(face.x + face.width, face.y + face.height),
                        new Scalar(255, 0, 0, 255),
                        2
                );
            }
            Log.d("cvManager", String.valueOf(detectedFaces.size()));
            for(detectedFace dFace : detectedFaces){
                MatOfRect eyeDetections = new MatOfRect();
                cvEyeClassifier.detectMultiScale(dFace.face, eyeDetections);
                Rect[] theseEyes = eyeDetections.toArray();
                if (theseEyes.length > 1){
                    Log.d("cvManager", String.valueOf(theseEyes.length));
                    for (Rect eye : theseEyes ){
                        if (eye.x < (dFace.faceCords.width/2)){
                            Log.d("cvManager", "Left eye detected");
                            //Mat inputEye = new Mat();
                            Mat inputEye = new Mat(dFace.face, eye);
                            //dFace.face.submat(eye).copyTo(inputEye);
                            dFace.eyeLeft = inputEye;
                            dFace.eyeLeftCords = eye;
                        }
                        else{
                            Log.d("cvManager", "Right eye detected");
                            Mat inputEye = new Mat();
                            dFace.face.submat(eye).copyTo(inputEye);
                            dFace.eyeRight = inputEye;
                            dFace.eyeRightCords = eye;
                        }

                        Imgproc.rectangle(
                                faceMat,
                                //ADD previous rect dimensions when Drawing
                                new Point((eye.x + dFace.faceCords.x), (eye.y + dFace.faceCords.y)),
                                new Point((dFace.faceCords.x) + (eye.x + eye.width), (dFace.faceCords.y) + (eye.y + eye.height)),
                                new Scalar(0, 0, 255, 255),
                                2
                        );
                    }
                }
                else{
                    Log.d("cvManager", "Invalid number of eyes");
                    dFace.validGaze = false;
                }
            }
            //Modify this to change the threshholding amount
            //A good one is 22
            int athreshold = 22;
            for (detectedFace gFace : detectedFaces){
                if(gFace.validGaze){
                    Mat eyeLeftCopy = new Mat();
                    Mat eyeRightCopy = new Mat();
                    //Change this to modify the amount of erosion
                    int eSize = 10;
                    //Change this to modify the amount of dilation
                    int dSize = 10;
                    Log.d("cvManager", "Size of full mat given: " + greyMat.size());
                    do {
                        eyeLeftCopy = gFace.eyeLeft.clone();
                        Imgproc.threshold(eyeLeftCopy, eyeLeftCopy, athreshold, 255, Imgproc.THRESH_BINARY);
                        Imgproc.erode(eyeLeftCopy, eyeLeftCopy, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(eSize, eSize)));
                        Imgproc.dilate(eyeLeftCopy, eyeLeftCopy, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dSize, dSize)));
                        blobDetector.detect(eyeLeftCopy, pupilLeftKeys);
                        keyLeftAr = pupilLeftKeys.toArray();
                        Log.d("cvManager", "Left Size " + pupilLeftKeys.size());
                        Log.d("cvManager", "Left Width " + pupilLeftKeys.size().width);
                        Log.d("cvManager", "Left Width " + pupilLeftKeys.size().height);
                        athreshold = athreshold + 20;
                        Log.d("cvManager", "Left New Threshold " + athreshold);
                    }while(athreshold < 63 && keyLeftAr.length < 1);
                    athreshold = 22;
                    do{
                        eyeRightCopy = gFace.eyeRight.clone();
                        Imgproc.threshold(eyeRightCopy, eyeRightCopy, athreshold, 255, Imgproc.THRESH_BINARY);
                        Imgproc.dilate(eyeRightCopy, eyeRightCopy, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dSize, dSize)));
                        Imgproc.erode(eyeRightCopy, eyeRightCopy, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(eSize, eSize)));
                        blobDetector.detect(eyeRightCopy, pupilRightKeys);
                        keyRightAr = pupilRightKeys.toArray();
                        Log.d("cvManager", "Right Size " + pupilRightKeys.size());
                        Log.d("cvManager", "Right Width " + pupilRightKeys.size().width);
                        Log.d("cvManager", "Right Height " + pupilRightKeys.size().height);
                        athreshold = athreshold + 20;
                        Log.d("cvManager", "Right New Threshold " + athreshold);
                    }while(athreshold < 63 && keyRightAr.length < 1);
                    athreshold = 22;

                    //Log.d("cvManager", "Number of keypoints: " + pupilKeys.toString());
                    //Log.d("cvManager", "Number of keypoints: " + pupilKeys.size());
                    //Features2d.drawKeypoints(gFace.eyeLeft, pupilKeys, gFace.eyeLeft);
                    if(testGaze == false){
                        if(keyLeftAr.length > 0){
                            Log.d("cvManager", "Left Key: " + keyLeftAr[0]);
                            keyLeftAr[0].pt.x = keyLeftAr[0].pt.x + gFace.eyeLeftCords.x + gFace.faceCords.x;
                            keyLeftAr[0].pt.y = keyLeftAr[0].pt.y + gFace.eyeLeftCords.y + gFace.faceCords.y;
                        }
                        if(keyRightAr.length > 0){
                            Log.d("cvManager", "Right Key: " + keyRightAr[0]);
                            keyRightAr[0].pt.x = keyRightAr[0].pt.x + gFace.eyeRightCords.x + gFace.faceCords.x;
                            keyRightAr[0].pt.y = keyRightAr[0].pt.y + gFace.eyeRightCords.y + gFace.faceCords.y;
                        }
                        //Log.d("cvManager", "Keypoint X: " + keyAr[0].pt.x);
                        pupilLeftKeys.fromArray(keyLeftAr);
                        pupilRightKeys.fromArray(keyRightAr);
                        Features2d.drawKeypoints(faceMat, pupilLeftKeys, faceMat, new Scalar(0, 255, 0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                        Features2d.drawKeypoints(faceMat, pupilRightKeys, faceMat, new Scalar(0, 255, 0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                    }
                    else{
                        Features2d.drawKeypoints(eyeRightCopy, pupilRightKeys, eyeRightCopy, new Scalar(0, 255, 0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                        subFace = eyeRightCopy;
                    }
                    Log.d("cvManager", "Updated Greymat size: " + greyMat.size());
                }
            }
            if(testGaze == true){
                Bitmap testingBitMap = Bitmap.createBitmap(subFace.width(), subFace.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(subFace, testingBitMap);
                return testingBitMap;
            }
            else{
                Utils.matToBitmap(faceMat, inputImage);
                return inputImage;
            }
        }
        else{
            return inputImage;
        }
    }
}