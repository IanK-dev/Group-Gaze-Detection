package com.example.groupgazedetection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class detectedFace {
    Rect faceCords;
    Rect eyeLeftCords;
    Rect eyeRightCords;
    Rect pupilCenter;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;

    public void start() {

    }
    public void stop() {

    }

    public void setMinFaceSize(int size) {

    }

    public void release() {

    }

    public Mat createFaceCrop(Rect inputFaceCords){
        faceCords = inputFaceCords;
        return null;
    }

    public Mat createEyeCrop(Rect inputEyeCords){
        return null;
    }
}
