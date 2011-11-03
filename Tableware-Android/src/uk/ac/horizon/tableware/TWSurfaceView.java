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
        
        case TablewareActivity.VIEW_MODE_GRAY:
        	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            //Get gray scale image.
        	capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
        	//Otsu threshold. Threshold values are ignored if Otsu threshold is used.
        	Imgproc.threshold(mGray, mOtsu, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        	Mat contourImg = mOtsu.clone();
        	//Find blobs using connect component.
        	Imgproc.findContours(contourImg, mComponents, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        	//No need to use contourImg so release it.
        	contourImg.release();
        	
        	Scalar contourColor = new Scalar(0, 0, 255);
        	Scalar codesColor = new Scalar(255,0,0,255);
        	List<Integer> codes = new ArrayList<Integer>();
        	Mat contour = null;
        	Point contourLocation = null;
        	
        	for (int i = 0; i < mComponents.size(); i++){
        		contour = mComponents.get(i);
        		//clean this list.
        		codes.clear();
        		if (markerDetector.verifyRoot(i, mComponents.get(i), mHierarchy,mOtsu,codes)){
        			String code = codeArrayToString(codes);
        			Imgproc.drawContours(mRgba, mComponents, i, contourColor, 2, 8, mHierarchy, 0);
        			//Get contour location.
        			contourLocation = new Point(contour.get(0,0));
        			Core.putText(mRgba, code, contourLocation, Core.FONT_HERSHEY_COMPLEX, 1, codesColor,3);
        		}
    		}
            break;
        case TablewareActivity.VIEW_MODE_RGBA:
            capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
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
    
    @Override
    public void run() {
        super.run();
        releaseData();
    }
    
    private void initData(){
    	releaseData();
        
    	synchronized (this) {
            // initialize Mats before usage
            mGray = new Mat();
            mOtsu = new Mat();
            mContourImg = new Mat();
            mRgba = new Mat();
            mIntermediateMat = new Mat();
            mComponents = new ArrayList<Mat>();
            mHierarchy = new Mat();
            markerDetector = new MarkerDetector();
        }
    }
    
    private void releaseData(){
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
            markerDetector = null;
        }
    }
}
