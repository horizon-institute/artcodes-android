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

package uk.ac.horizon.artcodes.scanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.List;

import uk.ac.horizon.artcodes.animator.TextAnimator;
import uk.ac.horizon.artcodes.animator.VisibilityAnimator;
import uk.ac.horizon.artcodes.camera.CameraView;
import uk.ac.horizon.artcodes.detect.ArtcodeDetector;
import uk.ac.horizon.artcodes.detect.DetectorCallback;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.handler.CodeDetectionHandler;
import uk.ac.horizon.artcodes.detect.handler.MarkerCodeDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class ScannerActivity extends AppCompatActivity
{
	private static final int CAMERA_PERMISSION_REQUEST = 47;
	private LinearLayout settingIcons;
	protected ProgressBar progressBar;
	private ArtcodeDetector detector;
	private Experience experience;
	private VisibilityAnimator menuAnimator;
	private TextAnimator textAnimator;
	private CameraView cameraView;

	@SuppressWarnings("UnusedParameters")
	public void hideMenu(View view)
	{
		menuAnimator.hideView();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void hideSystemUI()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		hideSystemUI();

		setContentView(R.layout.scanner);

		settingIcons = (LinearLayout) findViewById(R.id.settingsSwitches);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		cameraView = (CameraView) findViewById(R.id.cameraView);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
		}

		menuAnimator = new VisibilityAnimator(findViewById(R.id.settingsMenu), findViewById(R.id.settingsMenuButton));
		TextView settingsFeedback = (TextView) findViewById(R.id.settingsFeedback);
		textAnimator = new TextAnimator(settingsFeedback);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case CAMERA_PERMISSION_REQUEST:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					startScanning();
				}
				else
				{
					Log.i("a", "Permission not granted");
					// TODO
				}
			}
		}
	}

	public void loaded(Experience experience)
	{
		this.experience = experience;
		startScanning();
	}

	@SuppressWarnings("UnusedParameters")
	public void showMenu(View view)
	{
		createSettingsUI(detector.getSettings());
		menuAnimator.showView();
	}

	protected Experience getExperience()
	{
		return experience;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			if (getCallingActivity() != null)
			{
				setResult(RESULT_CANCELED);
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void startScanning()
	{
		if (experience != null)
		{
			Log.i("a", "Start Scanning");
			detector = this.getNewDetector(experience);
			detector.setCallback(new DetectorCallback()
			{
				@Override
				public void detectionStart(final int margin)
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							View errorView = findViewById(R.id.cameraError);
							if (errorView != null)
							{
								errorView.setVisibility(View.GONE);
							}
							progressBar.setVisibility(View.VISIBLE);
							if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
							{
								View topView = findViewById(R.id.topView);
								if (topView != null)
								{
									topView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, margin));
								}
								View bottomView = findViewById(R.id.bottomView);
								if (bottomView != null)
								{
									bottomView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, margin));
								}
							}
							else
							{
								View topView = findViewById(R.id.topView);
								if (topView != null)
								{
									topView.setLayoutParams(new LinearLayout.LayoutParams(margin, ViewGroup.LayoutParams.MATCH_PARENT));
								}
								View bottomView = findViewById(R.id.bottomView);
								if (bottomView != null)
								{
									bottomView.setLayoutParams(new LinearLayout.LayoutParams(margin, ViewGroup.LayoutParams.MATCH_PARENT));
								}
							}
						}
					});
				}
			});
			detector.setOverlay((ImageView) findViewById(R.id.overlay));
			if (getSupportActionBar() != null)
			{
				getSupportActionBar().setDisplayShowTitleEnabled(true);
				getSupportActionBar().setTitle(experience.getName());
			}
			cameraView.setDetector(detector, experience);

			if (cameraView.deviceNeedsTapToFocus())
			{
				// tap to focus
				View v = findViewById(R.id.thumbnailImageLayout);
				if (v != null)
				{
					v.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							setFocusTextVisible(false);
							cameraView.focus(new Runnable()
							{
								@Override
								public void run()
								{
									setFocusTextVisible(true);
								}
							});

						}
					});
				}
				setFocusTextVisible(true);
			}
		}
		else
		{
			cameraView.setDetector(null, null);
		}
	}

	private boolean focusTextAlreadyShown = false;
	private void setFocusTextVisible(final boolean visible)
	{
		final TextView focusTextView = (TextView) findViewById(R.id.focusText);
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (focusTextView != null)
				{
					if (focusTextAlreadyShown)
					{
						focusTextView.setText(R.string.tap_to_refocus);
						focusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
						focusTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
					}
					if (visible)
					{
						focusTextView.setVisibility(View.VISIBLE);
					}
					else
					{
						focusTextView.setVisibility(View.INVISIBLE);
						focusTextAlreadyShown = true;
					}
				}
			}
		});
	}

	private void onCodeDetected(String markerCode)
	{
		Log.i("Marker", "MarkerDisplay Detected: " + markerCode);
		if (markerCode != null)
		{
			if (experience.getCallback() != null)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(experience.getCallback().replace("{code}", markerCode))));
			}
			else
			{
				Intent intent = getIntent();
				intent.putExtra("marker", markerCode);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	}

	protected void loadExperience(Bundle savedInstanceState)
	{
		if (savedInstanceState != null && savedInstanceState.containsKey("experience"))
		{

			loaded(new Gson().fromJson(savedInstanceState.getString("experience"), Experience.class));
		}
		else
		{
			Intent intent = getIntent();
			if (intent.hasExtra("experience"))
			{
				loaded(new Gson().fromJson(intent.getStringExtra("experience"), Experience.class));
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		loadExperience(savedInstanceState);
	}

	private Drawable getTintedDrawable(@DrawableRes int drawable, @ColorInt int color)
	{
		final Drawable original = ContextCompat.getDrawable(this, drawable);
		if (original != null)
		{
			final Drawable wrapDrawable = DrawableCompat.wrap(original);
			DrawableCompat.setTint(wrapDrawable, color);
			return wrapDrawable;
		}
		return null;
	}

	private void createSettingsUI(List<DetectorSetting> settings)
	{
		settingIcons.removeAllViews();
		if (settings != null && !settings.isEmpty())
		{
			final int padding = getResources().getDimensionPixelSize(R.dimen.setting_padding);
			List<DetectorSetting> settingList = settings;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
				settingList = Lists.reverse(settings);
			}
			for (final DetectorSetting setting : settingList)
			{
				final ImageView button = new ImageView(this);
				button.setContentDescription(getString(setting.getText()));
				final int[] attrs = new int[]{android.R.attr.selectableItemBackground};
				final TypedArray ta = obtainStyledAttributes(attrs);
				final Drawable drawableFromTheme = ta.getDrawable(0);
				ta.recycle();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					button.setImageResource(setting.getIcon());
					button.setImageTintList(ColorStateList.valueOf(Color.WHITE));
					button.setBackground(drawableFromTheme);
				}
				else
				{
					button.setImageDrawable(getTintedDrawable(setting.getIcon(), Color.WHITE));
					//noinspection deprecation
					button.setBackgroundDrawable(drawableFromTheme);
				}

				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						setting.nextValue();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
						{
							button.setImageResource(setting.getIcon());
						}
						else
						{
							button.setImageDrawable(getTintedDrawable(setting.getIcon(), Color.WHITE));
						}
						button.setContentDescription(getString(setting.getText()));
						textAnimator.setText(setting.getText());
					}
				});
				final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
				params.weight = 1;
				button.setLayoutParams(params);
				settingIcons.addView(button);
				button.setPadding(padding, padding, padding, padding);
			}

		}

		menuAnimator.setViewVisible(settingIcons.getChildCount() > 0);
	}

	protected ArtcodeDetector getNewDetector(Experience experience)
	{
		return new ArtcodeDetector(this, experience, new MarkerCodeDetectionHandler(this.experience, new CodeDetectionHandler()
		{
			@Override
			public void onMarkerCodeDetected(String code)
			{
				onCodeDetected(code);
			}
		}));
	}
}
