package com.example.groupgazedetection;

import android.app.Application;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class globalStorage extends Application {
    public ArrayList<Mat> compareFrames;
}
