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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.Marker;
import uk.ac.horizon.aestheticodes.MarkerAction;
import uk.ac.horizon.aestheticodes.MarkerSelection;
import uk.ac.horizon.aestheticodes.MarkerSettings;
import uk.ac.horizon.aestheticodes.Mode;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.CameraManager;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.detect.ViewfinderView;

import java.io.IOException;
import java.util.List;

public class CameraActivity extends FragmentActivity implements SurfaceHolder.Callback, MarkerDetectionListener
{
	private static final String TAG = CameraActivity.class.getName();
	private static final String MODE_PREFIX = "mode_";
	private static final int MAX_PROGRESS = 1000;

	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e(TAG, "Error Initializing OpenCV");
		}
	}

	public static final class ModeFragment extends Fragment
	{
		private Context context;
		private Mode mode;

		public ModeFragment(Context context, Mode mode)
		{
			this.context = context;
			this.mode = mode;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState)
		{
			// Inflate the layout for this fragment
			TextView view = new TextView(context);
			view.setGravity(Gravity.CENTER_HORIZONTAL);
			//view.setRotation(90);
			int id = context.getResources().getIdentifier(MODE_PREFIX + mode.name(), "string", context.getPackageName());
			if (id != 0)
			{
				view.setText(getString(id));
			}
			else
			{
				view.setText(mode.name());
			}

			return view;
		}
	}

	private final MarkerSettings settings = MarkerSettings.getSettings();
	private final MarkerSelection detectingMarkers = new MarkerSelection();
	private SurfaceHolder holder;
	private CameraManager cameraManager;
	private MarkerDetectionThread thread;
	private ViewfinderView viewfinder;
	private ViewPager pager;
	private ProgressBar progress;

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

		setContentView(R.layout.capture);

		cameraManager = new CameraManager(this);

		viewfinder = (ViewfinderView) findViewById(R.id.viewfinder);
		viewfinder.setCameraManager(cameraManager);

		progress = (ProgressBar) findViewById(R.id.progress);
		progress.setMax(MAX_PROGRESS);

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public int getCount()
			{
				return settings.getModes().size();
			}

			@Override
			public Fragment getItem(int position)
			{
				return new ModeFragment(CameraActivity.this, settings.getModes().get(position));
			}
		});
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{

			}

			@Override
			public void onPageSelected(int position)
			{
				thread.setMode(settings.getModes().get(position));
				viewfinder.invalidate();
				if (progress != null)
				{
					detectingMarkers.reset();
					progress.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}
		});

		pager.setCurrentItem(0);
		pager.setOffscreenPageLimit(3);
		pager.setPageTransformer(true, new ZoomOutPageTransformer());
		pager.setPageMargin((int) (getResources().getDisplayMetrics().widthPixels / -1.5));

		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	private void update()
	{
		if (settings.getModes().size() <= 1)
		{
			pager.setVisibility(View.INVISIBLE);
		}
		else
		{
			pager.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

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
				thread = new MarkerDetectionThread(cameraManager, this, settings);
				thread.start();
				if (pager != null)
				{
					thread.setMode(Mode.values()[pager.getCurrentItem()]);
				}

				if (progress != null)
				{
					detectingMarkers.reset();
					progress.setVisibility(View.INVISIBLE);
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
			case R.id.action_switch_camera:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void markersDetected(final List<Marker> markers)
	{
		if(thread == null)
		{
			return;
		}
		if (thread.getMode() == Mode.detect)
		{
			detectingMarkers.addMarkers(markers);

			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (detectingMarkers.hasStarted())
					{
						progress.setVisibility(View.VISIBLE);
						progress.setProgress((int) (MAX_PROGRESS * detectingMarkers.getProgress()));
						progress.setAlpha(1 - detectingMarkers.expiration());
					}
					else if(detectingMarkers.isTimeUp())
					{
						progress.setVisibility(View.INVISIBLE);
					}
				}
			});

			if(detectingMarkers.isTimeUp())
			{
				detectingMarkers.reset();
			}
			else if (detectingMarkers.isFinished())
			{
				Marker marker = detectingMarkers.getLikelyMarker();
				detectingMarkers.reset();

				MarkerAction markerDetail = settings.getMarkers().get(marker.getCodeKey());
				if (markerDetail != null)
				{
					cameraManager.stopPreview();
					if (markerDetail.getShowDetail())
					{
						startActivity(new Intent(this, SettingsActivity.class));
					}
					else
					{
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(markerDetail.getAction())));
					}
				}
				else
				{
					Log.w(TAG, "No details for marker " + marker.getCodeKey());
				}
			}
		}
		else
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
}