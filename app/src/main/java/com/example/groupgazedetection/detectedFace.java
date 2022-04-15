package com.example.groupgazedetection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class detectedFace {
    Rect faceCords;
    Rect eyeLeftCords;
    Rect eyeRightCords;
    Rect pupilCenter;
    boolean validGaze;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;
    enum gazeDirection{
        LEFT,
        CENTER,
        RIGHT,
        INDETERMINATE
    }

    public detectedFace(Mat inputFace){
        face = inputFace;
        validGaze = true;
    }

    public void getThreshold(){
        //Determine what constitutes left right or center here.

    }

    public gazeDirection determineGaze(){
        //Gaze Direction Code Here

        return gazeDirection.INDETERMINATE;
    }
}