/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.settings.AddMarkerSettingsItem;

import java.util.List;

public class ExperienceStoreActivity extends ActionBarActivity implements ExperienceEventListener
{
	private ExperienceManager experienceManager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.i("", getIntent().toString());
		String experienceID = getIntent().getData().getHost();

		experienceManager = ExperienceManager.get(this);

		String code = getIntent().getData().getLastPathSegment();
		Log.i("", "Code: " + code);
		if (code != null && !code.isEmpty())
		{
			final AddMarkerSettingsItem.AddMarkerDialogFragment dialogFragment = new AddMarkerSettingsItem.AddMarkerDialogFragment();
			final Bundle bundle = new Bundle();
			bundle.putString("code", code);
			dialogFragment.setArguments(bundle);
			dialogFragment.show(getSupportFragmentManager(), "missiles");
		}

		final ActionBar actionBar = getSupportActionBar();

		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener()
		{
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
			{
				// show the given tab
			}

			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
			{
				// hide the given tab
			}

			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
			{
				// probably ignore this event
			}
		};

		// Add 3 tabs, specifying the tab's text and TabListener
		for (int i = 0; i < 3; i++)
		{
			actionBar.addTab(
					actionBar.newTab()
							.setText("Tab " + (i + 1))
							.setTabListener(tabListener));
		}

	}

	@Override
	public void experienceSelected(Experience experience)
	{

	}

	@Override
	public void experiencesChanged()
	{
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		experienceManager.removeListener(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		experienceManager.addListener(this);
	}

	@Override
	public void markersFound(List<Marker> markers)
	{

	}
}