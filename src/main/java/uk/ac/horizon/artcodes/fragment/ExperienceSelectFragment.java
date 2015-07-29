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

package uk.ac.horizon.artcodes.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.Nullable;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.activity.ExperienceActivity;
import uk.ac.horizon.artcodes.databinding.ExperienceItemBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceSelectBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceSelectGroupBinding;
import uk.ac.horizon.artcodes.json.ExperienceParserFactory;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.storage.ExperienceListStore;
import uk.ac.horizon.artcodes.storage.ExperienceStorage;
import uk.ac.horizon.artcodes.storage.StoreListener;

import java.util.List;
import java.util.Map;

public class ExperienceSelectFragment extends Fragment
{
	private static final int RECENT_MAX = 3;
	private ExperienceSelectBinding binding;
	private ExperienceSelectGroupBinding recentBinding;
	private ExperienceSelectGroupBinding nearbyBinding;
	private ExperienceSelectGroupBinding newBinding;
	private ExperienceSelectGroupBinding popularBinding;

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceSelectBinding.inflate(inflater, container, false);
		if (Feature.get(getActivity(), R.bool.feature_show_welcome).isEnabled())
		{
			binding.welcome.setVisibility(View.VISIBLE);
			binding.dismissButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					binding.welcome.setVisibility(View.GONE);
					Feature.get(getActivity(), R.bool.feature_show_welcome).setEnabled(false);
				}
			});
			binding.moreButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://aestheticodes.com/info/")));
				}
			});
		}
		else
		{
			binding.welcome.setVisibility(View.GONE);
		}

		recentBinding = ExperienceSelectGroupBinding.inflate(inflater, binding.layout, false);
		recentBinding.title.setText(R.string.recent);
		nearbyBinding = ExperienceSelectGroupBinding.inflate(inflater, binding.layout, false);
		nearbyBinding.title.setText(R.string.hint_near);
		newBinding = ExperienceSelectGroupBinding.inflate(inflater, binding.layout, false);
		newBinding.title.setText(R.string.hint_new);
		popularBinding = ExperienceSelectGroupBinding.inflate(inflater, binding.layout, false);
		popularBinding.title.setText(R.string.hint_popular);
		binding.layout.addView(recentBinding.getRoot());
		binding.layout.addView(nearbyBinding.getRoot());
		binding.layout.addView(newBinding.getRoot());
		binding.layout.addView(popularBinding.getRoot());

		loadExperiences(inflater);

		return binding.getRoot();
	}

	private void loadExperiences(final LayoutInflater inflater)
	{
		final List<String> allRecentItems = ExperienceListStore.with(getActivity(), "recent").get();
		updateGroup(inflater, recentBinding, allRecentItems.subList(0, Math.min(RECENT_MAX, allRecentItems.size())));
		ExperienceStorage.loadRecommended().async(new StoreListener<Map<String, List<String>>>()
		{
			@Override
			public void onItemChanged(Map<String, List<String>> experiences)
			{
				updateGroup(inflater, nearbyBinding, experiences.get("nearby"));
				updateGroup(inflater, newBinding, experiences.get("new"));
				updateGroup(inflater, popularBinding, experiences.get("popular"));
			}
		});
	}

	private void startActivity(Class<?> activity, Experience experience)
	{
		Intent intent = new Intent(getActivity(), activity);
		intent.putExtra("experience", ExperienceParserFactory.toJson(experience));
		startActivity(intent);
	}

	private void updateGroup(LayoutInflater inflater, final ExperienceSelectGroupBinding binding, List<String> ids)
	{
		if (ids == null || ids.isEmpty())
		{
			binding.getRoot().setVisibility(View.GONE);
		}
		else
		{
			binding.items.removeAllViews();
			for (final String uri : ids)
			{
				final ExperienceItemBinding experienceBinding = ExperienceItemBinding.inflate(inflater, binding.items, false);
				experienceBinding.getRoot().setVisibility(View.GONE);
				Log.i("", uri);
				ExperienceStorage.load(Experience.class).fromUri(uri).async(new StoreListener<Experience>()
				{
					@Override
					public void onItemChanged(final Experience experience)
					{
						experienceBinding.setExperience(experience);
						experienceBinding.getRoot().setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								startActivity(ExperienceActivity.class, experience);
							}
						});
						experienceBinding.scanButton.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								startActivity(ArtcodeActivity.class, experience);
							}
						});
						experienceBinding.getRoot().setVisibility(View.VISIBLE);
						binding.getRoot().setVisibility(View.VISIBLE);
					}
				});

				binding.items.addView(experienceBinding.getRoot());
			}
		}
	}

//	@Override
//	protected void onStart()
//	{
//		super.onStart();
//		GoogleAnalytics.trackScreen("Experience Select Screen");
//	}
}
