/*
 * Artcodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.artcodes.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.LoadCallback;

public abstract class ExperienceActivityBase extends ArtcodeActivityBase implements LoadCallback<Experience>
{
	private String uri;
	private Experience experience;

	public Experience getExperience()
	{
		return experience;
	}

	@Override
	public void loaded(Experience item)
	{
		Log.i("experience", "Experience loaded");
		experience = item;
		if (experience != null)
		{
			uri = experience.getId();
		}
	}

	String getUri()
	{
		return uri;
	}

	protected boolean isLoaded()
	{
		return experience != null;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey("experience"))
		{

			loaded(new Gson().fromJson(savedInstanceState.getString("experience"), Experience.class));
		}
		else
		{
			Intent intent = getIntent();
			if (intent.hasExtra("experience"))
			{
				loaded(new Gson().fromJson(intent.getStringExtra("experience"), Experience.class));
			}
			else
			{
				final Uri data = intent.getData();
				if (data != null)
				{
					uri = data.toString();
					if(uri.contains("://aestheticodes.appspot.com/experience/info"))
					{
						uri = uri.replace("://aestheticodes.appspot.com/experience/info", "://aestheticodes.appspot.com/experience");
					}
					getServer().loadExperience(uri, this);
				}
			}
		}
	}
}
