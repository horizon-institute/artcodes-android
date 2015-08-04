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
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.ScannerActionBinding;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.VisibilityAnimator;
import uk.ac.horizon.artcodes.scanner.activity.ScannerActivity;
import uk.ac.horizon.artcodes.scanner.detect.ActionDetectionHandler;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.source.IntentSource;
import uk.ac.horizon.artcodes.source.Target;

public class ArtcodeActivity extends ScannerActivity implements Target<Experience>
{
	private static final String EXTRA_CUSTOM_TABS_SESSION_ID = "android.support.CUSTOM_TABS:session_id";

	private ScannerActionBinding actionBinding;
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

	protected Artcodes getArtcodes()
	{
		return (Artcodes) getApplication();
	}

	protected Account getAccount()
	{
		return getArtcodes().getAccount();
	}

	@Override
	public void onLoaded(Experience experience)
	{
		super.onLoaded(experience);

		Log.i("", "Set experience " + experience);
		if (experience != null)
		{
			GoogleAnalytics.trackEvent("Experience", "Loaded " + experience.getId());
			getAccount().getRecent().add(experience.getId());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, ExperienceActivityBase.createIntent(this, ExperienceActivity.class, getExperience()));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		new IntentSource<Experience>(getAccount(), getIntent(), savedInstanceState, Experience.class).loadInto(this);
	}

	@Override
	protected MarkerDetectionHandler createMarkerHandler(final Experience experience)
	{
		return new ActionDetectionHandler(experience)
		{
			@Override
			public void onActionChanged(final Action action)
			{
				if (action != null)
				{
					// TODO getAccount().scanned(experience.getId(), action, camera);
					GoogleAnalytics.trackEvent("action", "Detected " + action);
					if (Feature.get(ArtcodeActivity.this, R.bool.feature_log_scanned_images).isEnabled())
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

					actionBinding.setAction(action);
					actionBinding.getRoot().setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							GoogleAnalytics.trackEvent("action", "Opened " + action);
							//camera.stop();
							// TODO ExperienceLoaders.with(ArtcodeActivity.this).logAction(getRef());
							if (action.getShowDetail())
							{
								ActionActivity.start(ArtcodeActivity.this, action);
							}
							else
							{
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getUrl()));
								intent.putExtra(EXTRA_CUSTOM_TABS_SESSION_ID, -1); // -1 or any valid session id returned from newSession() call

								startActivity(intent);
							}
						}
					});
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							actionAnimator.showView();
						}
					});
				}
				else
				{
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
}