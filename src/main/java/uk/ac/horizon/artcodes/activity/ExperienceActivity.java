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

package uk.ac.horizon.artcodes.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.List;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallbackBase;
import uk.ac.horizon.artcodes.ui.IntentBuilder;

public class ExperienceActivity extends ExperienceActivityBase
{
	private ExperienceBinding binding;

	public void editExperience(View view)
	{
		IntentBuilder.with(this)
				.target(ExperienceEditActivity.class)
				.setServer(getServer())
				.set("experience", getExperience())
				.start();
	}

	@Override
	public void onResponse(Experience experience)
	{
		super.onResponse(experience);
		GoogleAnalytics.trackScreen("View Experience", experience.getId());
		binding.setExperience(experience);

		if (Feature.get(this, R.bool.feature_history).isEnabled())
		{
			binding.experienceHistoryButton.setVisibility(View.VISIBLE);
		}

		binding.experienceDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				if (binding.experienceDescription.getLineCount() > 1)
				{
					binding.experienceDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					Layout layout = binding.experienceDescription.getLayout();
					if (layout != null)
					{
						int lines = layout.getLineCount();
						if (lines > 0)
						{
							int ellipsisCount = layout.getEllipsisCount(lines - 1);
							if (ellipsisCount == 0)
							{
								binding.experienceDescriptionMore.setVisibility(View.GONE);
							}
							else
							{
								binding.experienceDescriptionMore.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			}
		});

		updateStarred();
	}

	public void readDescription(View view)
	{
		// TODO Animate
		binding.experienceDescriptionMore.setVisibility(View.GONE);
		binding.experienceDescription.setMaxLines(Integer.MAX_VALUE);
		binding.scrollView.smoothScrollTo(0, binding.experienceDescription.getTop());
	}

	public void scanExperience(View view)
	{
		IntentBuilder.with(this)
				.target(ArtcodeActivity.class)
				.setServer(getServer())
				.set("experience", getExperience())
				.start();
	}

	public void shareExperience(View view)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		if (isLoaded())
		{
			intent.putExtra(Intent.EXTRA_SUBJECT, getExperience().getName());
		}
		intent.putExtra(Intent.EXTRA_TEXT, getUri());
		Intent openInChooser = Intent.createChooser(intent, "Share with...");
		GoogleAnalytics.trackEvent("Experience", "Share", getUri());
		startActivity(openInChooser);
	}

	public void starExperience(View view)
	{
		getServer().loadStarred(new RequestCallbackBase<List<String>>()
		{
			@Override
			public void onResponse(List<String> starred)
			{
				if (starred.contains(getUri()))
				{
					GoogleAnalytics.trackEvent("Experience", "Unstar", getUri());
					starred.remove(getUri());
				}
				else
				{
					GoogleAnalytics.trackEvent("Experience", "Star", getUri());
					starred.add(getUri());
				}
				updateStarred();
			}
		});
	}

	public void startExperienceHistory(View view)
	{
		IntentBuilder.with(this)
				.target(ExperienceHistoryActivity.class)
				.setServer(getServer())
				.set("experience", getExperience())
				.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.experience);

		onNewIntent(getIntent());

		setSupportActionBar(binding.toolbar);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void updateStarred()
	{
		getServer().loadStarred(new RequestCallbackBase<List<String>>()
		{
			@Override
			public void onResponse(List<String> item)
			{
				if (item.contains(getUri()))
				{
					binding.experienceFavouriteButton.setText(R.string.unstar);
					binding.experienceFavouriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_black_24dp, 0, 0);
				}
				else
				{
					binding.experienceFavouriteButton.setText(R.string.star);
					binding.experienceFavouriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_border_black_24dp, 0, 0);
				}
			}
		});
	}
}
