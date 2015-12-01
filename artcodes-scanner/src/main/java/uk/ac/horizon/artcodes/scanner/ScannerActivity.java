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

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.detect.ExperienceFrameProcessor;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.scanner.databinding.ScannerBinding;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessorSetting;

public class ScannerActivity extends AppCompatActivity implements MarkerDetectionHandler
{
	protected ScannerBinding binding;
	// TODO Use binding variables?
	protected Scanner scanner;
	private VisibilityAnimator menuAnimator;
	private TextAnimator textAnimator;
	private final ImageBuffers buffers = new ImageBuffers();
	protected Experience experience;

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

		Log.i("", "Setup Scanner");
		binding = DataBindingUtil.setContentView(this, R.layout.scanner);
		scanner = new Scanner(this);
		binding.cameraSurface.getHolder().addCallback(scanner);
		binding.setBuffers(buffers);
		setSupportActionBar(binding.toolbar);

		menuAnimator = new VisibilityAnimator(binding.settingsMenu, binding.settingsMenuButton);
		textAnimator = new TextAnimator(binding.settingsFeedback);
	}

	protected static final int REQUIRED = 5;
	protected static final int MAX = REQUIRED * 4;
	private final Multiset<String> markerCounts = HashMultiset.create();

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

	private void onCodeDetected(String markerCode)
	{
		Log.i("", "MarkerDisplay Detected: " + markerCode);
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

	protected Experience getExperience()
	{
		return experience;
	}

	public void onLoaded(Experience experience)
	{
		this.experience = experience;
		binding.setExperience(experience);
		List<ImageProcessorSetting> settings;
		if (experience != null)
		{
			settings = scanner.setFrameProcessor(new ExperienceFrameProcessor(buffers, experience, this));
		}
		else
		{
			settings = scanner.setFrameProcessor(null);
		}
		createSettingsUI(settings);
	}

	private void createSettingsUI(List<ImageProcessorSetting> settings)
	{
		binding.settingsSwitches.removeAllViews();

		for(ImageProcessorSetting setting: settings)
		{

		}

		if(binding.settingsSwitches.getChildCount() > 0)
		{
			binding.settingsMenuButton.setVisibility(View.VISIBLE);
		}
		else
		{
			binding.settingsMenuButton.setVisibility(View.GONE);
		}
	}

	public void showMenu(View view)
	{
		menuAnimator.showView();
	}
}