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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.NavigationActivity;
import uk.ac.horizon.artcodes.adapter.ExperienceGroupAdapter;
import uk.ac.horizon.artcodes.databinding.ListBinding;

public class ExperienceRecommendFragment extends ArtcodeFragmentBase
{
	private static final int LOCATION_PERMISSION_REQUEST = 87;

	private ExperienceGroupAdapter adapter;

	@NonNull
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListBinding binding = ListBinding.inflate(inflater, container, false);
		adapter = new ExperienceGroupAdapter(getActivity(), getServer());
		binding.setAdapter(adapter);
		return binding.getRoot();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		GoogleAnalytics.trackScreen("View Recommended");
		getActivity().setTitle(R.string.nav_home);

		loadExperiences();
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

	private void loadExperiences()
	{
		loadLocal();
		adapter.loadStarted();
		getServer().loadRecommended(adapter.getCallback(), getLocation());
	}

	private void navigateTo(Fragment fragment)
	{
		if(getActivity() instanceof NavigationActivity)
		{
			NavigationActivity activity = (NavigationActivity) getActivity();
			activity.navigate(fragment, true);
		}
	}

	private void loadLocal()
	{
		adapter.loadStarted();
		getServer().loadRecent(adapter.getCallback("recent", new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				navigateTo(new ExperienceRecentFragment());
			}
		}));
		adapter.loadStarted();
		getServer().loadStarred(adapter.getCallback("starred", new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				navigateTo(new ExperienceStarFragment());
			}
		}));
	}

	@Nullable
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
}
