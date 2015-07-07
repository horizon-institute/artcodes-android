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

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import uk.ac.horizon.artcodes.scanner.R;

import java.io.IOException;
import java.util.List;


@SuppressWarnings("deprecation")
public class CameraAdapter extends BaseObservable
{
	private static final String THREAD_NAME = "Frame Processor";
	public static boolean deviceNeedsManualAutoFocus = false;

	@BindingAdapter("bind:height")
	public static void bindHeight(View view, Integer height)
	{
		if (height != null)
		{
			view.getLayoutParams().height = height;
			view.getParent().requestLayout();
		}
	}

	@BindingAdapter("bind:snacktext")
	public static void bindSnackText(View view, int string)
	{
		if (string != 0)
		{
			Snackbar.make(view, string, Snackbar.LENGTH_SHORT).show();
		}
	}

	@BindingAdapter("bind:surface")
	public static void bindTextWatcher(SurfaceView view, SurfaceHolder.Callback callback)
	{
		view.getHolder().addCallback(callback);
	}

	private final Context context;
	private Camera camera;
	private int cameraId;
	private int cameraOrientation;
	private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
	private FrameProcessor frameProcessor;
	private HandlerThread cameraThread;
	private SurfaceHolder surface;
	private int surfaceWidth;
	private int surfaceHeight;

	public CameraAdapter(Context context)
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

	@Bindable
	public int getCameraFacingIcon()
	{
		if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			return R.drawable.ic_camera_front_white_24dp;
		}
		return R.drawable.ic_camera_rear_white_24dp;
	}

	public SurfaceHolder.Callback getSurfaceCallback()
	{
		return new SurfaceHolder.Callback()
		{
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
		};
	}

	@Bindable
	public boolean isFacingFront()
	{
		return facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	public void setFrameProcessor(FrameProcessor processor)
	{
		this.frameProcessor = processor;
		if (camera != null && frameProcessor != null)
		{
			frameProcessor.onCameraChanged(camera, surfaceWidth, surfaceHeight);
			frameProcessor.setOrientation(cameraOrientation);
			frameProcessor.setFacing(facing);
		}
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

	private int getOrientation()
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

		float ratioOfSurface = (float) surfaceHeight / surfaceWidth;
		Log.i("", "Surface size: " + surfaceWidth + "x" + surfaceHeight + " (Ratio: " + ratioOfSurface + ")");

		// Step 2: Find camera preview that is best match for estimated surface ratio
		List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size bestFitSoFar = null;
		float ratioDifferenceOfBestFitSoFar = 0;
		for (Camera.Size supportedSize : supportedPreviewSizes)
		{
			float ratio = (float) supportedSize.width / supportedSize.height;
			float ratioDifference = Math.abs(ratio - ratioOfSurface);

			//Log.i("", "Preview Size " + supportedSize.width + "x" + supportedSize.height + " (" + ratio + ")");

			if (bestFitSoFar == null || ratioDifference < ratioDifferenceOfBestFitSoFar)
			{
				bestFitSoFar = supportedSize;
				ratioDifferenceOfBestFitSoFar = ratioDifference;
			}
		}

		if (bestFitSoFar != null)
		{
			// Would only be null if there are no supportedPreviewSizes
			Log.i("", "Selected Preview Size: " + bestFitSoFar.width + "x" + bestFitSoFar.height + " (" + ((float) bestFitSoFar.width / (float) bestFitSoFar.height) + ")");
			parameters.setPreviewSize(bestFitSoFar.width, bestFitSoFar.height);

			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);

			facing = info.facing;

			camera.setParameters(parameters);
			setCameraDisplayOrientation();

			setFrameProcessor(frameProcessor);

			try
			{
				camera.setPreviewDisplay(surface);
			}
			catch (IOException e)
			{
				Log.w("", e.getMessage(), e);
			}

			camera.startPreview();
		}
	}

	private void setCameraDisplayOrientation()
	{
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int degrees = getOrientation();
		//Log.i("", "Orientation = " + degrees + "°");

		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			cameraOrientation = (info.orientation + degrees) % 360;
			cameraOrientation = (360 - cameraOrientation) % 360;  // compensate the mirror
		}
		else
		{  // back-facing
			cameraOrientation = (info.orientation - degrees + 360) % 360;
		}

		camera.setDisplayOrientation(cameraOrientation);
	}

	private void startCamera()
	{
		if (cameraThread == null)
		{
			Log.i("", "Start Camera");
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
