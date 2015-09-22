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

import android.os.Bundle;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.IntentSource;
import uk.ac.horizon.artcodes.request.RequestCallback;

public abstract class ExperienceActivityBase extends ArtcodeActivityBase implements RequestCallback<Experience>
{
	private String uri;
	private Experience experience;

	public Experience getExperience()
	{
		return experience;
	}

	public String getUri()
	{
		return uri;
	}

	protected boolean isLoaded()
	{
		return experience != null;
	}

	@Override
	public void onResponse(Experience item)
	{
		experience = item;
		if (experience != null)
		{
			uri = experience.getId();
		}
	}

	@Override
	public void onError(Exception e)
	{
		GoogleAnalytics.trackException(e);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		new IntentSource<Experience>(getServer(), getIntent(), savedInstanceState, Experience.class).loadInto(this);
	}
}
