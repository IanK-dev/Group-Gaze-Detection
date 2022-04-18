package com.example.groupgazedetection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class detectedFace {
    Rect faceCords;
    Rect eyeLeftCords;
    Rect eyeRightCords;
    Rect pupilCenter;
    boolean validGaze;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;
    int dgazeX;
    int dgazeY;
    enum gazeDirection{
        LEFT,
        CENTER,
        RIGHT,
        INDETERMINATE
    }

    public detectedFace(Mat inputFace){
        dgazeX = 13;
        dgazeY = 40;
        face = inputFace;
        validGaze = true;
    }

    public void getThreshold(){
        //Determine what constitutes left right or center here.
        //Get 25% of eye x cord (would mean looking left)
        //Get top 75% of eye y cord (would mean looking right)
    }

    public gazeDirection determineGaze(){
        //Gaze Direction Code Here

        return gazeDirection.INDETERMINATE;
    }
}