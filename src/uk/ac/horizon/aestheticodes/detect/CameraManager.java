/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.detect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CameraManager implements Camera.PreviewCallback
{
	private static final String TAG = CameraManager.class.getName();

	private Camera camera;
	private int cameraId;

	private Rect framingRect;

	private Context context;
	private Bitmap result;

	private byte[] data = null;

	public CameraManager(Context context)
	{
		this.context = context;
	}

	public synchronized Bitmap getResult()
	{
		return result;
	}

	public synchronized void setResult(Bitmap result)
	{
		this.result = result;

	}

	public void release()
	{
		if(camera != null)
		{
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

	public void stopPreview()
	{
		camera.stopPreview();
		camera.setPreviewCallback(null);
	}

	private void createCamera()
	{
		try
		{
			data = null;
			framingRect = null;
			result = null;
			Camera.CameraInfo info = new Camera.CameraInfo();

			for(int index = 0; index < Camera.getNumberOfCameras(); index++)
			{
				Camera.getCameraInfo(index, info);
				if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
				{
					cameraId = index;
					camera = Camera.open(index);
					Camera.Parameters parameters = camera.getParameters();
					List<String> focusModes = parameters.getSupportedFocusModes();
					if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
					{
						parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					}
					camera.setParameters(parameters);

					setCameraDisplayOrientation();
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public synchronized byte[] getData()
	{
		return data;
	}

	public void startPreview(SurfaceHolder holder) throws IOException
	{
		if(camera == null)
		{
			createCamera();
		}

		camera.setPreviewDisplay(holder);
		camera.setOneShotPreviewCallback(this);
		camera.startPreview();
	}

	public synchronized Rect getFramingRect()
	{
		if (framingRect == null)
		{
			if (camera == null)
			{
				return null;
			}
			Point screenResolution = getScreenResolution();
			if (screenResolution == null)
			{
				// Called early, before init even finished
				return null;
			}

			final int size = Math.min(screenResolution.x, screenResolution.y);
			final int leftOffset = (screenResolution.x - size) / 2;
			final int topOffset = (screenResolution.y - size) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + size, topOffset + size);
			Log.d(TAG, "Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}

	public void setCameraDisplayOrientation()
	{
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = manager.getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation)
		{
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

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

	private Point getScreenResolution()
	{
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point theScreenResolution = new Point();
		display.getSize(theScreenResolution);
		return theScreenResolution;
	}

	public Camera.Size getPreviewSize()
	{
		Camera.Parameters afterParameters = camera.getParameters();
		return afterParameters.getPreviewSize();
	}

	@Override
	public synchronized void onPreviewFrame(byte[] bytes, Camera camera)
	{
		if(data == null)
		{
			data = new byte[bytes.length];
			camera.setPreviewCallbackWithBuffer(this);
			Log.i(TAG, "Create data buffer");
		}
		camera.addCallbackBuffer(data);
	}

	public Context getContext()
	{
		return context;
	}
}
