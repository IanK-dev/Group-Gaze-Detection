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

    public detectedFace(Mat inputFace){
        face = inputFace;
        validGaze = true;
    }

    public void start() {

    }
    public void stop() {

    }

    public void setMinFaceSize(int size) {

    }

    public void release() {

    }

}