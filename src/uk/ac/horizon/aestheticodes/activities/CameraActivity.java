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
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.CameraManager;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.detect.ViewfinderView;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.model.MarkerSelection;
import uk.ac.horizon.aestheticodes.model.MarkerSettings;
import uk.ac.horizon.aestheticodes.model.Mode;

import java.util.List;

public class CameraActivity extends ActionBarActivity implements MarkerDetectionListener
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
			final View layoutView = inflater.inflate(R.layout.item_mode, container, false);
            final TextView view = (TextView)layoutView.findViewById(R.id.modeText);
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

	private final MarkerSettings settings = MarkerSettings.getSettings();
	private final MarkerSelection markerSelection = new MarkerSelection();
	private SurfaceHolder holder;
	private CameraManager cameraManager;
	private MarkerDetectionThread thread;
	private ViewfinderView viewfinder;
	private ViewPager pager;
	private ProgressBar progress;
	private RelativeLayout bottomView;


    /**
     * Get an OnClickListener that will change a ViewPager to the given position.
     */
    private View.OnClickListener getOnClickListenerForMode(final ViewPager pager, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(position);
            }
        };
    }

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_camera);

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
        viewfinder.addSizeChangedListener(new ViewfinderView.SizeChangedListener() {
            @Override
            public void sizeHasChanged() {
                Log.i(TAG, "Layout!");
                layout();
            }
        });

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
				Bundle bundle = new Bundle();
				bundle.putString("mode", settings.getModes().get(position).name());

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
				thread.setMode(settings.getModes().get(position));
				cameraManager.setResult(null);
				viewfinder.invalidate();
				if (progress != null)
				{
					markerSelection.reset();
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
		pager.setPageTransformer(true, new ModeSelectTransformer());
		pager.setPageMargin((int) (getResources().getDisplayMetrics().widthPixels / -1.3));
        pager.setClickable(true);

		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
		holder = surfaceView.getHolder();
		holder.addCallback(cameraManager);
		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		bottomView = (RelativeLayout) findViewById(R.id.bottomView);

		MarkerSettingsHelper.loadSettings(this);
		settingsUpdated();
	}

	private void settingsUpdated()
	{
		Log.i(TAG, "Settings updated");
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

	private void startCamera()
	{
		if (cameraManager != null)
		{
			try
			{
				cameraManager.start(holder);
				thread = new MarkerDetectionThread(cameraManager, this, settings);
				thread.start();
				if (pager != null)
				{
					thread.setMode(Mode.values()[pager.getCurrentItem()]);
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
		if(frame == null)
		{
			return;
		}

		Log.i(TAG,"Frame = " + frame + ", " + viewfinder.getWidth());
		ViewGroup.LayoutParams p = bottomView.getLayoutParams();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview);
        p.height = surfaceView.getHeight()-frame.bottom;
		p.width = frame.width();
		bottomView.setLayoutParams(p);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasNavBar(this))
		{
			// Don't draw over nav bar
			bottomView.setPadding(0,0,0,getNavBarHeight());
		}

		bottomView.requestLayout();
		pager.invalidate();
		viewfinder.invalidate();
	}

    /**
     * Get the height of the software NavBar. Note: This may return a height value even if the device does not display a NavBar, see hasNavBar(Context).
     * @return The height of the NavBar
     */
	private int getNavBarHeight()
	{
		Resources resources = getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}

    /**
     * Test if the device displays a software NavBar.
     * @param context
     * @return
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
			case R.id.action_settings:
				startActivity(new Intent(this, MarkerListActivity.class));
				return true;
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public void markersDetected(final List<Marker> markers)
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
                        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
                        {
                            // Android 2.3/API 10 does not support setAlpha
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

				if(marker != null)
				{
					MarkerAction markerDetail = settings.getMarkers().get(marker.getCodeKey());
					if (markerDetail != null)
					{
						cameraManager.stop();
						if (markerDetail.getShowDetail())
						{
							Intent intent = new Intent(this, MarkerActivity.class);
							intent.putExtra("marker", marker.getCodeKey());
							startActivity(intent);
						}
						else
						{
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(markerDetail.getAction())));
						}
					}
					else
					{
						Log.w(TAG, "No details for marker " + marker.getCodeKey());

                        if (MarkerSettings.getSettings().canAddMarkerByScanning())
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
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.INVISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("New marker " + code);
                builder.setMessage("Do you want to add an action for marker " + code + "?");
                builder.setPositiveButton("Add Action", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        markerSelection.unpause();
                        Intent addMarkerIntent =new Intent(context, MarkerListActivity.class);
                        addMarkerIntent.putExtra("code", code);
                        startActivity(addMarkerIntent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        markerSelection.unpause();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        markerSelection.unpause();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }
}