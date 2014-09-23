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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.CameraManager;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.detect.ViewfinderView;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.model.MarkerSelection;
import uk.ac.horizon.aestheticodes.model.Mode;

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

	public static class ExperienceAdapter extends BaseAdapter
	{
		private final Context context;
		private final LayoutInflater inflater;
		private final ExperienceManager experienceManager;

		public ExperienceAdapter(final Context context, final ExperienceManager experienceManager)
		{
			this.context = context;
			inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			this.experienceManager = experienceManager;
		}

		@Override
		public int getCount()
		{
			return experienceManager.list().size();
		}

		@Override
		public Object getItem(int i)
		{
			return experienceManager.list().get(i);
		}

		@Override
		public long getItemId(final int position)
		{
			return experienceManager.list().get(position).getId().hashCode();
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{
			final Experience experience = experienceManager.list().get(i);
			if (view == null)
			{
				view = inflater.inflate(R.layout.item_experience, viewGroup, false);
			}

			final TextView eventTitle = (TextView) view.findViewById(R.id.experience_title);
			final ImageView iconView = (ImageView) view.findViewById(R.id.experience_icon);
			if (experience.equals(experienceManager.getSelected()))
			{
				eventTitle.setText(experience.getName() + " Markers");

				iconView.setSelected(true);

				Picasso.with(context).cancelRequest(iconView);
				iconView.setImageResource(R.drawable.ic_action_next_item);
			}
			else
			{
				eventTitle.setText(experience.getName());

				iconView.setSelected(false);
				if (experience.getIcon() == null)
				{
					iconView.setVisibility(View.GONE);
				}
				else
				{
					Log.i(TAG, "Loading icon " + experience.getIcon());
					iconView.setVisibility(View.VISIBLE);
					Picasso.with(context).setLoggingEnabled(true);
					Picasso.with(context).cancelRequest(iconView);
					Picasso.with(context).load(experience.getIcon()).placeholder(R.drawable.ic_action_labels_light).into(iconView);
				}
			}

			return view;
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
			final View layoutView = inflater.inflate(R.layout.item_mode, container, false);
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

	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
		{
			selectItem(position);
		}
	}

	private final MarkerSelection markerSelection = new MarkerSelection();
	private ExperienceManager experienceManager;
	private ExperienceAdapter experienceAdapter;
	private SurfaceHolder holder;
	private CameraManager cameraManager;
	private MarkerDetectionThread thread;
	private ViewfinderView viewfinder;
	private TextView pager_mark;
	private ViewPager pager;
	private ProgressBar progress;
	private RelativeLayout bottomView;
	private DrawerLayout drawerLayout;
	private ListView drawerList;

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
		drawerList.setSelection(position);
		drawerLayout.closeDrawer(drawerList);

		Experience selected = experienceManager.list().get(position);
		if (selected == experienceManager.getSelected())
		{
			startActivity(new Intent(Intent.ACTION_EDIT, Uri.parse("aestheticodes://" + selected.getId())));
		}
		else
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

		pager_mark = (TextView) findViewById(R.id.pager_mark);

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
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
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

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.drawer_list);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());

		experienceManager = ExperienceManager.get(this);
		experienceManager.addListener(this);
		experienceAdapter = new ExperienceAdapter(this, experienceManager);
		experienceManager.load();

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Log.i(TAG, "Loading experience " + preferences.getString("experience", "uk.ac.horizon.aestheticodes.default"));
		Experience experience = experienceManager.get(preferences.getString("experience", "uk.ac.horizon.aestheticodes.default"));
		if (experience != null)
		{
			experienceManager.setSelected(experience);
		}

		drawerList.setAdapter(experienceAdapter);
	}

	void setItemSelected(final int position)
	{
		if (drawerList != null)
		{
			drawerList.setSelection(position);
		}
	}

	public void experiencesChanged()
	{
		experienceAdapter.notifyDataSetChanged();
	}

	private void setMode(Mode mode)
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
		Mode mode;
		if(thread != null)
		{
			mode = thread.getMode();
		}
		else
		{
			mode = Mode.detect;
		}
		if(!experience.getModes().contains(mode))
		{
			if(experience.getModes().isEmpty())
			{
				setMode(Mode.detect);
			}
			else
			{
				setMode(experience.getModes().get(0));
			}
		}

		if (experience.getModes().size() <= 1)
		{
			pager.setVisibility(View.INVISIBLE);
			pager_mark.setVisibility(View.INVISIBLE);
		}
		else
		{
			pager.setVisibility(View.VISIBLE);
			pager_mark.setVisibility(View.VISIBLE);
		}

		List<Experience> experiences = experienceManager.list();
		int index = experiences.indexOf(experience);
		setItemSelected(index);

		if (experience.getIcon() != null)
		{
			Log.i(TAG, "Setting icon to " + experience.getIcon());
			Picasso.with(this).load(experience.getIcon()).into(new Target()
			{
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
				{
					getSupportActionBar().setIcon(new BitmapDrawable(getResources(), bitmap));
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable)
				{

				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable)
				{

				}
			});
		}

		getPreferences(MODE_PRIVATE).edit().putString("experience", experience.getId()).commit();

		experienceAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		MenuItem item = menu.findItem(R.id.action_switch_camera);
		item.setVisible(cameraManager.getCameraCount() > 1);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		startCamera();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		experienceManager.removeListener(this);
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

		drawerList.setPadding(0, frame.top, 0, p.height);
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
				if (drawerLayout.isDrawerOpen(drawerList))
				{
					drawerLayout.closeDrawer(drawerList);
				}
				else
				{
					drawerLayout.openDrawer(drawerList);
				}
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
		if (thread.getMode() == Mode.detect)
		{
			markerSelection.addMarkers(markers);

			runOnUiThread(new Runnable()
			{
				@Override
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