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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;
import uk.ac.horizon.aestheticodes.detect.CameraManager;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.detect.ViewfinderView;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.MarkerCode;
import uk.ac.horizon.aestheticodes.model.MarkerSelection;

import java.util.List;

public class CameraActivity extends ActionBarActivity implements ExperienceEventListener
{
	private static final String TAG = CameraActivity.class.getName();
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
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
		}
		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		return !hasBackKey && !hasMenuKey;
	}

	private final MarkerSelection markerSelection = new MarkerSelection();
	private ExperienceManager experienceManager;
	private ExperienceAdapter experienceAdapter;
	private SurfaceHolder holder;
	private CameraManager cameraManager;
	private MarkerDetectionThread thread;
	private ViewfinderView viewfinder;
	private View bottomView;
	private PopupMenu modeMenu;
	private View modeMenuButton;
	private TextView modeText;
	private MarkerDetectionThread.Mode mode = MarkerDetectionThread.Mode.detect;

	@Override
	public void experienceSelected(Experience experience)
	{
		Log.i(TAG, "experience updated");

		if(experience != null)
		{
			startThread();

			setTitle(experience.getName());

			List<Experience> experiences = experienceAdapter.getExperiences();
			int index = experiences.indexOf(experience);
			getSupportActionBar().setSelectedNavigationItem(index);
		}
		updateMenu();
	}

	public void experiencesChanged()
	{
	}

	@Override
	public void markersFound(final List<MarkerCode> markers)
	{
		if (thread == null)
		{
			return;
		}
		if (mode == MarkerDetectionThread.Mode.detect)
		{
			if(markers.isEmpty())
			{
				modeText.setTextColor(Color.WHITE);
			}
			else
			{
				modeText.setTextColor(Color.YELLOW);
			}

			markerSelection.addMarkers(markers);

			String markerCode = markerSelection.getFoundMarker();
			if (markerCode != null)
			{
				markerSelection.reset();
				Marker marker = experienceManager.getSelected().getMarkers().get(markerCode);
				if (marker != null)
				{
					cameraManager.stop();
					if (marker.getShowDetail())
					{
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("aestheticodes://" + experienceManager.getSelected().getId() + "/" + markerCode)));
					}
					else
					{
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getAction())));
					}
				}
				else
				{
					Log.w(TAG, "No details for marker " + markerCode);

					// TODO if (experienceManager.getSelected().canAddMarkerByScanning())
					//{
					//	this.addMarkerDialog(marker.getCodeKey());
					//}
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

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		cameraManager = new CameraManager(this);

		viewfinder = (ViewfinderView) findViewById(R.id.viewfinder);
		viewfinder.setCameraManager(cameraManager);
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
		holder.addCallback(cameraManager);
		// deprecated setting, but required on Android versions prior to 3.0
		//noinspection deprecation
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		bottomView = findViewById(R.id.bottomView);

		experienceManager = ExperienceManager.get(this);
		experienceManager.addListener(this);
		experienceAdapter = new ExperienceAdapter(getSupportActionBar().getThemedContext(), experienceManager);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(experienceAdapter, new ActionBar.OnNavigationListener()
		{
			@Override
			public boolean onNavigationItemSelected(int position, long l)
			{
				final Experience selected = (Experience) experienceAdapter.getItem(position);
				if (selected != experienceManager.getSelected())
				{
					experienceManager.setSelected(selected);
				}
				return true;
			}
		});

		modeMenuButton = findViewById(R.id.modeMenuButton);
		modeText = (TextView) findViewById(R.id.modeText);
		modeMenu = new PopupMenu(this, modeMenuButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.experiences:
				startActivity(new Intent(this, ExperienceListActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		experienceManager.handleIntent(intent);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		startCamera();
		experienceManager.addListener(this);
		experienceManager.load();
	}

	public void showMenu(View view)
	{
		modeMenu.show();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		stopCamera();
		experienceManager.removeListener(this);
	}

	private void addMarkerDialog(final String code)
	{
		markerSelection.pause();
		final Context context = this;
		new Handler(getMainLooper()).post(new Runnable()
		{
			@Override
			public void run()
			{
				//progress.setVisibility(View.INVISIBLE);
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("New marker " + code);
				builder.setMessage("Do you want to add an action for marker " + code + "?");
				builder.setPositiveButton("Add Action", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						markerSelection.unpause();
						startActivity(new Intent(Intent.ACTION_EDIT, Uri.parse("aestheticodes://" + experienceManager.getSelected().getId() + "/" + code)));
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						markerSelection.unpause();
					}
				});
				builder.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialogInterface)
					{
						markerSelection.unpause();
					}
				});
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
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
		Rect frame = cameraManager.getFrame(viewfinder.getWidth(), viewfinder.getHeight());
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

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasNavBar(this))
		{
			// Don't draw over nav bar
			bottomView.setPadding(0, 0, 0, getNavBarHeight());
		}

		bottomView.requestLayout();
		viewfinder.invalidate();
	}

	private void setMode(MarkerDetectionThread.Mode mode)
	{
		Log.i("", "Set mode to " + mode);
		if (thread != null)
		{
			thread.setMode(mode);
		}
		this.mode = mode;
		cameraManager.setResult(null);
		viewfinder.invalidate();
		markerSelection.reset();
		modeText.setText(getTextString(MODE_ACTIVE_PREFIX + mode.name(), mode.name()));
		updateMenu();
	}

	private void startCamera()
	{
		if (cameraManager != null)
		{
			try
			{
				cameraManager.start(holder);
				startThread();
				markerSelection.reset();
				layout();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	private void startThread()
	{
		if(thread == null)
		{
			if (experienceManager.getSelected() != null)
			{
				thread = new MarkerDetectionThread(cameraManager, this, experienceManager);
				thread.setMode(mode);
				thread.start();
			}
		}
	}

	private void stopThread()
	{
		if(thread != null)
		{
			thread.setRunning(false);
			thread = null;
		}
	}

	private void stopCamera()
	{
		if (cameraManager != null)
		{
			cameraManager.release();
			stopThread();
		}
	}

	private void updateMenu()
	{
		if (modeMenu != null)
		{
			modeMenu.getMenu().clear();
			for (final MarkerDetectionThread.Mode amode : MarkerDetectionThread.Mode.values())
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
				if (amode == mode)
				{
					item.setEnabled(false);
				}
			}

			if (cameraManager.getCameraCount() > 1)
			{
				MenuItem switchItem = modeMenu.getMenu().add(getString(R.string.action_switch_camera));
				switchItem.setIcon(R.drawable.ic_switch_camera);
				switchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						try
						{
							stopCamera();
							cameraManager.flip();
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