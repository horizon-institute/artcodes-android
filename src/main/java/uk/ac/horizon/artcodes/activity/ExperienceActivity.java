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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import uk.ac.horizon.artcodes.AnalyticsTrackers;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.artcodes.controller.ExperienceLoader;
import uk.ac.horizon.aestheticodes.databinding.ExperienceBinding;
import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceActivity extends AppCompatActivity
{
	private String experienceID;
	private ExperienceBinding binding;

	public void editExperience(View view)
	{
		Intent intent = new Intent(ExperienceActivity.this, ExperienceEditActivity.class);
		intent.putExtra("experience", experienceID);

		startActivity(intent);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
		tracker.setScreenName("Experience " + experienceID + " Screen");
		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	public void shareExperience(View view)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		if(binding.getExperience() != null)
		{
			intent.putExtra(Intent.EXTRA_SUBJECT, binding.getExperience().getName());
		}
		intent.putExtra(Intent.EXTRA_TEXT, "http://aestheticodes.appspot.com/experience/info/" + experienceID);
		Intent openInChooser = Intent.createChooser(intent, "Share with...");
		startActivity(openInChooser);
	}

	public void openExperience(View view)
	{

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		binding = DataBindingUtil.setContentView(this, R.layout.experience);

		// TODO bindView(R.id.openExperience, new TintAdapter<Experience>("image"));
		//bindView(R.id.openExperience, new TintAdapter<Experience>("image"));

		final Bundle extras = getIntent().getExtras();
		experienceID = extras.getString("experience");
		new ExperienceLoader(this)
		{
			@Override
			protected void onProgressUpdate(Experience... values)
			{
				if(values != null && values.length != 0)
				{
					binding.setExperience(values[0]);
				}
			}
		}.execute(experienceID);
	}
}
