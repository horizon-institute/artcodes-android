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

package uk.ac.horizon.artcodes.adapter;

import android.content.Context;
import android.util.SparseArray;

import java.util.List;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceListAdapter extends ExperienceAdapter
{
	private final SparseArray<Experience> experiences = new SparseArray<>();

	public ExperienceListAdapter(Context context, ArtcodeServer server)
	{
		super(context, server);
	}

	@Override
	public int getViewCount()
	{
		return experiences.size();
	}

	@Override
	public Experience getExperience(final int position)
	{
		return experiences.get(position);
	}

	@Override
	public void loaded(final List<String> item)
	{
		experiences.clear();
		adapter.notifyDataSetChanged();
		for (int index = 0; index < item.size(); index++)
		{
			final int experienceIndex = index;
			final String uri = item.get(index);
			loadStarted();
			server.loadExperience(uri, new LoadCallback<Experience>()
			{
				@Override
				public void loaded(Experience item)
				{
					loadFinished();
					experiences.put(experienceIndex, item);
					adapter.notifyItemInserted(experiences.indexOfValue(item));
				}
			});
		}
		loadFinished();
	}
}
