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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.settings.ActivitySettingsItem;
import uk.ac.horizon.aestheticodes.settings.AddMarkerSettingsItem;
import uk.ac.horizon.aestheticodes.settings.MarkerSettingsItem;
import uk.ac.horizon.aestheticodes.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MarkerListActivity extends SettingsActivity
{
	private ExperienceManager experienceManager;
	private Experience experience;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.i("", getIntent().toString());
		String experienceID = getIntent().getData().getHost();

		experienceManager = new ExperienceManager(this);
		experience = experienceManager.get(experienceID);

		getSupportActionBar().setTitle(getString(R.string.marker_title, experience.getName()));
		if (experience.getIcon() != null)
		{
			Picasso.with(this).load(experience.getIcon()).into(new Target()
			{
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
				{
					getSupportActionBar().setIcon(new BitmapDrawable(getResources(), bitmap));
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable)
				{

				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable)
				{

				}
			});
		}

		if (this.getIntent().hasExtra("code"))
		{
			String code = this.getIntent().getStringExtra("code");
			final AddMarkerSettingsItem.AddMarkerDialogFragment dialogFragment = new AddMarkerSettingsItem.AddMarkerDialogFragment();
			dialogFragment.presetCode(code);
			dialogFragment.show(this.getSupportFragmentManager(), "missiles");
		}

		refresh();
	}

	@Override
	public void refresh()
	{
		adapter.clear();
		final List<MarkerAction> actions = new ArrayList<MarkerAction>(experience.getMarkers().values());
		Collections.sort(actions, new Comparator<MarkerAction>()
		{
			@Override
			public int compare(MarkerAction markerAction, MarkerAction markerAction2)
			{
				return markerAction.getCode().compareTo(markerAction2.getCode());
			}
		});
		for (MarkerAction action : actions)
		{
			if (action.isVisible())
			{
				adapter.add(new MarkerSettingsItem(this, experience, action));
			}
		}

		if (experience.canAddMarker())
		{
			adapter.add(new AddMarkerSettingsItem(this, experience, "Add Marker"));
		}

		if(experience.isEditable())
		{
			Intent intent = new Intent(this, MarkerSettingsActivity.class);
			intent.putExtra("experience", experience.getId());

			adapter.add(new ActivitySettingsItem(this, "Settings", intent));
		}

		Intent intent = new Intent(this, AboutActivity.class);

		adapter.add(new ActivitySettingsItem(this, "About", intent));

		adapter.notifyDataSetChanged();

		experienceManager.add(experience);
	}
}