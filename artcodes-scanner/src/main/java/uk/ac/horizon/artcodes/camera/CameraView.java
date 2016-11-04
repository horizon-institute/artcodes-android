/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import uk.ac.horizon.artcodes.detect.Detector;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.detect.DetectorSetting;

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView
{
	private static final String THREAD_NAME = "Frame Processor";
	private CameraInfo info;
	private Camera camera;
	private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
	private Detector detector;
	private HandlerThread cameraThread;
	private SurfaceHolder surface;
	private int surfaceWidth;
	private int surfaceHeight;
	private boolean deviceNeedsTapToFocus = false;
	private final Object cameraFocusLock = new Object();
	private boolean cameraIsFocused = false;


	public CameraView(Context context)
	{
		super(context);
		init();
	}

	public CameraView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public CameraView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public void setDetector(Detector processor)
	{
		this.detector = processor;
		if (detector != null)
		{
			if (camera == null)
			{
				createCamera();
			}
			if (camera != null)
			{
				camera.addCallbackBuffer(detector.createBuffer(info, surfaceWidth, surfaceHeight));
				camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback()
				{
					@Override
					public void onPreviewFrame(final byte[] data, final Camera camera)
					{
						detector.setData(data);

						if (deviceNeedsTapToFocus)
						{
							synchronized (cameraFocusLock)
							{
								while (!cameraIsFocused)
								{
									try
									{
										Log.i("Scanner", "Thread " + Thread.currentThread().getName() + " waiting on cameraFocusLock for camera focus");
										cameraFocusLock.wait();
									}
									catch (InterruptedException ignored)
									{
									}
									Log.i("Scanner", "Thread " + Thread.currentThread().getName() + " notified, cameraIsFocused="+cameraIsFocused);
								}
							}
						}

						camera.addCallbackBuffer(data);
					}
				});
			}

			if (Camera.getNumberOfCameras() > 1)
			{
				detector.getSettings().add(new DetectorSetting()
				{
					@Override
					public void nextValue()
					{
						stopCamera();
						facing = 1 - facing;
						startCamera();
					}

					@Override
					public int getIcon()
					{
						switch (facing)
						{
							case Camera.CameraInfo.CAMERA_FACING_BACK:
								return R.drawable.ic_camera_rear_24dp;
							case Camera.CameraInfo.CAMERA_FACING_FRONT:
								return R.drawable.ic_camera_front_24dp;
						}
						return 0;
					}

					@Override
					public int getText()
					{
						switch (facing)
						{
							case Camera.CameraInfo.CAMERA_FACING_BACK:
								return R.string.camera_rear;
							case Camera.CameraInfo.CAMERA_FACING_FRONT:
								return R.string.camera_front;
						}

						return 0;
					}
				});
			}
		}
	}

	private void init()
	{
		getHolder().addCallback(new SurfaceHolder.Callback()
		{
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
			{
				if (holder.getSurface() == null)
				{
					Log.i("scanner", "No surface?");
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
		});
	}

	private void createCamera()
	{
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
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
	}

	private void openCamera(int cameraId)
	{
		Log.i("Scanner", "Device manufacturer: " + android.os.Build.MANUFACTURER + " model: " + android.os.Build.MODEL);

		camera = Camera.open(cameraId);
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, cameraInfo);

		Camera.Parameters parameters = camera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (!android.os.Build.MANUFACTURER.equalsIgnoreCase("SAMSUNG") &&
				focusModes != null &&
				focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
		{
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}
		else if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
		{
			// if FOCUS_MODE_CONTINUOUS_VIDEO is not supported flag that manual auto-focus is needed every few seconds
			Log.w("Scanner", "Camera requires manual focusing");
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			deviceNeedsTapToFocus = true;
		}

		float ratioOfSurface = (float) surfaceHeight / surfaceWidth;
		if (ratioOfSurface < 1)
		{
			ratioOfSurface = 1 / ratioOfSurface;
		}
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

			info = new CameraInfo(cameraInfo, parameters, getDeviceRotation());
			camera.setDisplayOrientation(info.getRotation());
			setDetector(detector);

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

	private void startCamera()
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
			if (cameraThread != null)
			{
				cameraThread.quit();
				cameraThread = null;
			}
			camera = null;
		}
	}

	/* Auto focus methods */

	public boolean deviceNeedsTapToFocus()
	{
		return this.deviceNeedsTapToFocus;
	}

	public void focus(final Runnable callback)
	{

		synchronized (cameraFocusLock)
		{
			cameraIsFocused = false;
		}

		if (camera != null)
		{
			Log.i("Scanner", "Attempting to focus camera.");
			camera.autoFocus(null);
		}

		// set a timeout rather than use the auto focus callback
		// (because the auto focus callback never gets called on some devices)
		Handler h = new Handler(Looper.getMainLooper());
		h.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Log.i("Scanner", "Firing auto focus timeout, notifying on cameraFocusLock.");
				synchronized (cameraFocusLock)
				{
					cameraIsFocused = true;
					cameraFocusLock.notifyAll();
				}
				callback.run();
			}
		}, 1000);
	}
}