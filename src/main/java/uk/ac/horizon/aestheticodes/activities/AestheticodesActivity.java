/*
 * uk.ac.horizon.aestheticodes.Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  uk.ac.horizon.aestheticodes.Aestheticodes
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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListAdapter;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListController;
import uk.ac.horizon.aestheticodes.core.activities.ScanActivity;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

import java.util.List;

public class AestheticodesActivity extends ScanActivity implements ExperienceListController.Listener
{
	private static final String TAG = AestheticodesActivity.class.getName();

	private ExperienceListAdapter experiences;

	@Override
	public void experienceListChanged()
	{
		String selectedID = getPreferences(Context.MODE_PRIVATE).getString("experience", experience.get().getId());
		Experience newSelected = experiences.getSelected(selectedID);
		if (newSelected != null && newSelected != experience.get())
		{
			experience.set(newSelected);
		}
	}

	@Override
	public void experienceSelected(Experience experience)
	{
		super.experienceSelected(experience);

		List<Experience> experienceList = experiences.getExperiences();
		int index = experienceList.indexOf(experience);
		getSupportActionBar().setSelectedNavigationItem(index);
	}

	@Override
	public void markerFound(String markerCode)
	{
		Marker marker = experience.get().getMarkers().get(markerCode);
		if (marker != null)
		{
			camera.stop();
			if (marker.getShowDetail())
			{
				Intent intent = new Intent(this, MarkerActivity.class);
				intent.putExtra("experience", experience.get().getId());
				intent.putExtra("marker", markerCode);

				startActivity(intent);
			}
			else
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getAction())));
			}
		}
		else
		{
			Log.w(TAG, "No details for marker " + markerCode);

			// TODO if (experienceManager.get().canAddMarkerByScanning())
			//{
			//	this.addMarkerDialog(marker.getCodeKey());
			//}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		experiences = new ExperienceListAdapter(getSupportActionBar().getThemedContext(), Aestheticodes.getExperiences());
		//noinspection deprecation
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//noinspection deprecation
		getSupportActionBar().setListNavigationCallbacks(experiences, new ActionBar.OnNavigationListener()
		{
			@Override
			public boolean onNavigationItemSelected(int position, long l)
			{
				final Experience selected = (Experience) experiences.getItem(position);
				if (selected != experience.get())
				{
					experience.set(selected);
					getPreferences(Context.MODE_PRIVATE).edit().putString("experience", selected.getId()).commit();
				}
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.experiences:
				startActivity(new Intent(this, ExperienceListActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		experiences.addListener(this);
		experienceListChanged();
		experiences.update();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		experiences.removeListener(this);
	}
}