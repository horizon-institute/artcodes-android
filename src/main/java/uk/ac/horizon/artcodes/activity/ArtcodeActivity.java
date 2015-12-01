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
package uk.ac.horizon.artcodes.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.common.collect.Multiset;

import java.util.List;

import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.databinding.ScannerActionBinding;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.IntentSource;
import uk.ac.horizon.artcodes.request.RequestCallbackBase;
import uk.ac.horizon.artcodes.scanner.VisibilityAnimator;
import uk.ac.horizon.artcodes.scanner.ScannerActivity;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.ui.IntentBuilder;

public class ArtcodeActivity extends ScannerActivity
{
	private static final String EXTRA_CUSTOM_TABS_SESSION_ID = "android.support.CUSTOM_TABS:session_id";

	private ScannerActionBinding actionBinding;
	private VisibilityAnimator actionAnimator;

	private Action action;

	@Override
	protected void onMarkersDetected(Multiset<String> markers)
	{
		int best = 0;
		Action selected = null;
		for (Action action : experience.getActions())
		{
			if (action.getMatch() == Action.Match.any)
			{
				for (String code : action.getCodes())
				{
					int count = markers.count(code);
					if (count > best)
					{
						selected = action;
						best = count;
					}
				}
			}
			else if (action.getMatch() == Action.Match.all)
			{
				int min = MAX;
				int total = 0;
				for (String code : action.getCodes())
				{
					int count = markers.count(code);
					min = Math.min(min, count);
					total += count;
				}

				if (min > REQUIRED && total > best)
				{
					best = total;
					selected = action;
				}
			}
		}

		if (selected == null || best < REQUIRED)
		{
			if (action != null)
			{
				action = null;
				onActionChanged(null);
			}
		}
		else if (selected != action)
		{
			action = selected;
			onActionChanged(action);
		}
	}

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

	protected ArtcodeServer getServer()
	{
		return getArtcodes().getServer();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, IntentBuilder.with(this)
						.setServer(getServer())
						.target(ExperienceActivity.class)
						.set("experience", getExperience())
						.create());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		new IntentSource<Experience>(getServer(), getIntent(), savedInstanceState, Experience.class).loadInto(new RequestCallbackBase<Experience>()
		{
			@Override
			public void onResponse(final Experience experience)
			{
				onLoaded(experience);
				Log.i("", "Set experience " + experience);
				if (experience != null)
				{
					GoogleAnalytics.trackScreen("Scan", getExperience().getId());
					getServer().loadRecent(new RequestCallbackBase<List<String>>()
					{
						@Override
						public void onResponse(List<String> item)
						{
							item.remove(experience.getId());
							item.add(0, experience.getId());
						}
					});
				}
			}
		});
	}

	private void onActionChanged(final Action action)
	{
		if (action != null)
		{
			getServer().logScan(experience.getId(), action, scanner);
			GoogleAnalytics.trackEvent("Action", "Scanned", experience.getId(), action.getName());

			actionBinding.setAction(action);
			actionBinding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					GoogleAnalytics.trackEvent("Action", "Opened", experience.getId(), action.getName());
					if (action.getShowDetail())
					{
						ActionActivity.start(ArtcodeActivity.this, action);
					} else
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
		} else
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
}