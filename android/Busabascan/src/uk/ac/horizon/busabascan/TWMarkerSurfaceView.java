package uk.ac.horizon.busabascan;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import uk.ac.horizon.data.HIPreferenceTableware;
import uk.ac.horizon.dtouchMobile.DtouchMarker;
import uk.ac.horizon.dtouchMobile.MarkerDetector;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

class TWMarkerSurfaceView extends TWSurfaceViewBase {
    private Mat mRgba;
    private Mat mGray;
    private ArrayList<Mat> mComponents;
    private Mat mHierarchy;
    private MarkerDetector markerDetector;
    private Mat mMarkerImage;
    private OnMarkerDetectedListener markerListener;
    private Rect markerPosition;
    private HIPreferenceTableware mPreference;
	IntegratedMarkers integratedMarkers = new IntegratedMarkers();
    
    
    /*Define interface to call back when marker is detected:
     * 
     */
    public interface OnMarkerDetectedListener{
    		void onMarkerDetected(List<DtouchMarker> list);
    }
    
    public TWMarkerSurfaceView(Context context) {
        super(context);
    }
    
    public TWMarkerSurfaceView(Context context, AttributeSet attrs){
    	super(context, attrs);
    }
    
    public void setOnMarkerDetectedListener(OnMarkerDetectedListener listener){
    	this.markerListener = listener;
    }
    
