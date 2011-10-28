package uk.ac.horizon.tableware;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.SurfaceHolder;

class TWSurfaceView extends TWSurfaceViewBase {
    private Mat mRgba;
    private Mat mGray;
    private Mat mOtsu;
    private Mat mContourImg;
    private Mat mIntermediateMat;
    private ArrayList<Mat> mComponents;
    private Mat mHierarchy;
    private MarkerDetector markerDetector;

    public TWSurfaceView(Context context) {
        super(context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);
        
        markerDetector = new MarkerDetector();
        
        synchronized (this) {
            // initialize Mats before usage
            mGray = new Mat();
            mOtsu = new Mat();
            mContourImg = new Mat();
            mRgba = new Mat();
            mIntermediateMat = new Mat();
            mComponents = new ArrayList<Mat>();
            mHierarchy = new Mat();
        }
    }

    @Override
    protected Bitmap processFrame(VideoCapture capture) {
        switch (TablewareActivity.viewMode) {
        
        case TablewareActivity.VIEW_MODE_GRAY:
        	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            //Get gray scale image.
        	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
        	//Otsu threshold. It is used to reduce grey level image to a binary image. Not sure about
        	//thresh i.e 0 and maximum value i.e 255 used in this function call. Need to be verified.
        	Imgproc.threshold(mGray, mOtsu, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        	Mat mContourImg = mOtsu.clone();
        	//Find blobs using connect component.
        	Imgproc.findContours(mContourImg, mComponents, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        	
        	/*
        	double[][] imageArray = new double[mOtsu.rows()][mOtsu.cols()];
        	for (int i = 0; i < mOtsu.rows(); i ++){
        		for (int j = 0; j < mOtsu.cols(); j++){
        			imageArray[i][j] = mOtsu.get(i, j)[0];
        		}
        	}*/
        		
        	/*
        	String contourSize = "Total Contours: " + mComponents.size() + " in image";
        	Core.putText(test, contourSize, new Point(10,100), Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(255,0,0,255),3);
        	Imgproc.cvtColor(test, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);*/
        	//test.release();
        	
        	/*
        	for (int contourIndex = 0; contourIndex < mComponents.size(); contourIndex++){
        		double[] nodes = mHierarchy.get(0, contourIndex);
        		double next = nodes[0];
        		double prev = nodes[1];
        		double first = nodes[2];
        		double parent = nodes[3];
        	}*/
        	
        	/*
        	for (int contourIndex = 0; contourIndex < mComponents.size(); contourIndex++){
        		double[] nodes = mHierarchy.get(0, contourIndex);
        		double next = nodes[0];
        		double prev = nodes[1];
        		double first = nodes[2];
        		double parent = nodes[3];
        		Mat contourMat = mComponents.get(contourIndex);
        		for (int i = 0; i < contourMat.rows(); i++){
        			double[] values = contourMat.get(i,0);
        			double value = values[0];
        			Scalar color = new Scalar(0, 0, 255);
        			Core.putText(img, text, org, fontFace, fontScale, color)
        		}
        	}*/
        	
        	
        	//Random range = new Random();
        	//Draw contours
        	for (int i = 0; i < mComponents.size(); i++){
        		Scalar color = new Scalar(0, 0, 255);
        		Mat contour = mComponents.get(i);
        		
        		/*
        		for (int j = 0; j < contour.rows(); j++)
        		{
        			double[] cValues = null;
        			for (int k = 0; k < contour.cols(); k++)
        				//double[] data = new double[contour.cols];
        				cValues = contour.get(j, k);
        				double cValue = cValues[0];
        				double[] pixels = mOtsu.get((int)cValues[0], (int)cValues[1]);
        				double pixel = pixels[0];
        		}*/
        		//Imgproc.drawContours(mRgba, mComponents, i, color, 2, 8, mHierarchy, 0);
        		
    			double[] values = mComponents.get(i).get(0, 0);
    			//Core.putText(mRgba, String.valueOf(i), new Point(values[0],values[1]), Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(255,0,0,255),3);
        		//Scalar color = new Scalar(0, 0, 255);
    			//Imgproc.drawContours(mRgba, mComponents, i, color, 2, 8, mHierarchy, 0);
    			List<Integer> codes = new ArrayList<Integer>();
    			
    			if (checkRootNodeRegionColor(mComponents.get(i), mContourImg)){
    				if (markerDetector.verifyRoot(i, mComponents.get(i), mHierarchy,mOtsu,codes)){
    					//Scalar color = new Scalar(range.nextInt(256), range.nextInt(256), range.nextInt(256));
    					//Scalar color = new Scalar(0, 0, 255);
    					String code = codeArrayToString(codes);
    					Imgproc.drawContours(mRgba, mComponents, i, color, 2, 8, mHierarchy, 0);
    					//Core.putText(mRgba, code, new Point(values[0],values[1]), Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(255,0,0,255),3);
    				}
    			}
        	}
        	
        	//String contourSize = "Total Contours: " + mComponents.size() + " in image";
        	//Core.putText(mRgba, contourSize, new Point(10,100), Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(255,0,0,255),3);
        	
        	//Draw contours
        	/*
        	for (int i = 0; i < mComponents.size(); i++){
        		if (markerDetector.verifyRoot(i, mHierarchy)){
        			//Scalar color = new Scalar(range.nextInt(256), range.nextInt(256), range.nextInt(256));
        			Scalar color = new Scalar(0, 0, 255);
        			Imgproc.drawContours(mRgba, mComponents, i, color, 2, 8, mHierarchy, 0);
        		}
        	}*/
        	
        	//Scalar color = new Scalar(0,0,255);
        	//Imgproc.drawContours(mGray, mComponents, -1, color);
        	//Imgproc.drawContours(mRgba, mComponents, -1, color);
        	//mComponentFinder.FindBlobs(mGray);
        	//Transfer image to RGB image to display on screen.
        	//Imgproc.cvtColor(mOtsu, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        	//Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        	
        	/*
        	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
            Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            */
            break;
        case TablewareActivity.VIEW_MODE_RGBA:
            capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            //Core.putText(mRgba, "OpenCV + Android", new Point(10, 100), 3/* CV_FONT_HERSHEY_COMPLEX */, 2, new Scalar(255, 0, 0, 255), 3);
            break;
        case TablewareActivity.VIEW_MODE_CANNY:
            capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
            Imgproc.Canny(mGray, mIntermediateMat, 80, 100);
            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
            break;
        }

        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);

        if (Utils.matToBitmap(mRgba, bmp))
            return bmp;

        bmp.recycle();
        return null;
    }

    private String codeArrayToString(List<Integer> codes){
    	StringBuffer code = new StringBuffer();
    	for(int i = 0; i < codes.size(); i++){
    		if (i > 0)
    			code.append(":");
    		code.append(codes.get(i));
    	}
    	return code.toString();
    }
    
    private Boolean checkRootNodeRegionColor(Mat rootNode, Mat binaryImage){
		//Get the first point of this contour.
		Point point = new Point(rootNode.get(0,0));
		//Get the pixel value of this point from the binary image.
		double pixelValue = binaryImage.get((int)point.x, (int)point.y)[0];
		//check if it is equal to the desired color.
		if (pixelValue == 0.0)
			return true;
		else
			return false;
	}
    
    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();
            if (mOtsu != null)
            	mOtsu.release();
            if (mContourImg != null)
            	mContourImg.release();
            if (mComponents != null)
            	mComponents.clear();
            if (mHierarchy != null)
            	mHierarchy.release();
            mRgba = null;
            mGray = null;
            mIntermediateMat = null;
            mOtsu = null;
            mContourImg = null;
            mComponents = null;
            mHierarchy = null;
        }
    }
}
