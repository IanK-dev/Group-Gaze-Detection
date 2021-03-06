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
    double[] rightEyeVals;
    double[] leftEyeVals;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;
    //Assign this variable to the text you want to be on the label
    String outputLabel;
    private double thresholdIntensity;
    SharedPreferences thesePreferences;

    //TODO Forgot to add case when pupil DNE
    public detectedFace(Mat inputFace, Context calledContext){
        face = inputFace;
        validGaze = true;
        rightPupilCenter = new double[2];
        leftPupilCenter = new double[2];
        rightEyeVals = new double[6];
        leftEyeVals = new double[6];
        leftEyeRes = new String[2];
        rightEyeRes = new String[2];
        thresholdIntensity = 0.4;
        thesePreferences = PreferenceManager.getDefaultSharedPreferences(calledContext);
    }

    private void leftEyeResults(){
        //Determine Horizontal Results
        if(leftPupilCenter[0] < (eyeLeft.width()*thresholdIntensity)){
            leftEyeRes[0] = "Left"; leftEyeVals[0] = 1.0;
        }
        else if(leftPupilCenter[0] > (eyeLeft.width()*(1-thresholdIntensity))){
            leftEyeRes[0] = "Right"; leftEyeVals[1] = 1.0;
        }
        else{
            leftEyeRes[0] = "Forward"; leftEyeVals[2] = 1.0;
        }
        //Determine Vertical Results
        if(leftPupilCenter[1] < (eyeLeft.height()*thresholdIntensity)){
            leftEyeRes[1] = "Up"; leftEyeVals[3] = 1.0;
        }
        else if(leftPupilCenter[1] > (eyeLeft.height()*(1 - thresholdIntensity))){
            leftEyeRes[1] = "Down"; leftEyeVals[4] = 1.0;
        }
        else{
            leftEyeRes[1] = "Center"; leftEyeVals[5] = 1.0;
        }
    }

    private void rightEyeResults(){
        //Determine Horizontal Results
        if(rightPupilCenter[0] < (eyeRight.width()*thresholdIntensity)){
            rightEyeRes[0] = "Left"; rightEyeVals[0] = 1.0;
        }
        else if(rightPupilCenter[0] > (eyeRight.width()*(1-thresholdIntensity))){
            rightEyeRes[0] = "Right"; rightEyeVals[1] = 1.0;
        }
        else{
            rightEyeRes[0] = "Forward"; rightEyeVals[2] = 1.0;
        }
        //Determine Vertical Results
        if(rightPupilCenter[1] < (eyeRight.height()*thresholdIntensity)){
            rightEyeRes[1] = "Up"; rightEyeVals[3] = 1.0;
        }
        else if(rightPupilCenter[1] > (eyeRight.height()*(1 - thresholdIntensity))){
            rightEyeRes[1] = "Down"; rightEyeVals[4] = 1.0;
        }
        else{
            rightEyeRes[1] = "Center"; rightEyeVals[5] = 1.0;
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
        if(eyeLeft == null || eyeRight == null || (rightPupilCenter[0] == 0.0 && rightPupilCenter[1] == 0.0) || (leftPupilCenter[0] == 0.0 && leftPupilCenter[1] == 0.0)){
            outputLabel = "Gaze: Inconclusive";
            validGaze = false;
        }
        else{
            outputLabel = confirmGaze();
            validGaze = true;
        }
        //Return
        return outputLabel;
    }
}