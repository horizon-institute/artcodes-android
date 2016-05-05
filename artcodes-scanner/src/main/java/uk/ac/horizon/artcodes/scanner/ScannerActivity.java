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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import uk.ac.horizon.artcodes.animator.TextAnimator;
import uk.ac.horizon.artcodes.animator.VisibilityAnimator;
import uk.ac.horizon.artcodes.detect.ArtcodeDetector;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.databinding.ScannerBinding;

public class ScannerActivity extends AppCompatActivity implements MarkerDetectionHandler
{
	protected static final int REQUIRED = 20;
	protected static final int MAX = REQUIRED * 4;
	private static final int CAMERA_PERMISSION_REQUEST = 47;
	private final Multiset<String> markerCounts = HashMultiset.create();
	protected ScannerBinding binding;
	private ArtcodeDetector detector;
	private Experience experience;
	private VisibilityAnimator menuAnimator;
	private TextAnimator textAnimator;

	public void hideMenu(View view)
	{
		menuAnimator.hideView();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.scanner);
		binding.progressBar.setVisibility(View.INVISIBLE);
		setSupportActionBar(binding.toolbar);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
		}

		menuAnimator = new VisibilityAnimator(binding.settingsMenu, binding.settingsMenuButton);
		textAnimator = new TextAnimator(binding.settingsFeedback);
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

	public void onMarkersDetected(Collection<String> markers)
	{
		final Collection<String> removals = new HashSet<>(markerCounts.elementSet());

		for (String marker : markers)
		{
			final int count = markerCounts.count(marker);
			if (count > MAX)
			{
				markerCounts.setCount(marker, MAX);
			}

			//increase occurrence if this marker is already in the list.
			markerCounts.add(marker);
			removals.remove(marker);
		}

		markerCounts.removeAll(removals);

		onMarkersDetected(markerCounts);
	}

	public void loaded(Experience experience)
	{
		this.experience = experience;
		startScanning();
	}

	public void showMenu(View view)
	{
		createSettingsUI(detector.getSettings());
		menuAnimator.showView();
	}

	protected void onMarkersDetected(Multiset<String> markers)
	{
		int best = 0;
		String selected = null;
		for (String code : markers.elementSet())
		{
			int count = markers.count(code);
			if (count > best)
			{
				selected = code;
				best = count;
			}
		}

		if (selected != null || best >= REQUIRED)
		{
			onCodeDetected(selected);
		}
	}

	protected Experience getExperience()
	{
		return experience;
	}

	private void startScanning()
	{
		if (experience != null)
		{
			Log.i("a", "Start Scanning");
			detector = new ArtcodeDetector(experience, this);
			binding.setExperience(experience);
			binding.setDetector(detector);
		}
		else
		{
			binding.setDetector(null);
		}
	}

	private void onCodeDetected(String markerCode)
	{
		Log.i("Marker", "MarkerDisplay Detected: " + markerCode);
		if (markerCode != null)
		{
			if (experience.getCallback() == null)
			{
				Intent intent = getIntent();
				intent.putExtra("marker", markerCode);
				setResult(RESULT_OK, intent);
				finish();
			}
			else
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(experience.getCallback().replace("{code}", markerCode))));
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
		binding.settingsSwitches.removeAllViews();
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
				binding.settingsSwitches.addView(button);
				button.setPadding(padding, padding, padding, padding);
			}

		}

		if (binding.settingsSwitches.getChildCount() > 0)
		{
			binding.settingsMenuButton.setVisibility(View.VISIBLE);
		}
		else
		{
			binding.settingsMenuButton.setVisibility(View.GONE);
		}
	}
}