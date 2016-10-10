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

import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
				return oldItem.equals(newItem);
			}

			@Override
			public boolean areItemsTheSame(Experience item1, Experience item2)
			{
				return item1.equals(item2);
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
	public void loaded(final List<String> items)
	{
		// If this adapter is empty, wait for all experiences to load before adding to adapter.
		// This is because if the experiences are added in non-alphabetical order the user can
		// be left in the middle of the list rather than the start.
		final boolean batchUpdate = experiences.size() == 0;
		final List<Experience> experiencesToBatchUpdate = new ArrayList<>();
		final int[] count = {0};

		synchronized (experiences)
		{
			for (int i = 0; i < experiences.size(); ++i)
			{
				Experience e = experiences.get(i);
				if (!items.contains(e.getId()))
				{
					experiences.remove(e);
					--i;
				}
			}
		}

		for (String uri : items)
		{
			loadStarted();

			server.loadExperience(uri, new LoadCallback<Experience>()
			{
				@Override
				public void loaded(Experience experience)
				{
					loadFinished();
					synchronized (experiences)
					{
						if (batchUpdate)
						{
							experiencesToBatchUpdate.add(experience);
							if (++count[0] == items.size())
							{
								Collections.sort(experiencesToBatchUpdate, new Comparator<Experience>()
								{
									@Override
									public int compare(Experience experience1, Experience experience2)
									{
										return (experience1.getName()==null ? "" : experience1.getName()).compareTo(experience2.getName()==null ? "" : experience2.getName());
									}
								});
								for (Experience experienceFromBatchUpdate : experiencesToBatchUpdate)
								{
									experiences.add(experienceFromBatchUpdate);
								}
								experiencesToBatchUpdate.clear();
								count[0] = 0;
							}
						}
						else
						{
							int index = -1;
							for (int i = 0; i < experiences.size(); ++i)
							{
								if (experience.equals(experiences.get(i)))
								{
									index = i;
									break;
								}
							}
							// experiences.indexOf(item) seems to be buggy.
							if (index > -1)
							{
								// experiences.updateItemAt() is ignored if a.equals(b)
								experiences.removeItemAt(index);
								experiences.add(experience);
							}
							else
							{
								experiences.add(experience);
							}
						}
					}
				}

				@Override
				public void error(Throwable e)
				{
					loadFinished();
					showError(context.getString(R.string.connection_error));

					if (batchUpdate && ++count[0] == items.size())
					{
						for (Experience experienceFromBatchUpdate : experiencesToBatchUpdate)
						{
							experiences.add(experienceFromBatchUpdate);
						}
					}
				}
			});
		}

		loadFinished();
	}
}
