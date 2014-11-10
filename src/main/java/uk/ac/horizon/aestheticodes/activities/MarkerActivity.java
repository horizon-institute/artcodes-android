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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.bindings.DefaultPropertyBinding;
import uk.ac.horizon.aestheticodes.bindings.ViewBindings;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.MarkerAction;

public class MarkerActivity extends ActionBarActivity
{
	private MarkerAction marker;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.marker);

		String markerCode = getIntent().getData().getLastPathSegment();
		String experienceID = getIntent().getData().getHost();

		ExperienceManager experienceManager = ExperienceManager.get(this);
		Experience experience = experienceManager.get(experienceID);
		marker = experience.getMarkers().get(markerCode);

		ViewBindings viewBindings = new ViewBindings(this, marker);

		if (experience.getIcon() != null)
		{
			Picasso.with(this).load(experience.getIcon()).into(new ActionBarTarget(this));
		}

		viewBindings.bind(R.id.markerTitle, new DefaultPropertyBinding("title", getString(R.string.code_text, marker.getCode())));
		viewBindings.bind(R.id.markerImage, "image");
		viewBindings.bind(R.id.markerDescription, new DefaultPropertyBinding("description", marker.getAction()));
	}

	public void open(View view)
	{
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getAction())));
	}
}
