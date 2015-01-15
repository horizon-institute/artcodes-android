/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListController;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListUpdater;
import uk.ac.horizon.aestheticodes.dialogs.IntDialogFragment;
import uk.ac.horizon.aestheticodes.dialogs.IntRangeDialogFragment;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Properties;
import uk.ac.horizon.aestheticodes.properties.IntFormat;
import uk.ac.horizon.aestheticodes.properties.IntRangeFormat;
import uk.ac.horizon.aestheticodes.dialogs.MarkerEditDialog;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.properties.URLFormat;
import uk.ac.horizon.aestheticodes.properties.bindings.ClickBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ExperienceEditActivity extends ActionBarActivity
{
	private Experience experience;
	private LinearLayout markerSettings;
	private ImageView markerSettingsIcon;
	private Properties properties;
	private ExperienceListController experiences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.experience_edit);

		final Bundle extras = getIntent().getExtras();
		experiences = Aestheticodes.getExperiences();
		if(extras != null)
		{
			final String experienceID = extras.getString("experience");
			if (experienceID != null)
			{
				experience = experiences.get(experienceID);
			}
		}

		if(experience == null)
		{
			experience = new Experience();
			experience.setId(UUID.randomUUID().toString());
			experience.setOp(Experience.Operation.create);
		}

		properties = new Properties(this, experience);

		properties.get("name").bindTo(R.id.experienceTitle);
		properties.get("description").bindTo(R.id.experienceDescription);

		properties.get("icon").formatAs(new URLFormat())
				.bindTo(R.id.experienceIcon);
		properties.get("image").formatAs(new URLFormat())
				.bindTo(R.id.experienceImage);

		properties.get("maxRegionValue").formatAs(new IntFormat(1, 9))
				.bindTo(R.id.markerRegionValue)
				.bindTo(new ClickBinding(R.id.markerRegionValue, true) {
					@Override
					public void onClick(View v)
					{
						IntDialogFragment.create(getSupportFragmentManager(), "maxRegionValue");
					}
				});

		properties.get("validationRegions").formatAs(new IntFormat(0, properties.get("maxRegions"), 0))
				.bindTo(R.id.markerValidationRegions)
				.bindTo(new ClickBinding(R.id.markerValidationRegions, true)
				{
					@Override
					public void onClick(View v)
					{
						IntDialogFragment.create(getSupportFragmentManager(), "validationRegions");
					}
				});

		properties.get("validationRegionValue").formatAs(new IntFormat(1, properties.get("maxRegionValue")))
				.bindTo(R.id.markerValidationRegionValue)
				.bindTo(new ClickBinding(R.id.markerValidationRegionValue, true)
				{
					@Override
					public void onClick(View v)
					{
						IntDialogFragment.create(getSupportFragmentManager(), "validationRegionValue");
					}
				});

		properties.get("checksumModulo").formatAs(new IntFormat(1, 12, 1))
				.bindTo(R.id.markerChecksum)
				.bindTo(new ClickBinding(R.id.markerChecksum, true)
				{
					@Override
					public void onClick(View v)
					{
						IntDialogFragment.create(getSupportFragmentManager(), "checksumModulo");
					}
				});

		Format format = new IntRangeFormat(properties.get("minRegions"), properties.get("maxRegions"), 1, 9);
		properties.get("maxRegions").formatAs(format)
				.bindTo(R.id.markerRegions);
		properties.get("minRegions").formatAs(format)
				.bindTo(R.id.markerRegions)
				.bindTo(new ClickBinding(R.id.markerRegions, true)
				{
					@Override
					public void onClick(View v)
					{
						IntRangeDialogFragment.create(getSupportFragmentManager(), "minRegions", "maxRegions");
					}
				});
		properties.load();

		markerSettings = (LinearLayout) findViewById(R.id.markerSettings);
		markerSettingsIcon = (ImageView) findViewById(R.id.markerSettingsIcon);

		updateMarkers();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
	}

	public void updateMarkers()
	{
		final LinearLayout markerList = (LinearLayout) findViewById(R.id.markerList);
		markerList.removeAllViews();
		final List<Marker> markers = new ArrayList<>(experience.getMarkers().values());
		Collections.sort(markers, new Comparator<Marker>()
		{
			@Override
			public int compare(Marker markerAction, Marker markerAction2)
			{
				return markerAction.getCode().compareTo(markerAction2.getCode());
			}
		});
		LayoutInflater inflater = getLayoutInflater();
		for (final Marker marker : markers)
		{
			View view = inflater.inflate(R.layout.marker_listitem, markerList, false);
			Properties markerProperties = new Properties(this, marker, view);
			markerProperties.get("code").bindTo(R.id.markerCode);
			markerProperties.get("action").formatAs(new URLFormat()).bindTo(R.id.markerAction);
			markerProperties.load();
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					DialogFragment newFragment = new MarkerEditDialog();
					Bundle bundle = new Bundle();
					bundle.putString("code", marker.getCode());
					newFragment.setArguments(bundle);
					newFragment.show(getSupportFragmentManager(), "marker.edit");
				}
			});
			markerList.addView(view);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// Respond to the action bar's Up/Home open_button
			case android.R.id.home:
				properties.save();
				experiences.add(experience);
				if(experience.getOp() == null)
				{
					experience.setOp(Experience.Operation.update);
				}
				ExperienceListUpdater.save(this, experiences);
				Intent intent = new Intent(this, ExperienceActivity.class);
				intent.putExtra("experience", experience.getId());

				NavUtils.navigateUpTo(this, intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void toggleMarkerSettings(View view)
	{
		if(markerSettings.getVisibility() == View.VISIBLE)
		{
			markerSettings.setVisibility(View.GONE);
			markerSettingsIcon.setImageResource(R.drawable.ic_expand_more_24dp);
		}
		else
		{
			markerSettings.setVisibility(View.VISIBLE);
			markerSettingsIcon.setImageResource(R.drawable.ic_expand_less_24dp);
		}
	}

	public void addMarker(View view)
	{
		Log.i(ExperienceActivity.class.getName(), "Add Marker");
		DialogFragment newFragment = new MarkerEditDialog();
		newFragment.show(getSupportFragmentManager(), "marker.edit");
	}

	public Properties getProperties()
	{
		return properties;
	}

	public Experience getExperience()
	{
		return experience;
	}
}