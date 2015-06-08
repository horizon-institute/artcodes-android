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
import android.view.View;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ControllerActivity;
import uk.ac.horizon.aestheticodes.controllers.ExperienceLoader;
import uk.ac.horizon.aestheticodes.controllers.adapters.TintAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.VisibilityAdapter;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerActivity extends ControllerActivity<Marker>
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.marker);

		bindView(R.id.markerTitle, "title");// TODO, new DefaultValue(getString(R.string.code_text, marker.getCode())));
		bindView(R.id.markerDescription, "description");
		bindView(R.id.markerImage, "image");
		bindView(R.id.markerAction, new VisibilityAdapter<Marker>("action"));
		bindView(R.id.markerAction, new TintAdapter<Marker>("image"));

		final Bundle extras = getIntent().getExtras();

		final String markerCode = extras.getString("marker");
		String experienceID = extras.getString("experience");

		new ExperienceLoader(this)
		{
			@Override
			protected void onProgressUpdate(Experience... values)
			{
				if(values != null && values.length != 0)
				{
					setModel(values[0].getMarker(markerCode));
				}
			}
		}.execute(experienceID);
	}

	public void open(View view)
	{
		if(getModel() != null && getModel().getAction() != null && !getModel().getAction().isEmpty())
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getModel().getAction())));
		}
	}
}
