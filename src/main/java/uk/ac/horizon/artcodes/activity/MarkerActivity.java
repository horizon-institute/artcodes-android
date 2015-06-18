/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.horizon.artcodes.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import uk.ac.horizon.artcodes.AnalyticsTrackers;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.artcodes.controller.ExperienceLoader;
import uk.ac.horizon.aestheticodes.databinding.MarkerBinding;
import uk.ac.horizon.artcodes.model.Marker;
import uk.ac.horizon.artcodes.model.Experience;

public class MarkerActivity extends AppCompatActivity
{
	private String experienceID;
	private String markerCode;
	private MarkerBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.marker);

		binding = DataBindingUtil.setContentView(this, R.layout.marker);

		//bindView(R.id.markerImage, "image");
		//bindView(R.id.markerAction, new VisibilityAdapter<Marker>("action"));
		//bindView(R.id.markerAction, new TintAdapter<Marker>("image"));

		final Bundle extras = getIntent().getExtras();

		markerCode = extras.getString("marker");
		experienceID = extras.getString("experience");

		new ExperienceLoader(this)
		{
			@Override
			protected void onProgressUpdate(Experience... values)
			{
				if(values != null && values.length != 0)
				{
					Experience experience = values[0];
					if(experience != null)
					{
						Marker marker = experience.getMarker(markerCode);
						binding.setMarker(marker);
						if(marker != null)
						{
							Ion.with(MarkerActivity.this).load(marker.getImage()).intoImageView(binding.markerImage);
						}
					}
				}
			}
		}.execute(experienceID);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
		tracker.setScreenName("Marker " + markerCode + " @ " + experienceID + " Screen");
		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	public void open(View view)
	{
		if(binding.getMarker() != null && binding.getMarker().getAction() != null)
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(binding.getMarker().getAction())));
		}
	}
}
