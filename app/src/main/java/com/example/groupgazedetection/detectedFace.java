package com.example.groupgazedetection;

import android.widget.TextView;

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
    boolean validGaze;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;
    Mat eyeUp;
    Mat eyeDown;
    Mat eyeLeftUp;
    Mat eyeLeftDown;
    enum gazeDirection{
        LEFT,
        CENTER,
        RIGHT,
        INDETERMINATE
    }
    //For Deshawn Tuesday (I helped you w/t some basic setup, here's some to-do for gaze algo).
    //TODO Make and adjust gaze direction from eye coordinates
    //TODO Make visual representation of the current gaze direction on the image/detection screen
    //TODO Make more gaze directions (ex, up, down, centerleft, ect.) ---
    //TODO Handle cases where both eyes are not looking the same direction (Inconclusive? Or could do center/left?) ---
    //TODO Help Sal hone in on pupils for all edge cases
    //TODO Download more sample images that look in different directions, that we can find the eyes for

    public detectedFace(Mat inputFace){
        face = inputFace;
        validGaze = true;
        rightPupilCenter = new double[2];
        leftPupilCenter = new double[2];
    }

    public gazeDirection determineGaze(){
        //Gaze Direction Code Here
        //Text View variables
        String leftEyeStatusText = "";
        String rightEyeStatusText = "";
        int leftEyeLR,rightEyeLR,leftEyeUD, rightEyeUD;

        //Left Eye -- Looking left or right
        double elLeftLook = eyeLeft.width()*0.25;
        double elRightLook = eyeLeft.width()*0.75;

        leftEyeLR = leftPupilGaze(elLeftLook, elRightLook);

        //Left Eye -- Looking Up or Down
        double elUpLook = eyeLeft.height()*0.25;
        double elDownLook = eyeLeft.height()*0.75;

        leftEyeUD = leftPupilGaze(elUpLook, elDownLook);

        //Right eye -- Looking left or right
        double erLeftLook = eyeRight.width()*0.25;
        double erRightLook = eyeRight.width()*0.75;

        rightEyeLR = rightPupilGaze(erLeftLook,erRightLook);

        //Right eye -- Looking Up or Down
        double erUpLook = eyeRight.height()*0.25;
        double erDownLook = eyeRight.height()*0.75;

        rightEyeUD = rightPupilGaze(erUpLook, erDownLook);

        /**
         * Get the String for the left eye for
         * {Left-Up, Left-Down,Left-Center}
         * {Right-Up, Right-Down, Right-Center}
         * {Center-Up, Center-Down, Center-Center}
         */
        leftEyeStatusText = widthEyeGaze(leftEyeLR);
        leftEyeStatusText += heightEyeGaze(leftEyeUD);

        /** Get the String for the right eye for
         * Options Above ^^
         */
        rightEyeStatusText = widthEyeGaze(rightEyeLR);
        rightEyeStatusText += heightEyeGaze(rightEyeUD);

        /**
         * If will check to make sure the eyes are looking the same way
         * If eyes are not looking the same way, then both will never look center at same time
         * Will set @String rightEyeStatusText to "PASS"(temp) so that it's known that
         * both eyes don't need to be looking center to be fine
         */
        if(leftEyeLR != rightEyeLR || leftEyeUD != rightEyeUD){
            rightEyeStatusText = "PASS";
        }



        return gazeDirection.INDETERMINATE;
    }

    /**
     * Determines if the right eye is looking
     * {Left, Right, or Center} || {Up, Down, or Center}
     */
    public int rightPupilGaze(double rightEye, double rightEye2){
        int status;
        if(rightPupilCenter[0] < rightEye){
            //Right eye is looking left or Up
            status = 0;
        }
        else if(rightPupilCenter[0] > rightEye2){
            //Right eye is looking right or down
            status = 1;
        }
        else{
            //Right eye is looking dead center
            status = 2;
        }

        return status;
    }

    /**
     * Determines if the left eye is looking
     * {Left, Right, or Center} || {Up, Down, or Center}
     */
    public int leftPupilGaze(double leftEye, double leftEye2){
        int status;
        if(leftPupilCenter[0] < leftEye){
            //Left eye is looking left or up
            status = 0;
        }
        else if(leftPupilCenter[0] > leftEye2){
            //Left eye is looking right or down
            status =1;
        }
        else{
            //Left eye is looking dead center
            status =2;
        }

        return status;
    }

    /**
     * Determines if (Left or Right) eye is looking
     * {Left, Right, or Center}
     */
    public String widthEyeGaze(int temp){
        String stat = "";
        switch(temp){
            case 0:
                stat = "Left";
                break;
            case 1:
                stat = "Right";
                break;
            default:
                stat = "Center";
                break;
        }
        return stat;
    }

    /**
     * Determines if (Left or Right) eye is looking
     * {Up, Down, or Center}
     */
    public String heightEyeGaze(int temp){
        String stat = "";
        switch(temp){
            case 0:
                stat = "-Up";
                break;
            case 1:
                stat = "-Down";
                break;
            default:
                stat = "-Center";
                break;
        }
        return stat;
    }


}