/*
 * Aestheticodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.aestheticodes.controllers;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import uk.ac.horizon.aestheticodes.core.R;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraController implements Camera.PreviewCallback, SurfaceHolder.Callback
{
	private static final String TAG = CameraController.class.getName();
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

	private final Context context;
	private Camera camera;
	private int cameraId;
	private Rect framingRect;
	private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
	private byte[] data = null;

	public CameraController(Context context)
	{
		this.context = context;
	}

	public void flip()
	{
		facing = 1 - facing;
	}

	public int getCameraCount()
	{
		return Camera.getNumberOfCameras();
	}

	public synchronized byte[] getData()
	{
		return data;
	}

	public synchronized Rect getFrame(int width, int height)
	{
		if (framingRect == null)
		{
			createFrame(width, height);
		}
		return framingRect;
	}

	public int getPreviewFormat()
	{
		return camera.getParameters().getPreviewFormat();
	}

	public int getRotation()
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

	public Camera.Size getSize()
	{
		Camera.Parameters afterParameters = camera.getParameters();
		return afterParameters.getPreviewSize();
	}

	public boolean isFront()
	{
		return facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	@Override
	public synchronized void onPreviewFrame(byte[] bytes, Camera camera)
	{
		if (data == null)
		{
			data = new byte[bytes.length];
			camera.setPreviewCallbackWithBuffer(this);
			Log.i(TAG, "Create data buffer");
		}
		camera.addCallbackBuffer(data);
	}

	public void performManualAutoFocus(Camera.AutoFocusCallback callback)
	{
		if (camera != null)
		{
			camera.autoFocus(callback);
		}
	}

	public void release()
	{
		if (camera != null)
		{
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

	public void start(SurfaceHolder holder) throws IOException
	{
		if (camera == null)
		{
			createCamera();
		}

		if (camera != null)
		{
			camera.setPreviewDisplay(holder);
			camera.setOneShotPreviewCallback(this);
			camera.startPreview();
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.noCameraTitle);
			builder.setMessage(R.string.noCameraMessage);
			builder.setPositiveButton(R.string.noCameraButton, null);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	public void stop()
	{
		camera.stopPreview();
		camera.setPreviewCallback(null);
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
		try
		{
			start(holder);
		}
		catch (IOException e)
		{
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// empty. Take care of releasing the Camera preview in your activity.
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

	private void createCamera()
	{
		data = null;
		framingRect = null;

		for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++)
		{
			try
			{
				Camera.CameraInfo info = new Camera.CameraInfo();
				Camera.getCameraInfo(cameraId, info);

				if (info.facing == facing)
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
					DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

					float reportedDpHeight = displayMetrics.heightPixels / displayMetrics.density;
					float reportedDpWidth = displayMetrics.widthPixels / displayMetrics.density;

					// adjust the reported screen height (because of os bars)
					if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					{
						if (hasNavBar(context))
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

					return;
				}
			}
			catch (RuntimeException e)
			{
				Log.e(TAG, "Failed to open camera " + cameraId + ": " + e.getLocalizedMessage(), e);
			}
		}

		for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++)
		{
			try
			{
				camera = Camera.open(cameraId);
				this.cameraId = cameraId;

				Camera.CameraInfo info = new Camera.CameraInfo();
				Camera.getCameraInfo(cameraId, info);
				facing = info.facing;

				Camera.Parameters parameters = camera.getParameters();
				List<String> focusModes = parameters.getSupportedFocusModes();
				if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
				{
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				}
				camera.setParameters(parameters);

				setCameraDisplayOrientation();

				return;
			}
			catch (RuntimeException e)
			{
				Log.e(TAG, "Failed to open camera " + cameraId + ": " + e.getLocalizedMessage(), e);
			}
		}
	}

	private void createFrame(int viewWidth, int viewHeight)
	{
		if (camera == null)
		{
			return;
		}
		if (viewWidth == 0 || viewHeight == 0)
		{
			return;
		}

		Camera.Size camera = getSize();
		int degrees = getRotation();
		if (degrees == 0 || degrees == 180)
		{
			int swap = camera.height;
			//noinspection SuspiciousNameCombination
			camera.height = camera.width;
			camera.width = swap;
		}

		final int size = Math.min(camera.width, camera.height);
		final int width = (size * viewWidth / camera.width);
		final int height = (size * viewHeight / camera.height);
		final int leftOffset = (viewWidth - width) / 2;
		final int topOffset = (viewHeight - height) / 2;
		framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
		Log.d(TAG, "Calculated framing rect: " + framingRect);
	}

	private void setCameraDisplayOrientation()
	{
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int degrees = getRotation();
		Log.i(TAG, "Orientation = " + degrees + "°");

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

		framingRect = null;
	}
}
