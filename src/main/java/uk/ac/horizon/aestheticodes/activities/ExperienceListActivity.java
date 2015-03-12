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

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListAdapter;
import uk.ac.horizon.aestheticodes.model.Experience;

public class ExperienceListActivity extends ActionBarActivity
{
	private final Handler handler = new Handler();
	private final Runnable downloadClear = new Runnable()
	{
		@Override
		public void run()
		{
			downloadItem.setIcon(R.drawable.ic_cloud_download_white_24dp);
		}
	};
	private final Runnable downloadOver = new Runnable()
	{
		@Override
		public void run()
		{
			if (experiences.getStatus() == AsyncTask.Status.FINISHED)
			{
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					downloadItem.setActionView(null);
				}
				downloadItem.setIcon(R.drawable.ic_cloud_done_white_24dp);
				handler.removeCallbacksAndMessages(null);
				handler.postDelayed(downloadClear, 2000);

			}
			else
			{
				handler.removeCallbacksAndMessages(null);
				handler.postDelayed(downloadClear, 1000);
			}
		}
	};
	private ExperienceListAdapter experiences;
	private MenuItem downloadItem = null;

	public void addExperience(View view)
	{
		startActivity(new Intent(this, ExperienceEditActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.experience_list_actions, menu);

		downloadItem = menu.findItem(R.id.action_download);

		updateDownloadStatus();

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_download:
				experiences.update();
				updateDownloadStatus();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.experience_list);

		final ListView listView = (ListView) findViewById(R.id.experienceList);

		experiences = new ExperienceListAdapter(this, Aestheticodes.getExperiences());
		listView.setAdapter(experiences);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Experience experience = (Experience) experiences.getItem(position);
				if (experience != null)
				{
					Intent intent = new Intent(ExperienceListActivity.this, ExperienceActivity.class);
					intent.putExtra("experience", experience.getId());

					startActivity(intent);
				}
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		experiences.update();
		updateDownloadStatus();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void updateDownloadStatus()
	{
		if (downloadItem != null)
		{
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				if (experiences.getStatus() == AsyncTask.Status.RUNNING)
				{
					downloadItem.setActionView(R.layout.actionbar_indeterminate_progress);
					handler.removeCallbacksAndMessages(null);
					handler.postDelayed(downloadOver, 1000);
				}
				else
				{
					downloadItem.setActionView(null);
				}
			}
		}
	}
}