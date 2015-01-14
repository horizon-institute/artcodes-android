/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
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
package uk.ac.horizon.aestheticodes.core.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.gson.Gson;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.controllers.CameraController;
import uk.ac.horizon.aestheticodes.controllers.ExperienceController;
import uk.ac.horizon.aestheticodes.controllers.ExperienceParser;
import uk.ac.horizon.aestheticodes.controllers.MarkerDetector;
import uk.ac.horizon.aestheticodes.core.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.views.ViewfinderView;

public class ScanActivity extends ActionBarActivity implements ExperienceController.Listener, MarkerDetector.Listener
{
	private static final String TAG = ScanActivity.class.getName();
	private static final String MODE_PREFIX = "mode_";
	private static final String MODE_ACTIVE_PREFIX = "mode_active_";

	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e(TAG, "Error Initializing OpenCV");
		}
	}

	/**
	 * Test if the device displays a software NavBar.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static boolean hasNavBar(Context context)
	{
		boolean hasMenuKey = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
		}
		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		return !hasBackKey && !hasMenuKey;
	}

	protected final ExperienceController experience = new ExperienceController();
	protected CameraController camera;
	protected MarkerDetector detector;
	protected ViewfinderView viewfinder;
	protected TextView modeText;
	private SurfaceHolder holder;
	private View bottomView;
	private PopupMenu modeMenu;
	private View modeMenuButton;

	@Override
	public void experienceSelected(Experience experience)
	{
		Log.i(TAG, "experience updated");

		if (experience != null)
		{
			detector.start();

			setTitle(experience.getName());
		}
		updateMenu();
	}

	public void markerFound(final String markerCode)
	{
		stopCamera();

		Intent intent = getIntent();
		intent.putExtra("marker", markerCode);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void markersDetected(final boolean detected)
	{
		if (detector.getMode() == MarkerDetector.Mode.detect)
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (detected)
					{
						modeText.setTextColor(Color.YELLOW);
					}
					else
					{
						modeText.setTextColor(Color.WHITE);
					}
				}
			});

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

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		camera = new CameraController(this);

		detector = new MarkerDetector(camera, this, experience);

		viewfinder = (ViewfinderView) findViewById(R.id.viewfinder);
		viewfinder.setCamera(camera);
		viewfinder.setDetector(detector);
		viewfinder.addSizeChangedListener(new ViewfinderView.SizeChangedListener()
		{
			@Override
			public void sizeHasChanged()
			{
				layout();
			}
		});

		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
		holder = surfaceView.getHolder();
		holder.addCallback(camera);
		// deprecated setting, but required on Android versions prior to 3.0
		//noinspection deprecation
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		bottomView = findViewById(R.id.bottomView);

		modeMenuButton = findViewById(R.id.modeMenuButton);
		modeText = (TextView) findViewById(R.id.modeText);
		modeMenu = new PopupMenu(this, modeMenuButton);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		startCamera();
		experience.addListener(this);
	}

	public void showMenu(View view)
	{
		modeMenu.show();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		if(intent.getStringExtra("experience") != null)
		{
			try
			{
				Gson gson = ExperienceParser.createParser();
				Experience intentExperience = gson.fromJson(intent.getStringExtra("experience"), Experience.class);
				experience.set(intentExperience);
			}
			catch(Exception e)
			{
				Log.w(TAG, e.getMessage(), e);
			}
		}

		// TODO
		//experience.handleIntent(intent);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		stopCamera();
		experience.removeListener(this);
	}

	/**
	 * Get the height of the software NavBar. Note: This may return a height value even if the device does not display a NavBar, see hasNavBar(Context).
	 *
	 * @return The height of the NavBar
	 */
	private int getNavBarHeight()
	{
		Resources resources = getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0)
		{
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	private String getTextString(String name, String defaultValue)
	{
		int resource = getResources().getIdentifier(name, "string", getPackageName());
		if (resource != 0)
		{
			return getString(resource);
		}
		return defaultValue;
	}

	private void layout()
	{
		Rect frame = camera.getFrame(viewfinder.getWidth(), viewfinder.getHeight());
		if (frame == null)
		{
			return;
		}

		Log.i(TAG, "Frame = " + frame + ", " + viewfinder.getWidth());
		ViewGroup.LayoutParams p = bottomView.getLayoutParams();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
		p.height = surfaceView.getHeight() - frame.bottom;
		p.width = frame.width();
		bottomView.setLayoutParams(p);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasNavBar(this))
		{
			// Don't draw over nav bar
			bottomView.setPadding(0, 0, 0, getNavBarHeight());
		}

		bottomView.requestLayout();
		viewfinder.invalidate();
	}

	private void setMode(MarkerDetector.Mode mode)
	{
		Log.i("", "Set mode to " + mode);
		detector.setMode(mode);
		viewfinder.invalidate();
		modeText.setText(getTextString(MODE_ACTIVE_PREFIX + mode.name(), mode.name()));
		updateMenu();
	}

	private void startCamera()
	{
		if (camera != null)
		{
			try
			{
				camera.start(holder);
				detector.start();
				layout();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	private void stopCamera()
	{
		if (camera != null)
		{
			detector.stop();
			camera.release();
		}
	}

	private void updateMenu()
	{
		if (modeMenu != null)
		{
			modeMenu.getMenu().clear();
			for (final MarkerDetector.Mode amode : MarkerDetector.Mode.values())
			{
				MenuItem item = modeMenu.getMenu().add(getTextString(MODE_PREFIX + amode.name(), amode.name()));
				item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						setMode(amode);
						return true;
					}
				});
				if (amode == detector.getMode())
				{
					item.setEnabled(false);
				}
			}

			if (camera.getCameraCount() > 1)
			{
				MenuItem switchItem = modeMenu.getMenu().add(getString(R.string.action_switch_camera));
				switchItem.setIcon(R.drawable.ic_switch_camera_white_24dp);
				switchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						try
						{
							stopCamera();
							camera.flip();
							viewfinder.invalidate();
							startCamera();
						}
						catch (Exception e)
						{
							Log.e(TAG, e.getMessage(), e);
						}
						return true;
					}
				});
			}

			if (modeMenu.getMenu().hasVisibleItems())
			{
				modeMenuButton.setVisibility(View.VISIBLE);
			}
			else
			{
				modeMenuButton.setVisibility(View.GONE);
			}
		}
	}
}