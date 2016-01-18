/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
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
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.databinding.ScannerBinding;
import uk.ac.horizon.artcodes.scanner.detect.ExperienceFrameProcessor;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessorSetting;

public class ScannerActivity extends AppCompatActivity implements MarkerDetectionHandler
{
	private static final int CAMERA_PERMISSION_REQUEST = 47;
	protected static final int REQUIRED = 5;
	protected static final int MAX = REQUIRED * 4;
	private final ImageBuffers buffers = new ImageBuffers();
	private final Multiset<String> markerCounts = HashMultiset.create();
	protected ScannerBinding binding;
	// TODO Use binding variables?
	protected Scanner scanner;
	protected Experience experience;
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}

		scanner = new Scanner(this);
		binding = DataBindingUtil.setContentView(this, R.layout.scanner);
		binding.setBuffers(buffers);
		binding.cameraSurface.getHolder().addCallback(scanner);
		binding.progressBar.setVisibility(View.INVISIBLE);
		setSupportActionBar(binding.toolbar);

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

	private void startScanning()
	{
		List<ImageProcessorSetting> settings;
		if (experience != null)
		{
			Log.i("a", "Start Scanning");
			settings = scanner.setFrameProcessor(new ExperienceFrameProcessor(buffers, experience, this));
			binding.progressBar.setVisibility(View.VISIBLE);
		}
		else
		{
			settings = scanner.setFrameProcessor(null);
		}
		createSettingsUI(settings);
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
		binding.setExperience(experience);
		startScanning();
	}

	public void showMenu(View view)
	{
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

	private void createSettingsUI(List<ImageProcessorSetting> settings)
	{
		binding.settingsSwitches.removeAllViews();
		if (settings != null)
		{
			for (final ImageProcessorSetting setting : settings)
			{
				final View view = getLayoutInflater().inflate(R.layout.setting_button, binding.settingsSwitches, false);
				final ImageButton button = (ImageButton)view.findViewById(R.id.button);
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						setting.nextValue();
						button.setImageResource(setting.getIcon());
						textAnimator.setText(setting.getText());
					}
				});
				button.setImageResource(setting.getIcon());
				final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
				button.setLayoutParams(params);
				binding.settingsSwitches.addView(button);
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