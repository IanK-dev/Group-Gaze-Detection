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
    private String[] leftEyeRes;
    private String[] rightEyeRes;
    boolean validGaze;
    Mat face;
    Mat eyeLeft;
    Mat eyeRight;
    //Assign this variable to the text you want to be on the label
    String outputLabel;
    String leftEyeStatusText;
    String rightEyeStatusText;
    private double thresholdIntensity;

    enum gazeDirection{
        LEFT,
        CENTER,
        RIGHT,
        INDETERMINATE
    }
    //Alright some of this is pretty rough and needs to be rewritten
    //Group Right Eye and Left Eye, actually discern if they are different, don't try to calculate seperately
    //Add Forward Group
    //Tune Intervals because they're always wrong right now.

    public detectedFace(Mat inputFace){
        face = inputFace;
        validGaze = true;
        rightPupilCenter = new double[2];
        leftPupilCenter = new double[2];
        leftEyeRes = new String[2];
        rightEyeRes = new String[2];
        thresholdIntensity = 0.4;
    }

    public gazeDirection determineGaze(){
        //Gaze Direction Code Here
        //Text View variables
        leftEyeStatusText = "";
        rightEyeStatusText = "";
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
        leftEyeStatusText = widthEyeGaze(leftEyeLR);
        leftEyeStatusText += heightEyeGaze(leftEyeUD);
        rightEyeStatusText = widthEyeGaze(rightEyeLR);
        rightEyeStatusText += heightEyeGaze(rightEyeUD);

        if(leftEyeLR != rightEyeLR || leftEyeUD != rightEyeUD){
            //rightEyeStatusText = "Exception";
        }
        return gazeDirection.INDETERMINATE;
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
            leftEyeRes[1] = "Down";
        }
        else if(leftPupilCenter[1] > (eyeLeft.height()*(1 - thresholdIntensity))){
            leftEyeRes[1] = "Up";
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
            rightEyeRes[1] = "Down";
        }
        else if(rightPupilCenter[1] > (eyeRight.height()*(1 - thresholdIntensity))){
            rightEyeRes[1] = "Up";
        }
        else{
            rightEyeRes[1] = "Center";
        }
    }

    private String confirmGaze(){
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
    /**
     * The method will check to see if eyes are not equal == Exception
     * Check if Center-Center to condense it to Center
     * Else: Returns the direction of left eye since both are same
     * @return The String made is used to set the output of the gaze detection
     */
    public String getGaze(){
        String tempString = "Gaze: ";

        if(!(leftEyeStatusText.equalsIgnoreCase(rightEyeStatusText))){
            //Will set it to right eye since eyes are looking different direction
            tempString += rightEyeStatusText;
        }
        else if(leftEyeStatusText.equalsIgnoreCase("Center/Center")){
            //Quick condenser for Center-Center
            tempString += "Center";
        }
        else{
            tempString += leftEyeStatusText;
        }
        return tempString;
    }
    /**
     * Determines if the right eye is looking
     * {Left, Right, or Center} || {Up, Down, or Center}
     */
    public int rightPupilGaze(double rightEye, double rightEye2){
        int status = 0;
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
                stat = "/Up";
                break;
            case 1:
                stat = "/Down";
                break;
            default:
                stat = "/Center";
                break;
        }

        return stat;
    }

    /**
     * Will use cycle() -- mainly due to the program needing to run so that the output will be right
     * Afterwards, uses getGaze to get the current gaze then returns it out
     */
    public  String printDirection(){
        //Temp testing text
        //determineGaze();
        outputLabel = confirmGaze();
        //Return
        return outputLabel;
    }


}