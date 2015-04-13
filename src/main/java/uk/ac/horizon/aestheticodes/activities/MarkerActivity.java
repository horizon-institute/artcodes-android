/*
 * Aestheticodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.aestheticodes.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListController;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.properties.bindings.ColorImageBinding;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.properties.Properties;
import uk.ac.horizon.aestheticodes.properties.bindings.VisibilityBinding;

public class MarkerActivity extends ActionBarActivity
{
	private Marker marker;
	private Properties properties;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.marker);

		final Bundle extras = getIntent().getExtras();

		String markerCode = extras.getString("marker");
		String experienceID = extras.getString("experience");

		ExperienceListController experiences = Aestheticodes.getExperiences();
		Experience experience = experiences.get(experienceID);
		marker = experience.getMarkers().get(markerCode);

		properties = new Properties(this, marker);
		properties.get("title")
				.defaultTo(getString(R.string.code_text, marker.getCode()))
				.bindTo(R.id.markerTitle);
		properties.get("description")
				.defaultTo(marker.getAction())
				.bindTo(R.id.markerDescription);
		properties.get("action").bindTo(new VisibilityBinding(R.id.markerAction));
		properties.get("image").bindTo(R.id.markerImage).bindTo(new ColorImageBinding(R.id.markerAction));
		properties.load();
	}

	public void open(View view)
	{
		if(marker != null && marker.getAction() != null && !marker.getAction().isEmpty())
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getAction())));
		}
	}
}
