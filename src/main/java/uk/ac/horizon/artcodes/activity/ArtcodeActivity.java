/*
 * Artcodes recognises a different action scheme that allows the
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
package uk.ac.horizon.artcodes.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import uk.ac.horizon.artcodes.ArtcodeFeature;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.databinding.ScannerActionBinding;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.Feature;
import uk.ac.horizon.artcodes.scanner.VisibilityAnimator;
import uk.ac.horizon.artcodes.scanner.activity.ScannerActivity;
import uk.ac.horizon.artcodes.scanner.detect.ActionDetectionHandler;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;

public class ArtcodeActivity extends ScannerActivity
{
	private ScannerActionBinding actionBinding;
	private Action action;
	private VisibilityAnimator actionAnimator;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		actionBinding = ScannerActionBinding.inflate(getLayoutInflater(), binding.contentFrame, false);
		binding.contentFrame.addView(actionBinding.getRoot());
		actionAnimator = new VisibilityAnimator(actionBinding.getRoot());

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, upIntent());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void openAction(View view)
	{
		GoogleAnalytics.trackEvent("action", "Opened " + action);
		//camera.stop();
		if (action.getShowDetail())
		{
			ActionActivity.start(this, action);
		}
		else
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(action.getUrl())));
		}
	}

	@Override
	protected MarkerDetectionHandler createMarkerHandler(final Experience experience)
	{
		return new ActionDetectionHandler(experience)
		{
			@Override
			public void onActionChanged(Action newAction)
			{
				if (newAction != null)
				{
					GoogleAnalytics.trackEvent("action", "Detected " + newAction);

					if (Feature.isEnabled(ArtcodeFeature.LOG_SCAN_IMAGE))
					{
						try
						{
							// TODO camera.saveImage(Artcodes.createImageLogFile());
						}
						catch (Exception e)
						{
							Log.w("", e.getMessage(), e);
						}
					}

					if (action == null)
					{
						action = newAction;
						actionBinding.setAction(action);

						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								actionAnimator.showView();
							}
						});
					}
					else if (!action.equals(newAction))
					{
						action = newAction;
						actionBinding.setAction(action);
					}
				}
				else if (action != null)
				{
					action = null;
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							actionAnimator.hideView();
						}
					});
				}
			}
		};
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalytics.trackScreen("Scan Screen");
	}

	@Override
	protected void setExperience(Experience experience)
	{
		super.setExperience(experience);

		Log.i("", "Set experience " + experience);
		if (experience != null)
		{
			GoogleAnalytics.trackEvent("Experience", "Loaded " + experience.getId());
		}

		if (experience != null)
		{
			RecentExperiences.with(this).add(experience.getId());
		}
	}

	private Intent upIntent()
	{
		final Intent intent = new Intent(this, ExperienceActivity.class);
		intent.putExtra("experience", getExperience().getId());

		return intent;
	}
}