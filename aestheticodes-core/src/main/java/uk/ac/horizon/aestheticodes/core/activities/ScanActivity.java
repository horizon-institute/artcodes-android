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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
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
	protected RelativeLayout bottomView;
	private View settingsMenuButton;
	private View settingsMenu;

	@Override
	public void experienceSelected(Experience experience)
	{
		Log.i(TAG, "experience updated");

		if (experience != null)
		{
			detector.start();

			setTitle(experience.getName());
		}
	}

	public void flipCamera(View view)
	{
		stopCamera();
		camera.flip();
		viewfinder.invalidate();
		startCamera();
		updateMenu();
	}

	public void hideMenu(View view)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = (settingsMenuButton.getLeft() + settingsMenuButton.getRight()) / 2;
			final int cy = ((settingsMenuButton.getTop() + settingsMenuButton.getBottom()) / 2) - settingsMenu.getTop();

			Log.i("", "Circle center " + cx + ", " + cy + " width " + bottomView.getWidth());

			final Animator anim = ViewAnimationUtils.createCircularReveal(settingsMenu, cx, cy, bottomView.getWidth(), 0);

			anim.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					super.onAnimationEnd(animation);
					settingsMenu.setVisibility(View.INVISIBLE);
					settingsMenuButton.setVisibility(View.VISIBLE);
				}
			});

			anim.start();
		}
		else
		{
			settingsMenuButton.setVisibility(View.VISIBLE);
			settingsMenu.setVisibility(View.INVISIBLE);
		}
	}

	public void markerChanged(final String markerCode)
	{
		Intent intent = getIntent();
		intent.putExtra("marker", markerCode);
		setResult(RESULT_OK, intent);
		finish();
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

		bottomView = (RelativeLayout)findViewById(R.id.bottomView);

		settingsMenuButton = findViewById(R.id.settingsMenuButton);
		settingsMenu = findViewById(R.id.settingsMenu);
		modeText = (TextView) findViewById(R.id.modeText);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		startCamera();
		experience.addListener(this);
	}

	@Override
	public void resultUpdated(final boolean detected, final Bitmap image)
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
				viewfinder.setResult(image);
			}
		});
	}

	protected ViewGroup getRootView()
	{
		return (ViewGroup)bottomView.getRootView();
	}

	public void showMenu(View view)
	{
		updateMenu();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = (settingsMenuButton.getLeft() + settingsMenuButton.getRight()) / 2;
			final int cy = ((settingsMenuButton.getTop() + settingsMenuButton.getBottom()) / 2) - settingsMenu.getTop();

			Log.i("", "Circle center " + cx + ", " + cy + " width " + bottomView.getWidth());

			final Animator anim = ViewAnimationUtils.createCircularReveal(settingsMenu, cx, cy, 0, bottomView.getWidth());

			settingsMenuButton.setVisibility(View.INVISIBLE);
			settingsMenu.setVisibility(View.VISIBLE);
			anim.start();
		}
		else
		{
			settingsMenuButton.setVisibility(View.INVISIBLE);
			settingsMenu.setVisibility(View.VISIBLE);
		}
	}

	public void toggleMarkerDisplay(View view)
	{
		if (detector.getMarkerDrawMode() == MarkerDetector.MarkerDrawMode.off)
		{
			detector.setMarkerDrawMode(MarkerDetector.MarkerDrawMode.outline);
		}
		else if (detector.getMarkerDrawMode() == MarkerDetector.MarkerDrawMode.outline)
		{
			detector.setMarkerDrawMode(MarkerDetector.MarkerDrawMode.regions);
		}
		else
		{
			detector.setMarkerDrawMode(MarkerDetector.MarkerDrawMode.off);
		}

		updateMenu();
	}

	public void toggleThresholdDisplay(View view)
	{
		detector.setDrawThreshold(!detector.shouldDrawThreshold());
		updateMenu();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		if (intent.getStringExtra("experience") != null)
		{
			try
			{
				Gson gson = ExperienceParser.createParser();
				Experience intentExperience = gson.fromJson(intent.getStringExtra("experience"), Experience.class);
				experience.set(intentExperience);
			}
			catch (Exception e)
			{
				Log.w(TAG, e.getMessage(), e);
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		stopCamera();
		experience.removeListener(this);
	}

	protected void updateMenu()
	{
		Button flipCameraButton = (Button) findViewById(R.id.flipCameraButton);
		if (camera.getCameraCount() > 1)
		{
			flipCameraButton.setVisibility(View.VISIBLE);
			if (camera.isFront())
			{
				flipCameraButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_camera_front_white_24dp, 0, 0, 0);
				flipCameraButton.setText(getString(R.string.camera_front));
			}
			else
			{
				flipCameraButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_camera_rear_white_24dp, 0, 0, 0);
				flipCameraButton.setText(getString(R.string.camera_rear));
			}
		}
		else
		{
			flipCameraButton.setVisibility(View.GONE);
		}

		Button thresholdDisplayButton = (Button) findViewById(R.id.thresholdDisplayButton);
		if (detector.shouldDrawThreshold())
		{
			thresholdDisplayButton.setText(getString(R.string.threshold_on));
			thresholdDisplayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_filter_b_and_w_white_24dp, 0, 0, 0);
		}
		else
		{
			thresholdDisplayButton.setText(getString(R.string.threshold_off));
			thresholdDisplayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_filter_b_and_w_off_white_24dp, 0, 0, 0);
		}

		Button markerDisplayButton = (Button) findViewById(R.id.markerDisplayButton);
		if (detector.getMarkerDrawMode() == MarkerDetector.MarkerDrawMode.off)
		{
			markerDisplayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_border_clear_white_24dp, 0, 0, 0);
			markerDisplayButton.setText(getString(R.string.marker_off));
		}
		else if (detector.getMarkerDrawMode() == MarkerDetector.MarkerDrawMode.outline)
		{
			markerDisplayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_border_outer_white_24dp, 0, 0, 0);
			markerDisplayButton.setText(getString(R.string.marker_outline));
		}
		else
		{
			markerDisplayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_border_all_white_24dp, 0, 0, 0);
			markerDisplayButton.setText(getString(R.string.marker_on));
		}

		//private ImageView autoOpenIcon;
		//private TextView autoOpenLabel;
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

//	private void setMode(MarkerDetector.Mode mode)
//	{
//		Log.i("", "Set mode to " + mode);
//		//detector.setMode(mode);
//		viewfinder.invalidate();
//		modeText.setText(getTextString(MODE_ACTIVE_PREFIX + mode.name(), mode.name()));
//	}

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
}