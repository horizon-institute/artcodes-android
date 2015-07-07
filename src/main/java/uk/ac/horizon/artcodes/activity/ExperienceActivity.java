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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.loader.ExperienceLoader;
import uk.ac.horizon.artcodes.model.loader.LoadListener;
import uk.ac.horizon.artcodes.model.loader.Ref;

public class ExperienceActivity extends AppCompatActivity
{
	private Ref<Experience> experience;
	private ExperienceBinding binding;

	public void editExperience(View view)
	{
		ExperienceLoader.startActivity(ExperienceEditActivity.class, experience, this);
	}

	public void scanExperience(View view)
	{
		Log.i("", "Scan");
		ExperienceLoader.startActivity(ArtcodeActivity.class, experience, this);
	}

	public void shareExperience(View view)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		if (experience.isLoaded())
		{
			intent.putExtra(Intent.EXTRA_SUBJECT, experience.get().getName());
		}
		intent.putExtra(Intent.EXTRA_TEXT, experience.getUri());
		Intent openInChooser = Intent.createChooser(intent, "Share with...");
		startActivity(openInChooser);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.experience);

		// TODO bindView(R.id.openExperience, new TintAdapter<Experience>("image"));

		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(final Intent intent)
	{
		super.onNewIntent(intent);
		experience = ExperienceLoader.from(intent);
		experience.load(this, new LoadListener<Experience>()
		{
			@Override
			public void onLoaded(Experience item)
			{
				if (item != null)
				{
					GoogleAnalytics.trackEvent("Experience", "Loaded " + item.getId());
				}
				binding.setExperience(item);
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalytics.trackScreen("Experience Screen");
	}
}
