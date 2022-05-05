package com.example.groupgazedetection;

import android.app.Application;
import android.graphics.Bitmap;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class globalStorage extends Application {
    public ArrayList<Mat> compareFrames;
    public Bitmap takenPicture;
}
