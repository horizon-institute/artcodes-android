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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.opencv.android.OpenCVLoader;
import uk.ac.horizon.artcodes.scanner.ExperienceScanner;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.databinding.ScannerBinding;

import java.net.URLDecoder;

public class ScannerActivity extends AppCompatActivity
{
	protected ScannerBinding binding;

	public void flipCamera(View view)
	{
		binding.cameraView.flipCamera();
		//updateMenu();
	}

	public void hideMenu(View view)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = (binding.settingsMenuButton.getLeft() + binding.settingsMenuButton.getRight()) / 2;
			final int cy = ((binding.settingsMenuButton.getTop() + binding.settingsMenuButton.getBottom()) / 2) - binding.settingsMenu.getTop();

			Log.i("", "Circle center " + cx + ", " + cy + " width " + binding.bottomView.getWidth());

			final Animator anim = ViewAnimationUtils.createCircularReveal(binding.settingsMenu, cx, cy, binding.bottomView.getWidth(), 0);

			anim.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					super.onAnimationEnd(animation);
					binding.settingsMenu.setVisibility(View.INVISIBLE);
					binding.settingsMenuButton.setVisibility(View.VISIBLE);
				}
			});

			anim.start();
		}
		else
		{
			binding.settingsMenuButton.setVisibility(View.VISIBLE);
			binding.settingsMenu.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.scanner);
		binding.cameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				int height = getWindow().getDecorView().getHeight();
				int width = getWindow().getDecorView().getWidth();

				int dividerSize = Math.abs(height - width) / 2;

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					binding.toolbar.getLayoutParams().width = dividerSize;
					binding.bottomView.getLayoutParams().width = dividerSize;
				}
				else
				{
					binding.toolbar.getLayoutParams().height = dividerSize;
					binding.bottomView.getLayoutParams().height = dividerSize;

					int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
					if (resourceId > 0)
					{
						int navHeight = getResources().getDimensionPixelSize(resourceId);
						if (navHeight != 0)
						{
							binding.bottomView.setPadding(0, 0, 0, navHeight);
						}
					}
				}
			}
		});

		setSupportActionBar(binding.toolbar);

		Log.i("", "Intent onCreate: " + getIntent());
		onNewIntent(getIntent());
	}

	public void showMenu(View view)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = (binding.settingsMenuButton.getLeft() + binding.settingsMenuButton.getRight()) / 2;
			final int cy = ((binding.settingsMenuButton.getTop() + binding.settingsMenuButton.getBottom()) / 2) - binding.settingsMenu.getTop();

			Log.i("", "Circle center " + cx + ", " + cy + " width " + binding.bottomView.getWidth());

			final Animator anim = ViewAnimationUtils.createCircularReveal(binding.settingsMenu, cx, cy, 0, binding.bottomView.getWidth());

			binding.settingsMenuButton.setVisibility(View.INVISIBLE);
			binding.settingsMenu.setVisibility(View.VISIBLE);
			anim.start();
		}
		else
		{
			binding.settingsMenuButton.setVisibility(View.INVISIBLE);
			binding.settingsMenu.setVisibility(View.VISIBLE);
		}
	}

	public void toggleMarkerDisplay(View view)
	{
		//binding.getExperience().nextDrawMarker();
	}

	public void toggleThresholdDisplay(View view)
	{
		//binding.getExperience().nextThreshold();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		String artcode_scheme = getString(R.string.artcode_scan_scheme);
		Log.i("", "New Intent: " + intent);
		if (intent.getStringExtra("experience") != null)
		{
			try
			{
				final Gson gson = new GsonBuilder().create();
				final ExperienceScanner intentExperience = gson.fromJson(intent.getStringExtra("experience"), ExperienceScanner.class);
				binding.setExperience(intentExperience);
			}
			catch (Exception e)
			{
				Log.w("", e.getMessage(), e);
			}
		}
		else if (artcode_scheme.equals(intent.getScheme()))
		{
			String data = intent.getData().toString();
			Log.i("", "Data: " + data);

			if (data.startsWith(artcode_scheme + ":"))
			// should!
			{
				data = data.substring(artcode_scheme.length() + 1);
				if (data.startsWith("//"))
				{
					data = data.substring(2);
				}
			}
			try
			{
				Log.i("", "Data: " + data);
				data = URLDecoder.decode(data, "UTF-8");
				Gson gson = new GsonBuilder().create();
				ExperienceScanner intentExperience = gson.fromJson(data, ExperienceScanner.class);
				binding.setExperience(intentExperience);
			}
			catch (Exception e)
			{
				Log.e("", "Error decoding experience " + data + ": " + e);
				finish();
			}
		}
	}

	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e("", "Error Initializing OpenCV");
		}
	}
}
