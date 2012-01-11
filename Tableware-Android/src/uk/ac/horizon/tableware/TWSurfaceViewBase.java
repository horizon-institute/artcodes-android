package uk.ac.horizon.tableware;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class TWSurfaceViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "Tableware::SurfaceView";

    private SurfaceHolder	mHolder;
    private VideoCapture    mCamera;
    private volatile Thread	mThread;

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
        synchronized (this) {
        	mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
        	if (mCamera.isOpened()) {
        		this.startThread();
        	} else {
        		this.stopCamera();
        		Log.e(TAG, "Failed to open native camera");
        	}
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        synchronized (this) {
        if (mCamera != null)
            this.stopCamera();
         if (mThread != null)
            this.stopThread();
        }
    }

    protected abstract Bitmap processFrame(VideoCapture capture);
    
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
    
    public void stopProcessing(){
    	stopThread();
    }
    
    public void run() {
        try
        {
        	while (Thread.currentThread() == mThread) {
        		Bitmap bmp = null;
        		
        		synchronized (this) {
        			if (mCamera == null)
        				break;

        			if (!mCamera.grab()) {
        				break;
        			}
        			if (Thread.currentThread().isInterrupted()){
        				throw new InterruptedException("Thread interrupted.");
        			}
        			bmp = processFrame(mCamera);
        		}
        		if (bmp != null && mCamera != null) {
        			Canvas canvas = mHolder.lockCanvas();
        			if (canvas != null) {
        				canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
        				mHolder.unlockCanvasAndPost(canvas);
        			}
        			bmp.recycle();
        		}
        	}
        } catch(Throwable t){
        	Log.i(TAG, "Camera processing stopped due to interrupt");
        }

        Log.i(TAG, "Finishing processing thread");
    }
}