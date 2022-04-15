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
    public static Mat eyeMat;
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
    public Mat overlay;
    Rect[] theseFaces;

    public cvManager(Context appContext, String... classParams) throws InvalidParameterException, IOException {
        //Initialize CV dependent components
        faceMat = new Mat();
        detectedFaces = new ArrayList<>();
        faceDetections = new MatOfRect();
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
            rawCaffeFile = new File(caffeDir, "itracker_iter_92000");
            FileOutputStream writeCaffeClassifier = new FileOutputStream(rawCaffeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = readDNNCaffe.read(buffer)) != -1) {
                writeCaffeClassifier.write(buffer, 0, bytesRead);
            }
            Arrays.fill(buffer, (byte) 0);
            InputStream readProto = appContext.getResources().openRawResource(R.raw.itracker_deploy);
            rawProtoFile = new File(caffeDir, "itracker_deploy.prototext");
            FileOutputStream writeProto = new FileOutputStream(rawProtoFile);
            while ((bytesRead = readProto.read(buffer)) != -1) {
                writeProto.write(buffer, 0, bytesRead);
            }
            dnnClassifier = readNetFromCaffe(rawProtoFile.getAbsolutePath(), rawCaffeFile.getAbsolutePath());

            /*
            InputStream readDNNCaffe = appContext.getResources().openRawResource(R.raw.nn4small2);
            File caffeDir = appContext.getDir("cascade", Context.MODE_PRIVATE);
            rawCaffeFile = new File(caffeDir, "nn4small2.t7");
            FileOutputStream writeCaffeClassifier = new FileOutputStream(rawCaffeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = readDNNCaffe.read(buffer)) != -1) {
                writeCaffeClassifier.write(buffer, 0, bytesRead);
            }
            /*
             */
            //dnnClassifier = Dnn.readNetFromTorch(rawCaffeFile.getAbsolutePath());
            //dnnClassifier = dnn.read
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
            int i = 0;
            for (Rect face : theseFaces) {
                Mat inputFace = new Mat();
                faceMat.submat(face).copyTo(inputFace);
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
                            Mat inputEye = new Mat();
                            dFace.face.submat(eye).copyTo(inputEye);
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
            //Start Color Blob here
            int threshold = 50;
            for (detectedFace gFace : detectedFaces){
                if(gFace.validGaze){
                    //How to get mat/use for left eye
                    //gFace.eyeLeft
                    //How to get/use mat for right eye
                    //gFace.eyeRight
                    //Color blob goes here
                }
            }
            Utils.matToBitmap(cvManager.faceMat, inputImage);
            return inputImage;
        }
        else if (classifierType == "dnn") {
            Log.d("cvManager", "Starting DNN Detection");
            Utils.bitmapToMat(inputImage, faceMat);
            int cols = faceMat.cols();
            int rows = faceMat.rows();
            Log.d("cvManager", "Preprocessing Height " + faceMat.height() + " | Preprocessing Width: " + faceMat.width());
            Imgproc.resize(faceMat, faceMat, new Size(224, 224));
            Imgproc.cvtColor(faceMat, faceMat, Imgproc.COLOR_RGBA2RGB);
            //blobFromImage(faceMat, 1/255, new Size(96, 96), new Scalar(0, 0, 0, 0 ), true, false);
            Mat blob = blobFromImage(faceMat, 1.0, new Size(224, 224), new Scalar(104.0, 177.0, 123.0));//, false, false);
            dnnClassifier.setInput(blob);
            Mat outputDNN = dnnClassifier.forward();
            Log.d("cvManager", "Postprocessing Height " + outputDNN.height() + " | Preprocessing Width: " + outputDNN.width());
            Log.d("cvManager", "Total output: " + outputDNN.rows());
            ///outputDNN.reshape(1, (int)outputDNN.total()/8);
            for (int n = 0; n < outputDNN.rows(); n++){
                double confidence = outputDNN.get(n, 2)[0];
                Log.d("cvManager", "Confidence: " + confidence);
                if (confidence > 0.1){
                    Log.d("cvManager", "Found a face");
                    //int classId = (int)outputDNN.get(n, 1)[0];
                    int left   = (int)(outputDNN.get(n, 3)[0] * cols);
                    int top    = (int)(outputDNN.get(n, 4)[0] * rows);
                    int right  = (int)(outputDNN.get(n, 5)[0] * cols);
                    int bottom = (int)(outputDNN.get(n, 6)[0] * rows);
                    Imgproc.rectangle(faceMat, new Point(left, top), new Point(right, bottom),
                            new Scalar(0, 255, 0));
                }
            }
            //Imgproc.resize(outputDNN, outputDNN, new Size(inputImage.getWidth(), inputImage.getHeight()));
            //outputDNN.convertTo(outputDNN, CvType.CV_8UC2);
            //Utils.matToBitmap(cvManager.faceMat, inputImage);
            return inputImage;
        } else {
            return null;
        }
    }
}