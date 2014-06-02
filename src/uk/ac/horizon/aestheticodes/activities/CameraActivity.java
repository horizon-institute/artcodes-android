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
package uk.ac.horizon.aestheticodes.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.CameraManager;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.detect.ViewfinderView;
import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.aestheticodes.Marker;

import java.io.IOException;
import java.util.List;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, MarkerDetectionListener
{
	private static final String TAG = CameraActivity.class.getName();

	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e(TAG, "Error Initializing OpenCV");
		}
	}

	private SurfaceHolder holder;
	private CameraManager cameraManager;
	private MarkerDetectionThread thread;
	private ViewfinderView viewfinder;
	private Spinner spinner;

	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			cameraManager.startPreview(holder);
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

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		if (this.holder.getSurface() == null)
		{
			return;
		}
		cameraManager.setOrientation();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		PreferenceManager.setDefaultValues(this, R.xml.settings_defaults, false);

		setContentView(R.layout.capture);

		cameraManager = new CameraManager(this);

		viewfinder = (ViewfinderView) findViewById(R.id.viewfinder);
		viewfinder.setCameraManager(cameraManager);

		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		MenuItem spinnerItem = menu.findItem(R.id.action_mode);
		spinner = (Spinner) spinnerItem.getActionView();
		ArrayAdapter<CharSequence> listAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final String drawPrefix = getString(R.string.draw_prefix);
		for(MarkerDetectionThread.DrawMode mode: MarkerDetectionThread.DrawMode.values())
		{
			int id = getResources().getIdentifier(drawPrefix + mode.name(), "string", getPackageName());
			if(id != 0)
			{
				listAdapter.add(getString(id));
			}
			else
			{
				listAdapter.add(mode.name());
			}
		}
		spinner.setAdapter(listAdapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				thread.setDrawMode(MarkerDetectionThread.DrawMode.values()[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				thread.setDrawMode(MarkerDetectionThread.DrawMode.none);
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "On Resume");
		if (cameraManager != null)
		{
			try
			{
				cameraManager.startPreview(holder);
				thread = new MarkerDetectionThread(cameraManager, this);
				thread.start();
				if(spinner != null)
				{
					thread.setDrawMode(MarkerDetectionThread.DrawMode.values()[spinner.getSelectedItemPosition()]);
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (cameraManager != null)
		{
			cameraManager.release();
			thread.setRunning(false);
			thread = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.action_help:
				startActivity(new Intent(this, GuideActivity.class));
				return true;
//			case R.id.action_track:
//				if (thread != null)
//				{
//					thread.setDetecting(!thread.isDetecting());
//					if (thread.isDetecting())
//					{
//						item.setTitle(getString(R.string.action_track));
//					}
//					else
//					{
//						item.setTitle(getString(R.string.action_detect));
//					}
//				}
//				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void markerDetected(final Marker marker)
	{
		if(thread == null)
		{
			return;
		}
		if (thread.getDrawMode() == MarkerDetectionThread.DrawMode.none)
		{
			thread.setRunning(false);
			this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					// showProgressControls();
					getMarker(marker.getCodeKey());
				}
			});
		}
	}

	private void getMarker(String code)
	{
		DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new DataMarkerWebServices.MarkerDownloadRequestListener()
		{
			public void onMarkerDownloaded(DataMarker marker)
			{
				if (marker != null)
				{
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getUri())));
				}
				else
				{
					Log.e(TAG, "No data found for marker");
					thread = new MarkerDetectionThread(cameraManager, CameraActivity.this);
					thread.start();
				}
			}

			@Override
			public void onMarkerDownloadError()
			{
				Log.e(TAG, "Marker download error. WTF?");
			}
		});
		dtouchMarkerWebServices.executeMarkerRequestUsingCode(code, null, this);
	}

	@Override
	public void tracking(List<Marker> markers)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				viewfinder.invalidate();
			}
		});
	}
}