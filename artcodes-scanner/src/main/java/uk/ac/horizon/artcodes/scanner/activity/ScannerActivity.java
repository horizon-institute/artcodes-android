/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.horizon.artcodes.scanner.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.ExperienceFrameProcessor;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.VisibilityAnimator;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;
import uk.ac.horizon.artcodes.scanner.databinding.ScannerBinding;
import uk.ac.horizon.artcodes.scanner.detect.CodeDetectionHandler;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.scanner.overlay.Overlay;

public class ScannerActivity extends AppCompatActivity
{
	protected ScannerBinding binding;
	// TODO Use binding variables
	private CameraAdapter camera;
	private Overlay overlay;
	private VisibilityAnimator menuAnimator;
	private Experience experience;

	public void flipCamera(View view)
	{
		camera.flipCamera();
	}

	public void hideMenu(View view)
	{
		menuAnimator.hideView();
	}

	public void nextCodeDrawMode(View view)
	{
		overlay.nextCodeDrawMode();
	}

	public void nextMarkerDrawMode(View view)
	{
		overlay.nextMarkerDrawMode();
	}

	public void nextThresholdDrawMode(View view)
	{
		overlay.nextThresholdDrawMode();
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

		binding = DataBindingUtil.setContentView(this, R.layout.scanner);
		camera = new CameraAdapter(this);
		binding.setCamera(camera);
		overlay = new Overlay();
		binding.setOverlay(overlay);
		setSupportActionBar(binding.toolbar);

		menuAnimator = new VisibilityAnimator(binding.settingsMenu, binding.settingsMenuButton);
	}

	protected Experience getExperience()
	{
		return experience;
	}

	public void onLoaded(Experience experience)
	{
		this.experience = experience;
		binding.setExperience(experience);
		if (experience != null)
		{
			camera.setFrameProcessor(new ExperienceFrameProcessor(experience, createMarkerHandler(experience), overlay));
		}
		else
		{
			camera.setFrameProcessor(null);
		}
	}

	public void showMenu(View view)
	{
		menuAnimator.showView();
	}

	protected MarkerDetectionHandler createMarkerHandler(final Experience experience)
	{
		return new CodeDetectionHandler()
		{
			@Override
			public void onCodeDetected(String markerCode)
			{
				Log.i("", "Marker Detected: " + markerCode);
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
		};
	}

	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e("", "Error Initializing OpenCV");
		}
	}
}