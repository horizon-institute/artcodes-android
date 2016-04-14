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
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;

import com.google.common.collect.Ordering;

import java.util.List;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceSortedListAdapter extends ExperienceAdapter
{
	private static final Ordering<String> STRING_ORDERING =
			Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();
	private final SortedList<Experience> experiences;

	public ExperienceSortedListAdapter(Context context, ArtcodeServer server)
	{
		super(context, server);
		experiences = new SortedList<>(Experience.class, new SortedListAdapterCallback<Experience>(adapter)
		{
			@Override
			public boolean areContentsTheSame(Experience oldItem, Experience newItem)
			{
				return TextUtils.equals(oldItem.getId(), newItem.getId());
			}

			@Override
			public boolean areItemsTheSame(Experience item1, Experience item2)
			{
				return TextUtils.equals(item1.getId(), item2.getId());
			}

			@Override
			public int compare(Experience o1, Experience o2)
			{
				int result = STRING_ORDERING.compare(o1.getName(), o2.getName());
				if (result != 0)
				{
					return result;
				}
				return STRING_ORDERING.compare(o1.getId(), o2.getId());
			}
		});
	}

	@Override
	public Experience getExperience(final int position)
	{
		return experiences.get(position);
	}

	@Override
	public int getViewCount()
	{
		return experiences.size();
	}

	@Override
	public void loaded(final List<String> item)
	{
		for (String uri : item)
		{
			loadStarted();
			server.loadExperience(uri, new LoadCallback<Experience>()
			{
				@Override
				public void loaded(Experience item)
				{
					loadFinished();
					experiences.add(item);
				}

				@Override
				public void error(Throwable e)
				{
					loadFinished();
					showError(context.getString(R.string.connection_error));
				}
			});
		}
		loadFinished();
	}
}
