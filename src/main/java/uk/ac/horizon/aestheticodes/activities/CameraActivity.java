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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.adapters.ExperienceAdapter;
import uk.ac.horizon.aestheticodes.adapters.ModeSelectTransformer;
import uk.ac.horizon.aestheticodes.detect.CameraManager;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.detect.ViewfinderView;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.model.MarkerSelection;

import java.util.List;

public class CameraActivity extends ActionBarActivity implements ExperienceEventListener
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
		private View.OnClickListener onClickListener;


		public ModeFragment()
		{
		}

		public void setOnClickListener(View.OnClickListener onClickListener)
		{
			this.onClickListener = onClickListener;
		}


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View layoutView = inflater.inflate(R.layout.mode_listitem, container, false);
			final TextView view = (TextView) layoutView.findViewById(R.id.modeText);
			final String mode = getArguments().getString("mode");
			int id = getActivity().getResources().getIdentifier(MODE_PREFIX + mode, "string", getActivity().getPackageName());
			if (id != 0)
			{
				view.setText(getString(id));
			}
			else
			{
				view.setText(mode);
			}

			view.setOnClickListener(onClickListener);

			return layoutView;
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
	//private TextView pager_mark;
	private ViewPager pager;
	private ProgressBar progress;
	private RelativeLayout bottomView;
	private MenuItem cameraSwitch;

	/**
	 * Get an OnClickListener that will change a ViewPager to the given position.
	 */
	private View.OnClickListener getOnClickListenerForMode(final ViewPager pager, final int position)
	{
		return new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				pager.setCurrentItem(position);
			}
		};
	}

	void selectItem(final int position)
	{
		Experience selected = experienceManager.list().get(position);
		if (selected != experienceManager.getSelected())
		{
			experienceManager.setSelected(selected);
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

		// earlier versions of Android do not support addOnLayoutChangeListener
		// edited ViewfinderView class for alternative
		/*viewfinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8)
			{
				Log.i(TAG, "Layout!");
				layout();
			}
		});*/
		viewfinder.addSizeChangedListener(new ViewfinderView.SizeChangedListener()
		{
			@Override
			public void sizeHasChanged()
			{
				Log.i(TAG, "Layout!");
				layout();
			}
		});

		progress = (ProgressBar) findViewById(R.id.progress);
		progress.setMax(MAX_PROGRESS);

		//pager_mark = (TextView) findViewById(R.id.pager_mark);

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public int getCount()
			{
				if (experienceManager == null || experienceManager.getSelected() == null)
				{
					return 0;
				}
				return experienceManager.getSelected().getModes().size();
			}

			@Override
			public Fragment getItem(int position)
			{
				Bundle bundle = new Bundle();
				bundle.putString("mode", experienceManager.getSelected().getModes().get(position).name());

				ModeFragment fragment = new ModeFragment();
				fragment.setOnClickListener(getOnClickListenerForMode(pager, position));
				fragment.setArguments(bundle);
				return fragment;
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
				setMode(experienceManager.getSelected().getModes().get(position));
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}

		});

		pager.setCurrentItem(0);
		pager.setOffscreenPageLimit(3);
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			pager.setPageTransformer(true, new ModeSelectTransformer());
		}
		pager.setPageMargin((int) (getResources().getDisplayMetrics().widthPixels / -1.3));
		pager.setClickable(true);

		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
		holder = surfaceView.getHolder();
		holder.addCallback(cameraManager);
		// deprecated setting, but required on Android versions prior to 3.0
		//noinspection deprecation
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		bottomView = (RelativeLayout) findViewById(R.id.bottomView);

		experienceManager = ExperienceManager.get(this);
		experienceManager.addListener(this);
		experienceAdapter = new ExperienceAdapter(getSupportActionBar().getThemedContext(), experienceManager);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(experienceAdapter, new ActionBar.OnNavigationListener()
		{
			@Override
			public boolean onNavigationItemSelected(int position, long l)
			{
				selectItem(position);
				return true;
			}
		});
		experienceManager.load();


		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Log.i(TAG, "Loading experience " + preferences.getString("experience", "uk.ac.horizon.aestheticodes.default"));
		Experience experience = experienceManager.get(preferences.getString("experience", "uk.ac.horizon.aestheticodes.default"));
		if (experience != null)
		{
			experienceManager.setSelected(experience);
		}
	}

	void setItemSelected(final int position)
	{
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	public void experiencesChanged()
	{
		experienceAdapter.notifyDataSetChanged();
	}

	private void setMode(Experience.Mode mode)
	{
		if (thread != null)
		{
			thread.setMode(mode);
		}
		cameraManager.setResult(null);
		viewfinder.invalidate();
		if (progress != null)
		{
			markerSelection.reset();
			progress.setVisibility(View.INVISIBLE);
		}

		pager.setCurrentItem(experienceManager.getSelected().getModes().indexOf(mode), true);
	}

	@Override
	public void experienceSelected(Experience experience)
	{
		Log.i(TAG, "experience updated");
		setTitle(experience.getName());
		pager.getAdapter().notifyDataSetChanged();
		Experience.Mode mode;
		if (thread != null)
		{
			mode = thread.getMode();
		}
		else
		{
			mode = Experience.Mode.detect;
		}
		if (!experience.getModes().contains(mode))
		{
			if (experience.getModes().isEmpty())
			{
				setMode(Experience.Mode.detect);
			}
			else
			{
				setMode(experience.getModes().get(0));
			}
		}

		if (experience.getModes().size() <= 1)
		{
			pager.setVisibility(View.INVISIBLE);
		}
		else
		{
			pager.setVisibility(View.VISIBLE);
		}

		List<Experience> experiences = experienceManager.list();
		int index = experiences.indexOf(experience);
		setItemSelected(index);

		//if (experience.getIcon() != null)
		//{
		//	Log.i(TAG, "Setting icon to " + experience.getIcon());
			//Picasso.with(this).load(experience.getIcon()).into(new ActionBarTarget(this));
		//}

		getPreferences(MODE_PRIVATE).edit().putString("experience", experience.getId()).commit();

		experienceAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		cameraSwitch = menu.findItem(R.id.action_switch_camera);
		updateCameraIcon();

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		startCamera();
		experienceManager.addListener(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	private void startCamera()
	{
		if (cameraManager != null)
		{
			try
			{
				cameraManager.start(holder);
				thread = new MarkerDetectionThread(cameraManager, this, experienceManager);
				thread.start();

				updateCameraIcon();

				if (pager != null)
				{
					thread.setMode(experienceManager.getSelected().getModes().get(pager.getCurrentItem()));
				}

				if (progress != null)
				{
					markerSelection.reset();
					progress.setVisibility(View.INVISIBLE);
				}

				layout();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	private void updateCameraIcon()
	{
		if(cameraSwitch != null && cameraManager != null)
		{
			cameraSwitch.setVisible(cameraManager.getCameraCount() > 1);
			if(cameraManager.isFront())
			{
				cameraSwitch.setIcon(R.drawable.ic_switch_camera_front);
			}
			else
			{
				cameraSwitch.setIcon(R.drawable.ic_switch_camera_back);
			}
		}
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
		pager.invalidate();
		viewfinder.invalidate();
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

	@Override
	protected void onPause()
	{
		super.onPause();
		stopCamera();
		experienceManager.removeListener(this);
	}

	private void stopCamera()
	{
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
			case R.id.action_switch_camera:
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
			case R.id.action_experiences:
				startActivity(new Intent(this, ExperienceListActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void markersFound(final List<Marker> markers)
	{
		if (thread == null)
		{
			return;
		}
		if (thread.getMode() == Experience.Mode.detect)
		{
			markerSelection.addMarkers(markers);

			runOnUiThread(new Runnable()
			{
				@Override
				@TargetApi(Build.VERSION_CODES.HONEYCOMB)
				public void run()
				{
					if (markerSelection.hasStarted())
					{
						progress.setVisibility(View.VISIBLE);
						progress.setProgress((int) (MAX_PROGRESS * markerSelection.getProgress()));
						if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						{
							progress.setAlpha(1 - markerSelection.expiration());
						}
					}
					else if (markerSelection.isTimeUp())
					{
						progress.setVisibility(View.INVISIBLE);
					}
				}
			});

			if (markerSelection.isTimeUp())
			{
				markerSelection.reset();
			}
			else if (markerSelection.isFinished())
			{
				Marker marker = markerSelection.getLikelyMarker();
				markerSelection.reset();

				if (marker != null)
				{
					MarkerAction markerDetail = experienceManager.getSelected().getMarkers().get(marker.getCodeKey());
					if (markerDetail != null)
					{
						cameraManager.stop();
						if (markerDetail.getShowDetail())
						{
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("aestheticodes://" + experienceManager.getSelected().getId() + "/" + marker.getCodeKey())));
						}
						else
						{
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(markerDetail.getAction())));
						}
					}
					else
					{
						Log.w(TAG, "No details for marker " + marker.getCodeKey());

						if (experienceManager.getSelected().canAddMarkerByScanning())
						{
							this.addMarkerDialog(marker.getCodeKey());
						}
					}
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

	private void addMarkerDialog(final String code)
	{
		markerSelection.pause();
		final Context context = this;
		new Handler(getMainLooper()).post(new Runnable()
		{
			@Override
			public void run()
			{
				progress.setVisibility(View.INVISIBLE);
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
}