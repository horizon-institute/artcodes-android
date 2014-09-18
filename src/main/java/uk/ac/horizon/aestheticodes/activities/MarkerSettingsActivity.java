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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.settings.IntPropertySettingsItem;
import uk.ac.horizon.aestheticodes.settings.Property;
import uk.ac.horizon.aestheticodes.settings.SettingsActivity;

import java.util.List;

public class MarkerSettingsActivity extends SettingsActivity implements ExperienceEventListener
{
	private ExperienceManager experienceManager;
	private Experience experience;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		String experienceID = extras.getString("experience");

		experienceManager = ExperienceManager.get(this);
		experience = experienceManager.get(experienceID);

		adapter.add(new IntPropertySettingsItem(this, experience, "minRegions", 1, "maxRegions"));
		adapter.add(new IntPropertySettingsItem(this, experience, "maxRegions", "minRegions", 12));
		adapter.add(new IntPropertySettingsItem(this, experience, "maxRegionValue", 1, 9));
		adapter.add(new IntPropertySettingsItem(this, experience, "maxEmptyRegions", 0, "maxRegions", 0));
		adapter.add(new IntPropertySettingsItem(this, experience, "validationRegions", 0, "maxRegions", 0));
		adapter.add(new IntPropertySettingsItem(this, experience, "validationRegionValue", 1, "maxRegionValue"));
		adapter.add(new IntPropertySettingsItem(this, experience, "checksumModulo", 1, 12, 1));

		adapter.notifyDataSetChanged();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.marker_settings_title, experience.getName()));
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

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// Respond to the action bar's Up/Home open_button
			case android.R.id.home:
				NavUtils.navigateUpTo(this, new Intent(Intent.ACTION_EDIT, Uri.parse("aestheticodes://" + experience.getId())));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void saveChanges()
	{
		experienceManager.add(experience);
	}


	@Override
	public void setProperty(String propertyName, Object value)
	{
		Property property = new Property(experience, propertyName);
		property.set(value);
		experienceManager.add(experience);
	}


	@Override
	public void experienceSelected(Experience experience)
	{

	}

	@Override
	public void experiencesChanged()
	{
		refresh();
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
		refresh();
	}

	@Override
	public void markersFound(List<Marker> markers)
	{

	}
}
