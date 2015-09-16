/*
 * Aestheticodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.aestheticodes.controllers;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import uk.ac.horizon.storicodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.properties.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExperienceSelectAdapter extends BaseAdapter implements ExperienceListController.Listener
{
	private static final String DEFAULT_ID = "4c758e29-0759-4583-a0d4-71ee692b7f86";

	private static final int RECENT_MAX = 3;
	private static final int RECOMMENDED_MAX = 4;

	private List<Experience> recent = new ArrayList<>();
	private List<Experience> recommended = new ArrayList<>();

	private final Context context;
	private final LayoutInflater inflater;
	private final ExperienceListController experienceController;
	private static ExperienceListUpdater updater = null;

	public ExperienceSelectAdapter(final Context context, final ExperienceListController experienceController)
	{
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.experienceController = experienceController;
		this.experienceController.addListener(this);
		experienceListChanged();
	}

	@Override
	public void experienceListChanged()
	{
		List<Experience> newExperiences = new ArrayList<>();
		for (Experience experience : experienceController.get())
		{
			if (experience.getOp() != Experience.Operation.remove)
			{
				newExperiences.add(experience);
			}
		}
		Collections.sort(newExperiences, new Comparator<Experience>()
		{
			@Override
			public int compare(Experience experience, Experience experience2)
			{
				if (experience.getName() != null && experience2.getName() != null)
				{
					return experience.getName().compareTo(experience2.getName());
				}
				else
				{
					return 0;
				}
			}
		});

		//experiences = newExperiences;
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		if(recent.size() > 0)
		{
			return 1+ recent.size() + 1 + recommended.size();
		}
		return 1 + recommended.size();
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return super.getItemViewType(position);
	}

	@Override
	public Object getItem(int i)
	{
		if(recent.size() > 0)
		{
			if(i == 0)
			{
				return "Recent";
			}

			if(i <= recent.size())
			{
				return recent.get(i - 1);
			}
		}
		return null;
		//return experiences.get(i);
	}

	@Override
	public long getItemId(final int position)
	{
		return getItem(position).hashCode();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		Object item = getItem(i);
		if(item instanceof String)
		{
			if (view == null)
			{
				view = inflater.inflate(R.layout.experience_header, viewGroup, false);
			}

			final TextView textView = (TextView)view.findViewById(R.id.headerText);

			//.bindTo(R.id.headerText);
		}
		else if(item instanceof Experience)
		{
			if (view == null)
			{
				view = inflater.inflate(R.layout.experience_listitem, viewGroup, false);
			}

			final Properties experienceProperties = new Properties(context, item, view);
			experienceProperties.get("name").bindTo(R.id.markerCode);
			experienceProperties.get("icon").bindTo(R.id.experienceIcon);
			experienceProperties.load();
		}
		return view;
	}

	public AsyncTask.Status getStatus()
	{
		if (updater == null)
		{
			return AsyncTask.Status.FINISHED;
		}
		else
		{
			return updater.getStatus();
		}
	}

	public void update(String... uris)
	{
		if (getStatus() == AsyncTask.Status.FINISHED)
		{
			updater = new ExperienceListUpdater(context, experienceController);
			updater.execute(uris);
		}
	}
}
