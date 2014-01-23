package uk.ac.horizon.aestheticodes;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class TWSurfaceViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable
{
	protected static final String TAG = "Tableware::TWSurfaceViewBase";
	protected SurfaceHolder mHolder;
	protected VideoCapture mCamera;
	protected volatile Thread mThread;
	protected boolean mIsSurfaceValid;

	public TWSurfaceViewBase(Context context)
	{
		super(context);
		setHolder();
	}

	public TWSurfaceViewBase(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setHolder();
	}

	private void setHolder()
	{
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	// Altered to allow for the max camera size to be used (this way on large
	// devices full screen scan mode will be active
	public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height)
	{
		synchronized (this)
		{
			mIsSurfaceValid = true;
			if (mCamera != null && mCamera.isOpened())
			{
				List<Size> sizes = mCamera.getSupportedPreviewSizes();
				// Surface rotated with 90 requires height and width to be
				// flipped.
				// Size size = getOptimalPreviewSize(sizes, width, height);

				int mFrameWidth = width;
				int mFrameHeight = height;

				// selecting optimal camera size
				{

					double minDiff = Double.MAX_VALUE;
					for (Size size : sizes)
					{
						if (Math.abs(size.height - height) < minDiff)
						{
							mFrameWidth = (int) size.width;
							mFrameHeight = (int) size.height;
							minDiff = Math.abs(size.height - height);
						}
					}
				}
				mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
				mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
				//
				// Size size = getOptimalPreviewSize(sizes, height, width);
				// mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, width);//
				// size.width);
				// mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, height);//
				// size.height);
			}
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h)
	{
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes)
		{
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff)
			{
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null)
		{
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes)
			{
				if (Math.abs(size.height - targetHeight) < minDiff)
				{
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		this.stopProcessing();
		mIsSurfaceValid = true;
		this.startProcessing();
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mIsSurfaceValid = false;
		this.stopProcessing();
	}

	// protected abstract Bitmap processFrame(VideoCapture capture);
	private synchronized void startThread()
	{
		if (mThread != null)
		{
			this.stopThread();
		}
		mThread = new Thread(this);
		// set the thread as a daemon thread to ensure that it is automatically
		// destroyed when there are no non-daemon threads
		// such as this Android application.
		mThread.setDaemon(true);
		mThread.start();
	}

	private synchronized void stopThread()
	{
		Thread tmpThread = mThread;
		mThread = null;
		if (tmpThread != null)
		{
			tmpThread.interrupt();
		}
	}

	private synchronized void stopCamera()
	{
		mCamera.release();
		mCamera = null;
	}

	protected abstract void initData();

	protected abstract void releaseData();

	public boolean startProcessing()
	{
		Log.d(TAG, "Start processing");
		boolean started;
		synchronized (this)
		{
			if (mIsSurfaceValid)
			{
				// initData();
				clearCanvas();
				if (mCamera == null)
					mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
				if (mCamera.isOpened())
				{
					this.startThread();
					started = true;
				}
				else
					started = false;
			}
			else
				started = false;
		}
		return started;
	}

	private void clearCanvas()
	{
		Canvas canvas = null;
		try
		{
			canvas = mHolder.lockCanvas();
			if (canvas != null)
			{
				// clear the screen.
				canvas.drawColor(Color.BLACK);
			}
		}
		finally
		{
			if (canvas != null)
				mHolder.unlockCanvasAndPost(canvas);
		}
	}

	public void stopProcessing()
	{
		Log.d(TAG, "Stop processing");
		if (mIsSurfaceValid)
			clearCanvas();
		synchronized (this)
		{
			if (mCamera != null)
				this.stopCamera();
			if (mThread != null)
				this.stopThread();
			releaseData();
		}
	}

}