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
package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.common.collect.Multiset;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.List;

import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.animator.VisibilityAnimator;
import uk.ac.horizon.artcodes.databinding.ScannerActionBinding;
import uk.ac.horizon.artcodes.detect.ArtcodeDetector;
import uk.ac.horizon.artcodes.detect.marker.Marker;
import uk.ac.horizon.artcodes.detect.marker.MarkerActionDetectionHandler;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.ScannerActivity;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ArtcodeActivity extends ScannerActivity implements LoadCallback<Experience>
{
	private ScannerActionBinding actionBinding;
	private VisibilityAnimator actionAnimator;

	private Action action;

	public static void start(Context context, Experience experience)
	{
		final String experienceJSON = new Gson().toJson(experience);
		TaskStackBuilder.create(context)
				.addNextIntent(new Intent(context, NavigationActivity.class))
				.addNextIntent(new Intent(context, ExperienceActivity.class)
						.putExtra("experience", experienceJSON))
				.addNextIntent(new Intent(context, ArtcodeActivity.class)
						.putExtra("experience", experienceJSON))
				.startActivities();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		actionBinding = ScannerActionBinding.inflate(getLayoutInflater(), binding.bottomView, false);
		binding.bottomView.addView(actionBinding.getRoot());
		actionAnimator = new VisibilityAnimator(actionBinding.getRoot());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.apptheme_accent)));
		}

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			final Experience experience = getExperience();
			NavUtils.navigateUpTo(this, ExperienceActivity.intent(this, experience));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void loaded(final Experience experience)
	{
		super.loaded(experience);
		if (experience != null)
		{
			GoogleAnalytics.trackScreen("Scan", getExperience().getId());
			getServer().loadRecent(new LoadCallback<List<String>>()
			{
				@Override
				public void loaded(List<String> item)
				{
					item.remove(experience.getId());
					item.add(0, experience.getId());
					getServer().saveRecent(item);
				}

				@Override
				public void error(Throwable e)
				{
					GoogleAnalytics.trackException(e);
				}
			});
		}
	}

	@Override
	public void error(Throwable e)
	{
		GoogleAnalytics.trackException(e);
	}

	@Override
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
			else
			{
				final Uri data = intent.getData();
				if (data != null)
				{
					getServer().loadExperience(data.toString(), this);
				}
			}
		}
	}

	private Artcodes getArtcodes()
	{
		return (Artcodes) getApplication();
	}

	private ArtcodeServer getServer()
	{
		return getArtcodes().getServer();
	}

	private void onActionChanged(final Action action)
	{
		Log.i("action", "" + action);
		if (action != null)
		{
			final Experience experience = getExperience();
			getServer().logScan(experience.getId(), action);
			GoogleAnalytics.trackEvent("Action", "Scanned", experience.getId(), action.getName());

			actionBinding.setAction(action);
			actionBinding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					GoogleAnalytics.trackEvent("Action", "Opened", experience.getId(), action.getName());
					CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
					// TODO Warmup urls
					//builder.setSession(session);
					builder.setToolbarColor(ContextCompat.getColor(ArtcodeActivity.this, R.color.apptheme_primary));
					CustomTabsIntent customTabsIntent = builder.build();
					customTabsIntent.launchUrl(ArtcodeActivity.this, Uri.parse(action.getUrl()));
				}
			});
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					actionAnimator.showView();
					binding.progressBar.setVisibility(View.INVISIBLE);
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
					binding.progressBar.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	protected ArtcodeDetector getNewDetector(Experience experience)
	{
		return new ArtcodeDetector(experience, new MarkerActionDetectionHandler(new MarkerActionDetectionHandler.ActionDetectionHandler()
		{
			@Override
			public void onMarkerActionDetected(Action detectedAction, Collection<Marker> detectedMarkers, Action asPartOfFutureAction)
			{
				onActionChanged(detectedAction);
			}
		}, experience));
	}
}