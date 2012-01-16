package uk.ac.horizon.tableware;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class TWSurfaceViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    protected static final String TAG = "Tableware::SurfaceView";
 
    protected SurfaceHolder	mHolder;
    protected VideoCapture    mCamera;
    protected volatile Thread	mThread;
    protected boolean mIsSurfaceValid;
        
    public TWSurfaceViewBase(Context context) {
        super(context);
        setHolder();
   }
    
    public TWSurfaceViewBase(Context context, AttributeSet attrs){
    	super(context, attrs);
    	setHolder();
    }
    
    private void setHolder(){
    	mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        synchronized (this) {
        	mIsSurfaceValid = true;
            if (mCamera != null && mCamera.isOpened()) {
                List<Size> sizes = mCamera.getSupportedPreviewSizes();
                                
                int frameWidth = width;
                int frameHeight = height;
                // selecting optimal camera preview size
                double minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                	if (Math.abs(size.height - height) < minDiff) {
                		frameWidth = (int) size.width;
                		frameHeight = (int) size.height;
                		minDiff = Math.abs(size.height - height);
                	}
                }
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, frameWidth);
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, frameHeight);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	mIsSurfaceValid = true;
    	this.startProcessing();
    }

    public void surfaceDestroyed(SurfaceHolder holder){
    	mIsSurfaceValid = false;
    	this.stopProcessing();
    }

    //protected abstract Bitmap processFrame(VideoCapture capture);
    private synchronized void startThread(){
    	if (mThread != null){
    		this.stopThread();
    	}
    	mThread = new Thread(this);
    	//set the thread as a daemon thread to ensure that it is automatically destroyed when there are no non-daemon threads
    	//such as this Android application.
    	mThread.setDaemon(true);
        mThread.start();
    }
    
    private synchronized void stopThread(){
    	Thread tmpThread = mThread;
    	mThread = null;
    	if (tmpThread != null){
    		tmpThread.interrupt();
    	}
    }
    
        
    private synchronized void stopCamera(){
    	mCamera.release();
        mCamera = null;
    }
    
    protected abstract void initData();
    protected abstract void releaseData();
    
    public boolean startProcessing(){
    	boolean started;
    	synchronized (this) {
    		initData();
    		if (mIsSurfaceValid){
    			if (mCamera == null)
    				mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
    			if (mCamera.isOpened()){
    				this.startThread();
    				started = true;
    			}else
    				started = false;
    		}else
    			started = false;
    	}
    	return started;
    }
    
    public void stopProcessing(){
    	synchronized (this) {
    		if (mThread != null)
    			this.stopThread();
    		if (mCamera != null)
    			this.stopCamera();
    		releaseData();
    	}
    }
}