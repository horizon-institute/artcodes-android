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
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.controller.ExperienceManager;

public class ExperienceListActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final ExperienceManager experienceManager = ExperienceManager.get(this);

		setContentView(R.layout.experience_list);

		final ListView listView = (ListView) findViewById(R.id.experienceList);

		final ExperienceAdapter experienceAdapter = new ExperienceAdapter(this, experienceManager);
		listView.setAdapter(experienceAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Experience experience = (Experience) experienceAdapter.getItem(position);
				if(experience != null)
				{
					Intent intent = new Intent(ExperienceListActivity.this, ExperienceActivity.class);
					intent.putExtra("experience", experience.getId());

					startActivity(intent);
				}
			}
		});

		experienceManager.load();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void addExperience(View view)
	{
		startActivity(new Intent(this, ExperienceEditActivity.class));
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}
}