    public void setPreference(HIPreferenceTableware preference){
    	mPreference = preference;
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);
    }
    
    private Bitmap processFrameForMarkers(VideoCapture capture, List<DtouchMarker> markers) {
      	switch (TWCameraMainActivity.viewMode) {
           case TWCameraMainActivity.VIEW_MODE_MARKER:
	        	processFrameForMarkersFull(capture, markers);
	            break;
           case TWCameraMainActivity.VIEW_MODE_MARKER_DEBUG:
        	   processFrameForMarkersDebug(capture);
	            break;
	       default:
	    	    break;
        }

        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        if (Utils.matToBitmap(mRgba, bmp))
            return bmp;
        bmp.recycle();
        return null;
    }
    
    private Bitmap displayDetectedMarker(VideoCapture capture, Mat markerImage){
    	//Get original image.
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
    	displayRectOnImageSegment(mRgba,true);
    	displayMarkerImage(mMarkerImage, mRgba);
    	
    	 Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
         if (Utils.matToBitmap(mRgba, bmp))
             return bmp;
         bmp.recycle();
         return null;
    }
    
    private void processFrameForMarkersFull(VideoCapture capture, List<DtouchMarker> markers){
    	//Get original image.
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        //Get gray scale image.
       	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
       	//Get image segment to detect marker.
    	markerPosition = calculateImageSegmentArea(mGray);
       	Mat imgSegmentMat = cloneMarkerImageSegment(mGray);
    	//apply threshold.
    	Mat thresholdedImgMat = new Mat(imgSegmentMat.size(), imgSegmentMat.type());
    	applyThresholdOnImage(imgSegmentMat,thresholdedImgMat);
    	imgSegmentMat.release();
    	//find markers.
    	boolean markerFound = findMarkers(thresholdedImgMat, markers);
    	thresholdedImgMat.release();
    	//Marker detected.
    	if (markerFound){
    		setMarkerDetected(true);
    		//if marker is found then copy the marker image segment.
    		mMarkerImage = cloneMarkerImageSegment(mRgba);
    		//display codes on the original image.
    		//displayMarkerCodes(mRgba, markers);
    		//display rect with indication that a marker is identified.
    		displayRectOnImageSegment(mRgba,true);
    		//display marker image
    		displayMarkerImage(mMarkerImage, mRgba);
    	}else
    		displayRectOnImageSegment(mRgba,false);
    }
    
    private void processFrameForMarkersDebug(VideoCapture capture){
    	//Get original image.
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        //Get gray scale image.
    	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
    	
    	//Get image segment to detect marker.    	
    	Mat imgSegmentMat = cloneMarkerImageSegment(mGray);
    	Mat thresholdedImgMat = new Mat(imgSegmentMat.size(), imgSegmentMat.type());
    	applyThresholdOnImage(imgSegmentMat,thresholdedImgMat);
      	copyThresholdedImageToRgbImgMat(thresholdedImgMat, mRgba);
    	
    	Scalar contourColor = new Scalar(0, 0, 255);
    	Scalar codesColor = new Scalar(255,0,0,255);

    	displayMarkersDebug(thresholdedImgMat, contourColor, codesColor);
    	//displayThresholds(mRgba, codesColor, localThresholds);
		displayRectOnImageSegment(mRgba,false);

    }
    
    private Mat cloneMarkerImageSegment(Mat imgMat){
    	Rect rect = calculateImageSegmentArea(imgMat);
        //Mat calculatedImg = imgMat.submat(rect.x, rect.x + rect.width,rect.y,rect.y + rect.height);
        Mat calculatedImg = imgMat.submat(rect.y, rect.y + rect.height,rect.x,rect.x + rect.width);
    	return calculatedImg.clone();
    }
    
    /**
     * Returns square area for image segment.
     * @param imgMat Source image from which to compute image segment.
     * @return square area which contains image segment.
     */
    private Rect calculateImageSegmentArea(Mat imgMat){
        int width = imgMat.cols();
        int height = imgMat.rows();
        
        int imgWidth = width / 2;
    	int imgHeight = height / 2;
    	        
        //find the origin  in the source image.
        int x = width / 4;
        int y = height / 4;
        
        return new Rect(x, y, imgWidth, imgHeight);
    }
    
    private void displayRectOnImageSegment(Mat imgMat, boolean markerFound){
    	Scalar color = null;
    	if (markerFound)
    		color = new Scalar(0,255,0,255);
    	else
    		color = new Scalar(255,160,36,255);
    	Rect rect = calculateImageSegmentArea(imgMat);
    	Core.rectangle(imgMat, rect.tl(), rect.br(), color, 3, Core.LINE_AA);
    }
    
    private void displayMarkerImage(Mat srcImgMat, Mat destImageMat){
    	//find location of image segment to be replaced in the destination image.
    	Rect rect = calculateImageSegmentArea(destImageMat);
    	//Mat destSubmat = destImageMat.submat(rect.x,rect.x + rect.width, rect.y, rect.y + rect.height);
    	Mat destSubmat = destImageMat.submat(rect.y,rect.y + rect.height, rect.x, rect.x + rect.width);
    	//copy image.
    	srcImgMat.copyTo(destSubmat);
    }
    
    private void copyThresholdedImageToRgbImgMat(Mat thresholdedImgMat, Mat dest){
    	//convert thresholded image segment to RGB. 
    	Mat smallRegionImg = new Mat();
    	Imgproc.cvtColor(thresholdedImgMat, smallRegionImg, Imgproc.COLOR_GRAY2BGRA, 4);
    	//find location of image segment to be replaced in the destination image.
    	Rect rect = calculateImageSegmentArea(dest);
    	//Mat destSubmat = dest.submat(rect.x,rect.x+rect.width,rect.y, rect.y+rect.height);
    	Mat destSubmat = dest.submat(rect.y,rect.y+rect.height,rect.x, rect.x+rect.width);
    	//copy image.
    	smallRegionImg.copyTo(destSubmat);
    	smallRegionImg.release();
    }
    
    
    private ArrayList<Double> applyThresholdOnImage(Mat srcImgMat, Mat outputImgMat){
    	double localThreshold;
    	int startRow;
    	int endRow;
    	int startCol;
    	int endCol;
    	    	
    	ArrayList<Double> localThresholds = new ArrayList<Double>();
    	
    	int numberOfTiles = mPreference.getNumberOfTiles();
    	int tileWidth = (int)srcImgMat.size().height / numberOfTiles;
    	int tileHeight = (int)srcImgMat.size().width / numberOfTiles;
    	
    	//Split image into tiles and apply threshold on each image tile separately.
    	
    	//process image tiles other than the last one.
    	for (int tileRowCount = 0; tileRowCount < numberOfTiles; tileRowCount++){
    		startRow = tileRowCount * tileWidth;
    		if (tileRowCount < numberOfTiles - 1)
    			endRow = (tileRowCount + 1) * tileWidth;
    		else
    			endRow = (int)srcImgMat.size().height;
    		
    		for (int tileColCount = 0; tileColCount < numberOfTiles; tileColCount++){
    			startCol = tileColCount * tileHeight;
    			if (tileColCount < numberOfTiles -1 )
    				endCol = (tileColCount + 1) * tileHeight;
    			else
    				endCol = (int)srcImgMat.size().width;
    			
    			Mat tileThreshold = new Mat();
    			Mat tileMat = srcImgMat.submat(startRow, endRow, startCol, endCol);
    			//localThreshold = Imgproc.threshold(tileMat, tileThreshold, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    			//RNM: Adaptive threshold rules!
    			localThreshold = 0x80; Imgproc.adaptiveThreshold(tileMat, tileThreshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 91, 2);
    			Mat copyMat = outputImgMat.submat(startRow, endRow, startCol, endCol);
    			tileThreshold.copyTo(copyMat);
    			tileThreshold.release();
    			localThresholds.add(localThreshold);
    		}
    	}
    	    	
    	return localThresholds;
    }
    
    private boolean findMarkers(Mat imgMat, List<DtouchMarker> markers){
    	boolean markerFound = false;
    	//holds all the markers identified in the camera.
    	List<DtouchMarker> markersDetected = new ArrayList<DtouchMarker>();
    	Mat contourImg = imgMat.clone();
    	//Find blobs using connect component.
    	Imgproc.findContours(contourImg, mComponents, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
    	//No need to use contourImg so release it.
    	contourImg.release();
    	
    	List<Integer> code = new ArrayList<Integer>();
    	    	
    	for (int i = 0; i < mComponents.size(); i++){
    		//clean this list.
    		code.clear();
    		if (markerDetector.verifyRoot(i, mComponents.get(i), mHierarchy,code)){
    			//if marker found then add in the list.
    			DtouchMarker markerDetected = new DtouchMarker();
    			markerDetected.setCode(code);
    			markerDetected.setComponentIndex(i);
    			markersDetected.add(markerDetected);
    			markers.add(markerDetected);
    		}
		}
    	//if markers are found then decide which marker code occurred most. 
    	if (markersDetected.size() > 0){
    		markerFound = true;
    		//DtouchMarker markerSelected = markerDetector.compareDetectedMarkers(markersDetected);
    		//if (markerSelected != null){
    			//markers.setCode(markerSelected.getCode());
    			//markers.setComponentIndex(markerSelected.getComponentIndex());
    			//markerFound = true;
    		//}
    	}
    	return markerFound;
    }
    
    //private void displayMarkerCodes(Mat imgMat, DtouchMarker marker){
    //	Scalar codesColor = new Scalar(255,0,0,255);
    //	String code = codeArrayToString(marker.getCode());
	//	Point codeLocation = new Point(imgMat.cols() / 4, imgMat.rows()/8);
	//	Core.putText(imgMat, code, codeLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
    //	
    //	/*for (DtouchMarker marker : markers){
    //		String code = codeArrayToString(marker.getCode());
    //		Point codeLocation = new Point(imgMat.cols() / 4, imgMat.rows()/8);
    //		Core.putText(imgMat, code, codeLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
    //	}*/
    //}
    
    private void displayMarkersDebug(Mat imgMat, Scalar contourColor, Scalar codesColor){
    	List<DtouchMarker> markers= new ArrayList<DtouchMarker>();
    	boolean markerFound = findMarkers(imgMat, markers);
    	int m=0;
    	if (markerFound){    	
    	    for(DtouchMarker marker : markers)
    	    {
	    		String code = codeArrayToString(marker.getCode());
	    		Point codeLocation = new Point((imgMat.cols() / 4), (imgMat.rows()/8)+(m*32));
	    		Core.putText(mRgba, code, codeLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
	 
	    		Rect rect = calculateImageSegmentArea(mRgba);
	        	Mat destSubmat = mRgba.submat(rect.y,rect.y + rect.height, rect.x, rect.x + rect.width);
	    		Imgproc.drawContours(destSubmat, mComponents, marker.getComponentIndex(), contourColor, 3, 8, mHierarchy, 0);
	    		m++;
	    	}
    	}
    	/*
    	for (DtouchMarker marker : markers){
    		String code = codeArrayToString(marker.getCode());
    		//Point codeLocation = new Point(imgMat.cols() / 4, imgMat.rows()/8);
    		Core.putText(mRgba, code, codeLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
    		Imgproc.drawContours(mRgba, mComponents, marker.getComponentIndex(), contourColor, 3, 8, mHierarchy, 0);
    	}*/
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
    
    public void run() {
        try
        {
        	initData();
        	while (Thread.currentThread() == mThread) {
        		Bitmap bmp = null;
        		List<DtouchMarker> dtouchMarkers = new ArrayList<DtouchMarker>();

        		synchronized (this) {
        			if (mCamera == null)
        				break;

        			if (!mCamera.grab()) {
        				break;
        			}
        			if (Thread.currentThread().isInterrupted()){
        				throw new InterruptedException("Thread interrupted.");
        			}
        			
            		if(!integratedMarkers.any()){
						bmp = processFrameForMarkers(mCamera, dtouchMarkers);
        			}else{
        				bmp = displayDetectedMarker(mCamera,mMarkerImage); 
        			}
        		}
        		if (bmp != null && mCamera != null) {
        			Canvas canvas = mHolder.lockCanvas();
        			if (canvas != null) {
        				//RNM rotate to receive landscape camera in portrait display
        				canvas.rotate(90);
        				//canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
        				//RNM Position empirically.
        				float left = 20;
        				float top = -480;
        				canvas.drawBitmap(bmp, left, top, null);
        				mHolder.unlockCanvasAndPost(canvas);
        			}
        			bmp.recycle();
        		}
        		
        		integratedMarkers.integrate(dtouchMarkers);
        	  	
        		if (integratedMarkers.any()){
        			if (markerListener != null){
        				markerListener.onMarkerDetected(integratedMarkers.get());
            		}
        		}
        	}
        } catch(InterruptedException t){
        	Log.i(TAG, "Camera processing stopped due to interrupt");
        }
        
        Log.i(TAG, "Finishing processing thread");
        releaseData();
    }    
    
	@Override
    protected void initData(){
    	releaseData();
        
    	synchronized (this) {
            // initialise Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mMarkerImage = new Mat();
            mComponents = new ArrayList<Mat>();
            mHierarchy = new Mat();
        }
    	markerDetector = new MarkerDetector(this.getContext(), new HIPreferenceTableware(this.getContext()));
    }
    
    @Override
    protected void releaseData(){
        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mMarkerImage != null)
            	mMarkerImage.release();
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
    
    private void setMarkerDetected(boolean detected){
    }
    
    public Rect getMarkerPosition(){
    	return markerPosition;
    }
    
    public void stopDisplayingDetectedMarker(){
    	setMarkerDetected(false);
    }
 
}