/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.scanner;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.ImageButton;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import uk.ac.horizon.artcodes.scanner.process.ImageProcessorSetting;

@SuppressWarnings("deprecation")
public class Scanner implements SurfaceHolder.Callback
{
	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e("OpenCV", "Error Initializing OpenCV");
		}
	}

	public class CameraInfo
	{
		private int rotation;
		private int imageWidth;
		private int imageHeight;
		private int imageDepth;
		private boolean frontFacing;

		public int getImageWidth()
		{
			return imageWidth;
		}

		public int getImageHeight()
		{
			return imageHeight;
		}

		public int getImageDepth()
		{
			return imageDepth;
		}

		public int getRotation()
		{
			return rotation;
		}

		public boolean isFrontFacing()
		{
			return frontFacing;
		}
	}

	private static final String THREAD_NAME = "Frame Processor";
	// TODO private static boolean deviceNeedsManualAutoFocus = false;
	private final Context context;
	private Camera camera;
	private final CameraInfo info = new CameraInfo();
	private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
	private FrameProcessor frameProcessor;
	private HandlerThread cameraThread;
	private SurfaceHolder surface;
	private int surfaceWidth;
	private int surfaceHeight;

	public Scanner(Context context)
	{
		this.context = context;
	}

	public void flipCamera()
	{
		stopCamera();
		facing = 1 - facing;
		startCamera();
	}

	public void focus(Camera.AutoFocusCallback callback)
	{
		if (camera != null)
		{
			camera.autoFocus(callback);
		}
	}

	public int getCameraCount()
	{
		return Camera.getNumberOfCameras();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		if (holder.getSurface() == null)
		{
			return;
		}
		surface = holder;
		stopCamera();
		surfaceWidth = width;
		surfaceHeight = height;
		startCamera();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		stopCamera();
	}

	public CameraInfo getCameraInfo()
	{
		return info;
	}

	public List<ImageProcessorSetting> setFrameProcessor(FrameProcessor processor)
	{
		this.frameProcessor = processor;
		if(frameProcessor != null)
		{
			if(camera != null)
			{
				camera.addCallbackBuffer(frameProcessor.createBuffer(info, surfaceWidth, surfaceHeight));
				camera.setPreviewCallbackWithBuffer(frameProcessor);
			}
			final List<ImageProcessorSetting> settings = frameProcessor.getSettings();
			settings.add(new ImageProcessorSetting()
			{
				@Override
				public void nextValue()
				{
					flipCamera();
				}

				@Override
				public void updateUI(ImageButton button, TextAnimator textAnimator)
				{

				}
			});
			return settings;
		}
		return Collections.EMPTY_LIST;
	}

	private void createCamera()
	{
		Log.i("Scanner", "Create Camera");
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
				Log.e("Scanner", "Failed to open scanner " + cameraId + ": " + e.getLocalizedMessage(), e);
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
				Log.e("Scanner", "Failed to open scanner " + cameraId + ": " + e.getLocalizedMessage(), e);
			}
		}
	}

	private void openCamera(int cameraId)
	{
		camera = Camera.open(cameraId);
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, cameraInfo);

		Camera.Parameters parameters = camera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
		{
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}
		else if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
		{
			// if FOCUS_MODE_CONTINUOUS_VIDEO is not supported flag that manual auto-focus is needed every few seconds
			Log.w("Scanner", "Camera requires manual focussing");
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			//deviceNeedsManualAutoFocus = true;
		}

		float ratioOfSurface = (float) surfaceHeight / surfaceWidth;
		Log.i("Scanner", "Surface size: " + surfaceWidth + "x" + surfaceHeight + " (Ratio: " + ratioOfSurface + ")");
		Log.i("Scanner", "Format = " + parameters.getPictureFormat());

		// Step 2: Find scanner preview that is best match for estimated surface ratio
		final List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size bestFitSoFar = null;
		float ratioDifferenceOfBestFitSoFar = 0;
		for (Camera.Size supportedSize : supportedPreviewSizes)
		{
			float ratio = (float) supportedSize.width / supportedSize.height;
			float ratioDifference = Math.abs(ratio - ratioOfSurface);

			//Log.i("Scanner", "Preview Size " + supportedSize.width + "x" + supportedSize.height + " (" + ratio + ")");

			if (bestFitSoFar == null || ratioDifference < ratioDifferenceOfBestFitSoFar)
			{
				bestFitSoFar = supportedSize;
				ratioDifferenceOfBestFitSoFar = ratioDifference;
			}
		}

		if (bestFitSoFar != null)
		{
			// Would only be null if there are no supportedPreviewSizes
			Log.i("Scanner", "Selected Preview Size: " + bestFitSoFar.width + "x" + bestFitSoFar.height + " (" + ((float) bestFitSoFar.width / (float) bestFitSoFar.height) + ")");
			parameters.setPreviewSize(bestFitSoFar.width, bestFitSoFar.height);

			camera.setParameters(parameters);

			info.frontFacing = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
			info.imageWidth = parameters.getPreviewSize().width;
			info.imageHeight = parameters.getPreviewSize().height;
			info.imageDepth = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat());
			info.rotation = getRotation(cameraInfo);
			camera.setDisplayOrientation(info.rotation);
			setFrameProcessor(frameProcessor);

			try
			{
				camera.setPreviewDisplay(surface);
			}
			catch (IOException e)
			{
				Log.w("Scanner", e.getMessage(), e);
			}

			camera.startPreview();
		}
	}

	private int getDeviceRotation()
	{
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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

	private int getRotation(Camera.CameraInfo info)
	{
		int degrees = getDeviceRotation();
		int rotation;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			rotation = (info.orientation + degrees) % 360;
			rotation = (360 - rotation) % 360;  // compensate the mirror
		}
		else
		{  // back-facing
			rotation = (info.orientation - degrees + 360) % 360;
		}

		return rotation;
	}

	private void startCamera()
	{
		if (cameraThread == null)
		{
			Log.i("Scanner", "Start Camera");
			cameraThread = new HandlerThread(THREAD_NAME);
			cameraThread.start();
			Handler cameraHandler = new Handler(cameraThread.getLooper());
			cameraHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					createCamera();
					if (camera == null)
					{
						// TODO Display error
					}
				}
			});
		}
		else
		{
			camera.startPreview();
		}
	}

	private void stopCamera()
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
}