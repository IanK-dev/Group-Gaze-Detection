package com.example.groupgazedetection;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class detectedFace {
    Rect faceCords;
    Rect eyeLeftCords;
    Rect eyeRightCords;
    double[] rightPupilCenter;
    double[] leftPupilCenter;
    public String[] leftEyeRes;
    public String[] rightEyeRes;
    boolean validGaze;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;
    //Assign this variable to the text you want to be on the label
    String outputLabel;
    private double thresholdIntensity;
    SharedPreferences thesePreferences;

    public detectedFace(Mat inputFace, Context calledContext){
        face = inputFace;
        validGaze = true;
        rightPupilCenter = new double[2];
        leftPupilCenter = new double[2];
        leftEyeRes = new String[2];
        rightEyeRes = new String[2];
        thresholdIntensity = 0.4;
        thesePreferences = PreferenceManager.getDefaultSharedPreferences(calledContext);
    }

    private void leftEyeResults(){
        //Determine Horizontal Results
        if(leftPupilCenter[0] < (eyeLeft.width()*thresholdIntensity)){
            leftEyeRes[0] = "Left";
        }
        else if(leftPupilCenter[0] > (eyeLeft.width()*(1-thresholdIntensity))){
            leftEyeRes[0] = "Right";
        }
        else{
            leftEyeRes[0] = "Forward";
        }
        //Determine Vertical Results
        if(leftPupilCenter[1] < (eyeLeft.height()*thresholdIntensity)){
            leftEyeRes[1] = "Up";
        }
        else if(leftPupilCenter[1] > (eyeLeft.height()*(1 - thresholdIntensity))){
            leftEyeRes[1] = "Down";
        }
        else{
            leftEyeRes[1] = "Center";
        }
    }

    private void rightEyeResults(){
        //Determine Horizontal Results
        if(rightPupilCenter[0] < (eyeRight.width()*thresholdIntensity)){
            rightEyeRes[0] = "Left";
        }
        else if(rightPupilCenter[0] > (eyeRight.width()*(1-thresholdIntensity))){
            rightEyeRes[0] = "Right";
        }
        else{
            rightEyeRes[0] = "Forward";
        }
        //Determine Vertical Results
        if(rightPupilCenter[1] < (eyeRight.height()*thresholdIntensity)){
            rightEyeRes[1] = "Up";
        }
        else if(rightPupilCenter[1] > (eyeRight.height()*(1 - thresholdIntensity))){
            rightEyeRes[1] = "Down";
        }
        else{
            rightEyeRes[1] = "Center";
        }
    }

    private String confirmGaze(){
        thresholdIntensity = 0.45 * (double) thesePreferences.getInt("gaze_threshold", 8)/(10.0);
        Log.d("detectedFace", "Threshold Result: " + thresholdIntensity);
        leftEyeResults(); rightEyeResults();
        String direction = "Gaze: ";
        if(leftEyeRes[0].equals(rightEyeRes[0])){
            direction += (leftEyeRes[0] + " ");
        }
        else{
            direction += (leftEyeRes[0] + "/" + rightEyeRes[0] + " ");
        }
        if(leftEyeRes[1].equals(rightEyeRes[1])){
            direction += (leftEyeRes[1]);
        }
        else{
            direction += (leftEyeRes[1] + "/" + rightEyeRes[1]);
        }
        return direction;
    }

    public  String printDirection(){
        //Temp testing text
        //determineGaze();
        outputLabel = confirmGaze();
        //Return
        return outputLabel;
    }
}