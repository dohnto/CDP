package com.example.lightdetector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.content.res.AssetManager;

public class LightDetector {
	// Lower and Upper bounds for range checking in HSV color space
	private Scalar mLowerBound = new Scalar(0);
	private Scalar mUpperBound = new Scalar(0);
	// Minimum contour area in percent for contours filtering
	private static double mMinContourArea = 0.1;
	// Color radius for range checking in HSV color space
	private Scalar mColorRadius = new Scalar(25, 50, 50, 0);
	private Mat mSpectrum = new Mat();
	private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

	// Cache
	Mat mPyrDownMat = new Mat();
	Mat mHsvMat = new Mat();
	Mat mMask = new Mat();
	Mat mDilatedMask = new Mat();
	Mat mHierarchy = new Mat();

	/* METHODS */
	
	/**
	 * 
	 * @param rgbaImage	An image to be processed
	 * @param rgbColor RGB color as a string (e.g. "#FF0000" - Red)
	 * @return List of center points of found blobs.
	 */
	public ArrayList<Point> getBlobCoords(Mat rgbaImage, Scalar rgbColor) {

		// RGB color HSV
		Scalar hsvColor = scalarRgba2Hsv(rgbColor);
		
		this.setHsvColor(hsvColor);
		
		
		Imgproc.pyrDown(rgbaImage, mPyrDownMat);
		Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

		
		Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

		Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
		Imgproc.dilate(mMask, mDilatedMask, new Mat());
		
	
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(mDilatedMask, contours, mHierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		ArrayList<Point> centerPoints = new ArrayList<Point>();

		// Find max contour area
		double maxArea = 0;
		Iterator<MatOfPoint> each = contours.iterator();
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			double area = Imgproc.contourArea(wrapper);
			if (area > maxArea)
				maxArea = area;
		}

		// Filter contours by area and resize to fit the original image size
		mContours.clear();
		each = contours.iterator();
		while (each.hasNext()) {
			MatOfPoint contour = each.next();
			if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
				Core.multiply(contour, new Scalar(4, 4), contour);
				mContours.add(contour);
			}
		}
		
		// Find center of mass of each blob	
		Moments center = new Moments();		
		each = mContours.iterator();
		while(each.hasNext()) {
			Point p = new Point();
			center = Imgproc.moments(each.next());
			
			p.x = center.get_m10()/center.get_m00();
			p.y = center.get_m01()/center.get_m00();
			
			centerPoints.add(p);
		}
		
		return centerPoints;
	}
	
	
	public void setColorRadius(Scalar radius) {
		mColorRadius = radius;
	}

	public void setHsvColor(Scalar hsvColor) {
		double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]
				- mColorRadius.val[0]
				: 0;
		double maxH = (hsvColor.val[0] + mColorRadius.val[0] <= 255) ? hsvColor.val[0]
				+ mColorRadius.val[0]
				: 255;

		mLowerBound.val[0] = minH;
		mUpperBound.val[0] = maxH;

		mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
		mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

		mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
		mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

		mLowerBound.val[3] = 0;
		mUpperBound.val[3] = 255;

		Mat spectrumHsv = new Mat(1, (int) (maxH - minH), CvType.CV_8UC3);

		for (int j = 0; j < maxH - minH; j++) {
			byte[] tmp = { (byte) (minH + j), (byte) 255, (byte) 255 };
			spectrumHsv.put(0, j, tmp);
		}

		Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
	}
	
	/**
	 * @param rgbColor
	 * @return HSV color represented by scalar
	 */
    public Scalar scalarRgba2Hsv(Scalar rgbColor) {
        Mat pointMatHsv = new Mat();
        Mat pointMatRgba = new Mat(50, 50, CvType.CV_8UC3, rgbColor);                       
        Imgproc.cvtColor(pointMatRgba, pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL, 3);                             

        Scalar retval = new Scalar(pointMatHsv.get(10, 10));
        return retval;
    }

	public Mat getSpectrum() {
		return mSpectrum;
	}

	public void setMinContourArea(double area) {
		mMinContourArea = area;
	}

	public List<MatOfPoint> getContours() {
		return mContours;
	}
	
}