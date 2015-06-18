/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.scanner.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.Bindable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import uk.ac.horizon.artcodes.scanner.R;

import java.io.IOException;
import java.util.List;


@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback
{
	private static final String THREAD_NAME = "Frame Processor";

	public CameraView(Context context)
	{
		super(context);
		getHolder().addCallback(this);
	}

	public CameraView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		getHolder().addCallback(this);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		getHolder().addCallback(this);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		getHolder().addCallback(this);
	}

	public static boolean deviceNeedsManualAutoFocus = false;

	/**
	 * Test if the device displays a software NavBar.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static boolean hasNavBar(Context context)
	{
		boolean hasMenuKey = true;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
		}
		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		return !hasBackKey && !hasMenuKey;
	}

	private Camera camera;
	private int cameraId;
	private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
	private FrameProcessor frameProcessor;
	private HandlerThread cameraThread;

	public void flipCamera()
	{
		stopCamera();
		facing = 1 - facing;
		startCamera();
	}

	public int getCameraCount()
	{
		return Camera.getNumberOfCameras();
	}

	public int getOrientation()
	{
		WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		int rotation = manager.getDefaultDisplay().getRotation();
		switch (rotation)
		{
			case Surface.ROTATION_0:
				return 0;
			case Surface.ROTATION_90:
				return 90;
			case Surface.ROTATION_180:
				return 180;
			case Surface.ROTATION_270:
				return 270;
		}
		return 0;
	}


	public boolean isFrontCamera()
	{
		return facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

//	@Override
//	public synchronized void onPreviewFrame(byte[] bytes, Camera camera)
//	{
//		if (data == null)
//		{
//			data = new byte[bytes.length];
//			camera.setPreviewCallbackWithBuffer(this);
//			Log.i("", "Create data buffer");
//		}
//		camera.addCallbackBuffer(data);
//	}

	public void cameraFocus(Camera.AutoFocusCallback callback)
	{
		if (camera != null)
		{
			camera.autoFocus(callback);
		}
	}

	public void pauseCamera()
	{
		if (camera != null)
		{
			camera.stopPreview();
		}
	}

	public void startCamera()
	{
		if (cameraThread == null)
		{
			cameraThread = new HandlerThread(THREAD_NAME);
			cameraThread.start();
			Handler cameraHandler = new Handler(cameraThread.getLooper());
			cameraHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					createCamera();
					if(camera == null)
					{
						// TODO
					}
				}
			});
		}
		else
		{
			camera.startPreview();
		}
	}

	public void stopCamera()
	{
		if (camera != null)
		{
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			cameraThread.quit();
			cameraThread = null;
			camera = null;
		}
	}

	public void setFrameProcessor(FrameProcessor processor)
	{
		Log.i("", "Set Frame Processor " + processor);
		this.frameProcessor = processor;
		if(camera != null && frameProcessor != null)
		{
			processor.startProcessing(camera);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		if (holder.getSurface() == null)
		{
			return;
		}
		updateOrientation();
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		startCamera();
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		stopCamera();
	}

	void updateOrientation()
	{
		if (camera != null)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				setCameraDisplayOrientation();
			}
			else
			{
				camera.stopPreview();
				setCameraDisplayOrientation();
				camera.startPreview();
			}
		}
	}

	private void openCamera(int cameraId)
	{
		camera = Camera.open(cameraId);
		this.cameraId = cameraId;

		Camera.Parameters parameters = camera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
		{
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}
		else if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
		{
			// if FOCUS_MODE_CONTINUOUS_VIDEO is not supported flag that manual auto-focus is needed every few seconds
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			deviceNeedsManualAutoFocus = true;
		}

		// Select preview size:
		// Step 1: Estimate the ratio of the surface using the size of the screen
		// (estimate because we can not measure the surface until it is drawn!)
		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

		float reportedDpHeight = displayMetrics.heightPixels / displayMetrics.density;
		float reportedDpWidth = displayMetrics.widthPixels / displayMetrics.density;

		// adjust the reported screen height (because of os bars)
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			if (hasNavBar(getContext()))
			{
				// on Kitkat (with the NAVBAR) the surface is larger than the reported screen size
				reportedDpHeight += 48;
			}
		}
		else
		{
			// in earlier versions the surface is smaller than the reported screen size
			reportedDpHeight -= 25;
		}

		float ratioOfSurface = reportedDpHeight / reportedDpWidth;
		Log.i("EST.SURFACE SIZE", reportedDpWidth + "x" + reportedDpHeight + " (Ratio: " + ratioOfSurface + ", Density: " + displayMetrics.density + ")");

		// Step 2: Find camera preview that is best match for estimated surface ratio
		List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size bestFitSoFar = null;
		float ratioDifferenceOfBestFitSoFar = 0;
		for (Camera.Size supportedSize : supportedPreviewSizes)
		{
			float ratio = (float) supportedSize.width / (float) supportedSize.height;
			float ratioDifference = Math.abs(ratio - ratioOfSurface);

			Log.i("SUP.PREVIEW", supportedSize.width + "x" + supportedSize.height + " (" + ratio + ")");

			if (bestFitSoFar == null || ratioDifference < ratioDifferenceOfBestFitSoFar)
			{
				bestFitSoFar = supportedSize;
				ratioDifferenceOfBestFitSoFar = ratioDifference;
			}
		}
		Log.i("SUP.PREVIEW", "Selected: " + bestFitSoFar.width + "x" + bestFitSoFar.height + " (" + ((float) bestFitSoFar.width / (float) bestFitSoFar.height) + ")");
		parameters.setPreviewSize(bestFitSoFar.width, bestFitSoFar.height);

		camera.setParameters(parameters);
		setCameraDisplayOrientation();

		if(frameProcessor != null)
		{
			frameProcessor.startProcessing(camera);
		}
		try
		{
			camera.setPreviewDisplay(getHolder());
		}
		catch (IOException e)
		{
			Log.w("", e.getMessage(), e);
		}

		camera.startPreview();
	}

	private void createCamera()
	{
		for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++)
		{
			try
			{
				Camera.CameraInfo info = new Camera.CameraInfo();
				Camera.getCameraInfo(cameraId, info);

				if (info.facing == facing)
				{
					openCamera(cameraId);
					return;
				}
			}
			catch (RuntimeException e)
			{
				Log.e("", "Failed to open camera " + cameraId + ": " + e.getLocalizedMessage(), e);
			}
		}

		for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++)
		{
			try
			{
				openCamera(cameraId);
				return;
			}
			catch (RuntimeException e)
			{
				Log.e("", "Failed to open camera " + cameraId + ": " + e.getLocalizedMessage(), e);
			}
		}
	}

	private void setCameraDisplayOrientation()
	{
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int degrees = getOrientation();
		Log.i("", "Orientation = " + degrees + "°");

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		}
		else
		{  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		camera.setDisplayOrientation(result);
	}
}
