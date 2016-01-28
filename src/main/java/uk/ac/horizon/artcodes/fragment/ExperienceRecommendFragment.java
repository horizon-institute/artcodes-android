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

package uk.ac.horizon.artcodes.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.SectionedExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceRecommendBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceRecommendFragment extends ArtcodeFragmentBase
{
	private static final int LOCATION_PERMISSION_REQUEST = 87;
	private static final int RECENT_MAX = 3;
	private SectionedExperienceAdapter adapter;
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

		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			loadExperiences();
		}
		else
		{
			requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
		}

		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		GoogleAnalytics.trackScreen("View Recommended");
	}

	private void loadExperiences()
	{
		binding.progress.addPending();
		getServer().loadRecent(new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> item)
			{
				updateGroup("recent", item.subList(0, Math.min(RECENT_MAX, item.size())));
				binding.progress.removePending();
			}
		});

		binding.progress.addPending();
		getServer().loadRecommended(new LoadCallback<Map<String, List<String>>>()
		{
			@Override
			public void loaded(Map<String, List<String>> item)
			{
				// Why? ((NavigationActivity) getActivity()).updateAccounts();
				for (String group : item.keySet())
				{
					updateGroup(group, item.get(group));
				}
				binding.progress.removePending();
			}
		}, getLocation());
	}

	private Location getLocation()
	{
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			final LocationManager locationManager = (LocationManager) getContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			float accuracy = Float.MAX_VALUE;
			Location location = null;
			for (String provider : locationManager.getProviders(new Criteria(), true))
			{
				Location newLocation = locationManager.getLastKnownLocation(provider);
				if (newLocation != null)
				{
					if (newLocation.getAccuracy() < accuracy)
					{
						accuracy = newLocation.getAccuracy();
						location = newLocation;
					}
				}
			}

			return location;
		}
		else
		{
			Log.i("location", "No location permission");

		}
		return null;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case LOCATION_PERMISSION_REQUEST:
			{
				loadExperiences();
			}
		}
	}

	private void updateGroup(final String group, List<String> ids)
	{
		int index = 0;
		for (final String uri : ids)
		{
			final int experienceIndex = index;
			binding.progress.addPending();
			getServer().loadExperience(uri, new LoadCallback<Experience>()
			{
				@Override
				public void loaded(final Experience experience)
				{
					binding.progress.removePending();
					adapter.addExperience(experience, group, experienceIndex);
				}
			});
			index++;
		}
	}
}
