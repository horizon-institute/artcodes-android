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
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceSelectBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceSelectItemBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.loader.ExperienceLoader;
import uk.ac.horizon.artcodes.model.loader.LoadListener;
import uk.ac.horizon.artcodes.model.loader.Ref;

import java.util.List;

public class ExperienceSelectActivity extends AppCompatActivity
{
	private ExperienceSelectBinding binding;
	private String testExperienceID = "http://aestheticodes.appspot.com/experience/4c758e29-0759-4583-a0d4-71ee692b7f86";

	public void dismissWelcome(View view)
	{
		binding.setWelcomeHidden(true);
	}

	public void openTestExperience(View view)
	{
		Intent intent = new Intent(this, ExperienceActivity.class);
		intent.setData(Uri.parse(testExperienceID));
		startActivity(intent);
	}

	public void scanTestExperience(View view)
	{
		Intent intent = new Intent(this, ArtcodeActivity.class);
		intent.setData(Uri.parse(testExperienceID));
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.experience_select);
		binding.setWelcomeHidden(false);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		final int width = 3;

		List<Ref<Experience>> experiences = ExperienceLoader.from(RecentExperiences.with(this).get(), width);
		for (final Ref<Experience> experience : experiences)
		{
			final ExperienceSelectItemBinding experienceBinding = ExperienceSelectItemBinding.inflate(getLayoutInflater(), binding.recentExperiences, false);
			experience.load(this, new LoadListener<Experience>()
			{
				@Override
				public void onLoaded(Experience item)
				{
					Log.i("", "Recent experience loaded " + experience.getUri() + " " + item);
					experienceBinding.setExperience(item);
				}
			});
			experienceBinding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					ExperienceLoader.startActivity(ExperienceActivity.class, experience, ExperienceSelectActivity.this);
				}
			});
			binding.recentExperiences.addView(experienceBinding.getRoot());
		}

		if (experiences.isEmpty())
		{

		}
		else if (experiences.size() < width)
		{
			Space space = new Space(this);
			space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, width - experiences.size()));
			binding.recentExperiences.addView(space);
		}

//		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		Criteria criteria = new Criteria();
//		String provider = locationManager.getBestProvider(criteria, true);
//		Location devicelocation = locationManager.getLastKnownLocation(provider);
//
//		Ion.with(this).load("").as(new TypeToken<List<Experience>>() {}).setCallback(new FutureCallback<List<Experience>>()
//		{
//			@Override
//			public void onCompleted(Exception e, List<Experience> result)
//			{
//				if (result != null && !result.isEmpty())
//				{
//					LinearLayout layout = null;
//					int index = 0;
//					for (Experience experience : result)
//					{
//						if (layout == null || index % width == 0)
//						{
//							layout = (LinearLayout) getLayoutInflater().inflate(R.layout.experience_select_group, binding.recommendedExperiences, false);
//							binding.recentExperiences.addView(layout);
//						}
//
//						final ExperienceSelectItemBinding experienceBinding = ExperienceSelectItemBinding.inflate(getLayoutInflater(), layout, false);
//						experienceBinding.setExperience(experience);
//						layout.addView(experienceBinding.getRoot());
//
//						index++;
//					}
//				}
//			}
//		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalytics.trackScreen("Experience Select Screen");
	}
}
