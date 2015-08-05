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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import org.jetbrains.annotations.Nullable;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.SectionedExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceRecommendBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.source.Target;

import java.util.List;
import java.util.Map;

public class ExperienceRecommendFragment extends ArtcodeFragmentBase
{
	private static final int RECENT_MAX = 3;
	private SectionedExperienceAdapter adapter;
	private int progress = 0;
	private ExperienceRecommendBinding binding;

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceRecommendBinding.inflate(inflater, container, false);
		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
		adapter = new SectionedExperienceAdapter(getActivity());
		binding.list.setAdapter(adapter);
		adapter.setShowHeaderItem(Feature.get(getActivity(), R.bool.feature_show_welcome).isEnabled());
		binding.progress.setEnabled(false);
		binding.progress.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				binding.progress.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				if (progress != 0)
				{
					binding.progress.setRefreshing(true);
				}
			}
		});

		loadExperiences();

		return binding.getRoot();
	}

	private void incrementProgress()
	{
		progress += 1;
		binding.progress.setRefreshing(true);
	}

	private void decrementProgress()
	{
		progress -= 1;
		if (progress == 0)
		{
			binding.progress.setRefreshing(false);
		}
	}

	private void loadExperiences()
	{
		incrementProgress();
		getAccount().getRecent().loadInto(new Target<List<String>>()
		{
			@Override
			public void onLoaded(List<String> item)
			{
				decrementProgress();
				updateGroup("recent", item.subList(0, Math.min(RECENT_MAX, item.size())));
			}
		});

		incrementProgress();
		getAccount().getRecommended().loadInto(new Target<Map<String, List<String>>>()
		{
			@Override
			public void onLoaded(Map<String, List<String>> item)
			{
				decrementProgress();
				for (String group : item.keySet())
				{
					updateGroup(group, item.get(group));
				}
			}
		});
	}

	private void updateGroup(final String group, List<String> ids)
	{
		int index = 0;
		for (final String uri : ids)
		{
			final int experienceIndex = index;
			incrementProgress();
			getAccount().getExperience(uri).loadInto(new Target<Experience>()
			{
				@Override
				public void onLoaded(final Experience experience)
				{
					decrementProgress();
					adapter.addExperience(experience, group, experienceIndex);
				}
			});
			index++;
		}
	}
}
