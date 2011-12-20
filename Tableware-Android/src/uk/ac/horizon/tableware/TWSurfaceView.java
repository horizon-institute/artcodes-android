package uk.ac.horizon.tableware;

import java.util.ArrayList;
import java.util.List;

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
import android.util.AttributeSet;
import android.view.SurfaceHolder;

class TWSurfaceView extends TWSurfaceViewBase {
    private static final int NO_OF_TILES = 25; 
	private Mat mRgba;
    private Mat mGray;
    private ArrayList<Mat> mComponents;
    private Mat mHierarchy;
    private MarkerDetector markerDetector;

    public TWSurfaceView(Context context) {
        super(context);
    }
    
    public TWSurfaceView(Context context, AttributeSet attrs){
    	super(context, attrs);
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
    	super.surfaceCreated(holder);
    	initData();
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
    	super.surfaceDestroyed(holder);
    	releaseData();
    }

    @Override
    protected Bitmap processFrame(VideoCapture capture) {
        
    	switch (TablewareActivity.viewMode) {
           case TablewareActivity.VIEW_MODE_MARKER:
	        	processFrameForMarkers(capture);
	            break;
           case TablewareActivity.VIEW_MODE_TILE:
	        	processFrameForTiles(capture);
	            break;
	       case TablewareActivity.VIEW_MODE_EDGES:
	    	   	processFrameForEdges(capture);
	    	   	break;
        }

        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        if (Utils.matToBitmap(mRgba, bmp))
            return bmp;
        bmp.recycle();
        return null;
    }
    
    private void processFrameForTiles(VideoCapture capture){
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        //Get gray scale image.
    	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
    	//Otsu threshold. Threshold values are ignored if Otsu threshold is used.
    	Mat otsuMat = new Mat();
    	//Imgproc.threshold(mGray, otsuMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    	Mat thresholdedImgMat = new Mat(mGray.size(), mGray.type());
    	//Mat thresholdedImgMat = mGray.clone();
    	ArrayList<Double> localThresholds = thresholdImageTiles(mGray,thresholdedImgMat);
    	Imgproc.cvtColor(thresholdedImgMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);    	
    	    	
    	
    	Scalar contourColor = new Scalar(0, 0, 255);
    	Scalar codesColor = new Scalar(255,0,0,255);
    	displayMarkers(thresholdedImgMat, contourColor, codesColor);
    	displayThresholds(mRgba, codesColor, localThresholds);
    	//Imgproc.cvtColor(thresholdedImgMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
    	//displayMarkers(otsuMat, contourColor, codesColor);
    	otsuMat.release();
    	thresholdedImgMat.release();
    }
    
    private void processFrameForMarkers(VideoCapture capture){
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        //Get gray scale image.
    	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
    	//Otsu threshold. Threshold values are ignored if Otsu threshold is used.
    	Mat otsuMat = new Mat();
    	Imgproc.threshold(mGray, otsuMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    	Imgproc.cvtColor(otsuMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
    	Scalar contourColor = new Scalar(0, 0, 255);
    	Scalar codesColor = new Scalar(255,0,0,255);
    	displayMarkers(otsuMat, contourColor, codesColor);
    	otsuMat.release();
    }
    
    private void processFrameForEdges(VideoCapture capture){
	   	Mat cannyMat = new Mat();
	   	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
	   	Imgproc.Canny(mGray, cannyMat, 80, 100);
	   	Imgproc.cvtColor(cannyMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
	   	cannyMat.release();
    }
    
    private ArrayList<Double> thresholdImageTiles(Mat ImgMat, Mat outputImgMat){
    	double localThreshold;
    	int startRow;
    	int endRow;
    	int startCol;
    	int endCol;
    	    	
    	ArrayList<Double> localThresholds = new ArrayList<Double>();
    	
    	int tileWidth = (int)ImgMat.size().height / NO_OF_TILES;
    	int tileHeight = (int)ImgMat.size().width / NO_OF_TILES;
    	
    	//process image tiles other than the last one.
    	for (int tileRowCount = 0; tileRowCount < NO_OF_TILES; tileRowCount++){
    		startRow = tileRowCount * tileWidth;
    		if (tileRowCount < NO_OF_TILES - 1)
    			endRow = (tileRowCount + 1) * tileWidth;
    		else
    			endRow = (int)ImgMat.size().height;
    		
    		for (int tileColCount = 0; tileColCount < NO_OF_TILES; tileColCount++){
    			startCol = tileColCount * tileHeight;
    			if (tileColCount < NO_OF_TILES -1 )
    				endCol = (tileColCount + 1) * tileHeight;
    			else
    				endCol = (int)ImgMat.size().width;
    			
    			Mat tileThreshold = new Mat();
    			Mat tileMat = ImgMat.submat(startRow, endRow, startCol, endCol);
    			localThreshold = Imgproc.threshold(tileMat, tileThreshold, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    			Mat copyMat = outputImgMat.submat(startRow, endRow, startCol, endCol);
    			tileThreshold.copyTo(copyMat);
    			tileThreshold.release();
    			localThresholds.add(localThreshold);
    		}
    	}
    	    	
    	return localThresholds;
    }
    
    private void displayMarkers(Mat inputMat, Scalar contourColor, Scalar codesColor){
    	Mat contourImg = inputMat.clone();
    	//Find blobs using connect component.
    	Imgproc.findContours(contourImg, mComponents, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
    	//No need to use contourImg so release it.
    	contourImg.release();
        
    	List<Integer> codes = new ArrayList<Integer>();
    	Mat contour = null;
    	Point contourLocation = null;
    	
    	for (int i = 0; i < mComponents.size(); i++){
    		contour = mComponents.get(i);
    		//clean this list.
    		codes.clear();
    		if (markerDetector.verifyRoot(i, mComponents.get(i), mHierarchy,inputMat,codes)){
    			String code = codeArrayToString(codes);
    			Imgproc.drawContours(mRgba, mComponents, i, contourColor, 3, 8, mHierarchy, 0);
    			//Get contour location.
    			contourLocation = new Point(contour.get(0,0));
    			Core.putText(mRgba, code, contourLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
    		}
		}
    }
    
    private void displayThresholds(Mat ImgMat, Scalar thresholdColor, ArrayList<Double> thresholds){
    	Point thresholdLocation = new Point(10.0,20.0);
    	int yOffset = 30;
    	int i = 1;
    	for (Double threshold : thresholds){
    		thresholdLocation.y = i * yOffset;    
    		String thresholdString = "(" + threshold.toString() + ")";
    		Core.putText(ImgMat, thresholdString, thresholdLocation, Core.FONT_HERSHEY_COMPLEX, 1, thresholdColor,3);
    		i++;
    	}
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
    
    @Override
    public void run() {
        super.run();
        releaseData();
    }
    
    private void initData(){
    	releaseData();
        
    	synchronized (this) {
            // initialise Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mComponents = new ArrayList<Mat>();
            mHierarchy = new Mat();
        }
    	markerDetector = new MarkerDetector(this.getContext());
    }
    
    private void releaseData(){
        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();
            if (mComponents != null)
            	mComponents.clear();
            if (mHierarchy != null)
            	mHierarchy.release();
            mRgba = null;
            mGray = null;
            mComponents = null;
            mHierarchy = null;
        }
        markerDetector = null;
    }
}
