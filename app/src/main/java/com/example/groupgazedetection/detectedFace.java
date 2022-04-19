package com.example.groupgazedetection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;

public class detectedFace {
    Rect faceCords;
    Rect eyeLeftCords;
    Rect eyeRightCords;
    double[] rightPupilCenter;
    double[] leftPupilCenter;
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
    //For Deshawn Tuesday (I helped you w/t some basic setup, here's some to-do for gaze algo).
    //TODO Make and adjust gaze direction from eye coordinates
    //TODO Make visual representation of the current gaze direction on the image/detection screen
    //TODO Make more gaze directions (ex, up, down, centerleft, ect.)
    //TODO Handle cases where both eyes are not looking the same direction (Inconclusive? Or could do center/left?)
    //TODO Help Sal hone in on pupils for all edge cases
    //TODO Download more sample images that look in different directions, that we can find the eyes for

    public detectedFace(Mat inputFace){
        face = inputFace;
        validGaze = true;
        rightPupilCenter = new double[2];
        leftPupilCenter = new double[2];
    }

    public void getThreshold(){
        double elLeftLook = eyeLeft.width()*0.25;
        double elRightLook = eyeLeft.width()*0.75;
        if(leftPupilCenter[0] < elLeftLook){
            //Left Eye Looking Left
        }
        else if(leftPupilCenter[0] > elRightLook){
            //Left Eye Looking Left
        }
        else{
            //Left Eye Looking Center
        }
        double erLeftLook = eyeRight.width()*0.25;
        double erRightLook = eyeRight.width()*0.75;
        if(rightPupilCenter[0] < erLeftLook){
            //Right Eye Looking Left
        }
        else if(leftPupilCenter[0] > erRightLook){
            //Right Eye Looking Right
        }
        else{
            //Right Eye Looking Center
        }
    }

    public gazeDirection determineGaze(){
        //Gaze Direction Code Here

        return gazeDirection.INDETERMINATE;
    }
}