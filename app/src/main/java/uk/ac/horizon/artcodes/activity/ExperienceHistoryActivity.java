/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.gson.Gson;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.ScanEventAdapter;
import uk.ac.horizon.artcodes.databinding.ListBinding;
import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceHistoryActivity extends ExperienceActivityBase
{
	private ScanEventAdapter adapter;

	public static void start(Context context, Experience experience)
	{
		Intent intent = new Intent(context, ExperienceHistoryActivity.class);
		intent.putExtra("experience", new Gson().toJson(experience));
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.container);

		final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		final ViewGroup content = (ViewGroup)findViewById(R.id.content);

		final ListBinding binding = ListBinding.inflate(getLayoutInflater(), content, false);
		adapter = new ScanEventAdapter(this);
		binding.setAdapter(adapter);

		content.addView(binding.getRoot());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, ExperienceActivity.intent(this, getExperience()));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void loaded(Experience experience)
	{
		super.loaded(experience);

		adapter.setHistory(getServer().getScanHistory(experience.getId()));
	}
}
