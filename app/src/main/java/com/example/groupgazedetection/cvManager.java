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
    private MatOfKeyPoint pupilKeys;
    public boolean testGaze = true;
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
        //blobParams.get_filterByCircularity();
        Log.d("cvManager", "Circularity: " + blobParams.get_filterByCircularity());
        Log.d("cvManager", "Color: " + blobParams.get_filterByColor());
        Log.d("cvManager", "Convexity: " + blobParams.get_filterByConvexity());
        Log.d("cvManager", "Inertia: " + blobParams.get_filterByInertia());
        Log.d("cvManager", "Min Circularity: " + blobParams.get_minCircularity());
        Log.d("cvManager", "Max Circularity: " + blobParams.get_maxCircularity());
        blobDetector = SimpleBlobDetector.create(blobParams);
        pupilKeys = new MatOfKeyPoint();
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
                        new Scalar(0, 0, 255, 255),
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
                                new Scalar(0, 255, 0, 255),
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
            int athreshold = 40;
            for (detectedFace gFace : detectedFaces){
                if(gFace.validGaze){
                    Mat eyeLeftCopy = gFace.eyeLeft.clone() ;
                    //Change this to modify the amount of erosion
                    int eSize = 10;
                    //Change this to modify the amount of dilation
                    int dSize = 10;
                    Log.d("cvManager", "Size of full mat given: " + greyMat.size());
                    Log.d("cvManager", "Size of eye mat given: " + gFace.eyeLeft.size());
                    Imgproc.threshold(eyeLeftCopy, eyeLeftCopy, athreshold, 255, Imgproc.THRESH_BINARY);
                    Imgproc.erode(eyeLeftCopy, eyeLeftCopy, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(eSize, eSize)));
                    Imgproc.dilate(eyeLeftCopy, eyeLeftCopy, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dSize, dSize)));
                    //gFace.eyeLeft
                    blobDetector.detect(eyeLeftCopy, pupilKeys);
                    Log.d("cvManager", "Number of keypoints: " + pupilKeys.toString());
                    Log.d("cvManager", "Number of keypoints: " + pupilKeys.size());
                    //Features2d.drawKeypoints(gFace.eyeLeft, pupilKeys, gFace.eyeLeft);
                    Features2d.drawKeypoints(gFace.eyeLeft, pupilKeys, gFace.eyeLeft, new Scalar(10, 255, 0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                    //Mat thisMat = new Mat(gFace.face, gFace.eyeLeftCords);
                    //subFace = eyeMask;
                    //Mat newImage = (gFace.face).submat(new Rect(gFace.eyeLeftCords.x, gFace.eyeLeftCords.y, gFace.eyeLeft.cols(), gFace.eyeLeft.rows()));
                    //(gFace.eyeLeft).copyTo(gFace.face);
                    //(gFace.face).copyTo(newImage);
                    subFace = eyeLeftCopy;
                    //eyeMask.copyTo(gFace.face);
                    //(gFace.face).copyTo(greyMat);
                    //mergeFace = greyMat.submat(new Rect(gFace.faceCords.x, gFace.faceCords.y, gFace.face.cols(), gFace.face.rows()));
                    //(newImage).copyTo(mergeFace);
                    //greyMat.copyTo(mergeFace);
                    /*
                    Mat newImage = gFace.face.submat(new Rect(gFace.eyeLeftCords.x, gFace.eyeLeftCords.y, gFace.eyeLeft.cols(), gFace.eyeLeft.rows()));
                    (gFace.eyeLeft).copyTo(newImage);
                    mergeFace = greyMat.submat(new Rect(gFace.faceCords.x, gFace.faceCords.y, gFace.face.cols(), gFace.face.rows()));
                    (newImage).copyTo(mergeFace);
                    /*
                     */
                    Log.d("cvManager", "Updated Greymat size: " + greyMat.size());
                    //gFace.eyeLeft
                    //How to get/use mat for right eye
                    //gFace.eyeRight
                    //Color blob goes here
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