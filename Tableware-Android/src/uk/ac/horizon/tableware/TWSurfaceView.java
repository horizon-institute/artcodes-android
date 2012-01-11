package uk.ac.horizon.tableware;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

class TWSurfaceView extends TWSurfaceViewBase {
    private static final int NO_OF_TILES = 2;
    private Mat mRgba;
    private Mat mGray;
    private ArrayList<Mat> mComponents;
    private Mat mHierarchy;
    private MarkerDetector markerDetector;
    private OnMarkerDetectedListener markerListener;

    /*Define interface to call back when marker is detected:
     * 
     */
    public interface OnMarkerDetectedListener{
    		void onMarkerDetected(List<DtouchMarker> markers);
    }
    
    public TWSurfaceView(Context context) {
        super(context);
    }
    
    public TWSurfaceView(Context context, AttributeSet attrs){
    	super(context, attrs);
    }
    
    public void setOnMarkerDetectedListener(OnMarkerDetectedListener listener){
    	this.markerListener = listener;
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
           case TablewareActivity.VIEW_MODE_MARKER_DEBUG:
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
    
    private void processFrameForMarkers(VideoCapture capture){
    	//Get original image.
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        //Get gray scale image.
       	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
       	displayRectOnImageSegment(mRgba);
    	//Get image segment to detect marker. 	
    	Mat imgSegmentMat = cloneImageSegmentToDetectMarker(mGray);
    	//apply threshold.
    	Mat thresholdedImgMat = new Mat(imgSegmentMat.size(), imgSegmentMat.type());
    	applyThresholdOnImage(imgSegmentMat,thresholdedImgMat);
    	imgSegmentMat.release();
    	//find markers.
    	List<DtouchMarker> dtouchMarkers = findMarkers(thresholdedImgMat);
    	thresholdedImgMat.release();
    	//Marker detected.
    	if (dtouchMarkers.size() > 0){
    		//display codes on the original image.
    		displayMarkerCodes(mRgba, dtouchMarkers);
    		if (markerListener != null){
    			markerListener.onMarkerDetected(dtouchMarkers);
    		}
    	}
    }
    
    private void processFrameForMarkersDebug(VideoCapture capture){
    	//Get original image.
    	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        //Get gray scale image.
    	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
    	//Get image segment to detect marker.    	
    	Mat imgSegmentMat = cloneImageSegmentToDetectMarker(mGray);
    	//apply threshold.
    	Mat thresholdedImgMat = new Mat(imgSegmentMat.size(), imgSegmentMat.type());
    	ArrayList<Double> localThresholds = applyThresholdOnImage(imgSegmentMat,thresholdedImgMat);
    	imgSegmentMat.release();
    	
      	copyThresholdedImageToRgbImgMat(thresholdedImgMat, mRgba);
      	  	
    	Scalar contourColor = new Scalar(0, 0, 255);
    	Scalar codesColor = new Scalar(255,0,0,255);
    	displayMarkersDebug(thresholdedImgMat, contourColor, codesColor);
    	displayThresholds(mRgba, codesColor, localThresholds);
    	
    	thresholdedImgMat.release();
    }
    
    private Mat cloneImageSegmentToDetectMarker(Mat imgMat){
    	Rect rect = calculateImageSegmentArea(imgMat);
        Mat calculatedImg = imgMat.submat(rect.y, rect.y + rect.height,rect.x,rect.x + rect.width);
    	return calculatedImg.clone();
    }
        
    private Rect calculateImageSegmentArea(Mat imgMat){
       	int x = imgMat.cols() / 4;
        int y = imgMat.rows() / 4;

        int width = imgMat.cols() / 2 ;
        int height = imgMat.rows() / 2;
        
        return new Rect(x, y, width, height);
    }
    
    private void displayRectOnImageSegment(Mat imgMat){
    	Scalar color = new Scalar(255,0,0,255);
    	Rect rect = calculateImageSegmentArea(imgMat);
    	Core.rectangle(imgMat, rect.tl(), rect.br(), color, 3, Core.LINE_AA);
    }
    
    private void copyThresholdedImageToRgbImgMat(Mat thresholdedImgMat, Mat dest){
    	//convert thresholded image segment to RGB. 
    	Mat smallRegionImg = new Mat();
    	Imgproc.cvtColor(thresholdedImgMat, smallRegionImg, Imgproc.COLOR_GRAY2BGRA, 4);
    	//find location of image segment to be replaced in the destination image.
    	Rect rect = calculateImageSegmentArea(dest);
    	Mat destSubmat = dest.submat(rect.y,rect.y + rect.height, rect.x, rect.x + rect.width);
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
    	
    	int tileWidth = (int)srcImgMat.size().height / NO_OF_TILES;
    	int tileHeight = (int)srcImgMat.size().width / NO_OF_TILES;
    	
    	//Split image into tiles and apply threshold on each image tile separately.
    	
    	//process image tiles other than the last one.
    	for (int tileRowCount = 0; tileRowCount < NO_OF_TILES; tileRowCount++){
    		startRow = tileRowCount * tileWidth;
    		if (tileRowCount < NO_OF_TILES - 1)
    			endRow = (tileRowCount + 1) * tileWidth;
    		else
    			endRow = (int)srcImgMat.size().height;
    		
    		for (int tileColCount = 0; tileColCount < NO_OF_TILES; tileColCount++){
    			startCol = tileColCount * tileHeight;
    			if (tileColCount < NO_OF_TILES -1 )
    				endCol = (tileColCount + 1) * tileHeight;
    			else
    				endCol = (int)srcImgMat.size().width;
    			
    			Mat tileThreshold = new Mat();
    			Mat tileMat = srcImgMat.submat(startRow, endRow, startCol, endCol);
    			localThreshold = Imgproc.threshold(tileMat, tileThreshold, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    			Mat copyMat = outputImgMat.submat(startRow, endRow, startCol, endCol);
    			tileThreshold.copyTo(copyMat);
    			tileThreshold.release();
    			localThresholds.add(localThreshold);
    		}
    	}
    	    	
    	return localThresholds;
    }
    
    private List<DtouchMarker> findMarkers(Mat imgMat){
    	List<DtouchMarker> dtouchMarkers = new ArrayList<DtouchMarker>();
    	Mat contourImg = imgMat.clone();
    	//Find blobs using connect component.
    	Imgproc.findContours(contourImg, mComponents, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
    	//No need to use contourImg so release it.
    	contourImg.release();
    	
    	List<Integer> codes = new ArrayList<Integer>();
    	    	
    	for (int i = 0; i < mComponents.size(); i++){
    		//clean this list.
    		codes.clear();
    		if (markerDetector.verifyRoot(i, mComponents.get(i), mHierarchy,imgMat,codes)){
    			//if marker found then add in return list.
    			if (dtouchMarkers == null){
    				dtouchMarkers = new ArrayList<DtouchMarker>();
    			}
    			dtouchMarkers.add(new DtouchMarker(mComponents.get(i),i, codes));
    		}
		}
    	return dtouchMarkers;
    }
    
    private void displayMarkerCodes(Mat imgMat, List<DtouchMarker> markers){
    	Scalar codesColor = new Scalar(255,0,0,255);
    	for (DtouchMarker marker : markers){
    		String code = codeArrayToString(marker.getCode());
    		Point codeLocation = new Point(imgMat.cols() / 4, imgMat.rows()/8);
    		Core.putText(imgMat, code, codeLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
    	}
    }
    
    private void displayMarkersDebug(Mat imgMat, Scalar contourColor, Scalar codesColor){
    	List<DtouchMarker> markers = findMarkers(imgMat);
    	
    	for (DtouchMarker marker : markers){
    		String code = codeArrayToString(marker.getCode());
    		Imgproc.drawContours(mRgba, mComponents, marker.getComponentIndex(), contourColor, 3, 8, mHierarchy, 0);
    		//Get contour location.
    		Point contourLocation = new Point(marker.getComponent().get(0,0));
    		Core.putText(mRgba, code, contourLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
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
