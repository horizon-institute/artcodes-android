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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import uk.ac.horizon.storicodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExperienceListAdapter extends BaseAdapter implements ExperienceListController.Listener
{
	private static final String DEFAULT_ID = "4c758e29-0759-4583-a0d4-71ee692b7f86";

	private final Context context;
	private final LayoutInflater inflater;
	private final ExperienceListController experienceController;
	private List<Experience> experiences = new ArrayList<>();
	private final List<ExperienceListController.Listener> listeners = new ArrayList<>();
	private static ExperienceListUpdater updater = null;

	public ExperienceListAdapter(final Context context, final ExperienceListController experienceController)
	{
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.experienceController = experienceController;
		this.experienceController.addListener(this);
		experienceListChanged();
	}

	public void addListener(ExperienceListController.Listener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void experienceListChanged()
	{
		List<Experience> newExperiences = new ArrayList<Experience>();
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
				String name1 = experience==null ? null : experience.getName();
				String name2 = experience2==null ? null : experience2.getName();

				// sort so null appears last in the list
				if (name1==null && name2==null)
				{
					return 0;
				}
				else if (name1==null && name2!=null)
				{
					return 1;
				}
				else if (name1!=null && name2==null)
				{
					return -1;
				}
				else
				{
					return name1.compareToIgnoreCase(name2);
				}
			}
		});

		experiences = newExperiences;
		notifyDataSetChanged();

		for (ExperienceListController.Listener listener : listeners)
		{
			listener.experienceListChanged();
		}
	}

	@Override
	public int getCount()
	{
		return experiences.size();
	}

	public List<Experience> getExperiences()
	{
		return experiences;
	}

	@Override
	public Object getItem(int i)
	{
		return experiences.get(i);
	}

	@Override
	public long getItemId(final int position)
	{
		return experiences.get(position).getId().hashCode();
	}

	/**
	 * Get an experience by its ID string.
	 * @param preferred The desired experiences ID string.
	 * @return The requested experience, or the default experience if not found.
	 */
	public Experience getSelected(String preferred)
	{
		Log.i("", "Looking for " + preferred);
		String id = preferred;
		if(id == null)
		{
			return null;
		}

		for (Experience experience : experiences)
		{
			if (experience.getId().equals(id))
			{
				Log.i("", "Found " + experience.getId());
				return experience;
			}
		}

		return null;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		final Experience experience = experiences.get(i);
		if (view == null)
		{
			view = inflater.inflate(R.layout.experience_listitem, viewGroup, false);
		}

		final TextView eventTitle = (TextView) view.findViewById(R.id.markerCode);
		final ImageView iconView = (ImageView) view.findViewById(R.id.experienceIcon);
		final LinearLayout layout = (LinearLayout) view.findViewById(R.id.rootView);

		eventTitle.setText(experience.getName());

		iconView.setSelected(false);
		iconView.setImageDrawable(null);
		if (experience.getIcon() != null && !experience.getIcon().isEmpty())
		{
			Picasso.with(context).cancelRequest(iconView);
			Picasso.with(context).load(experience.getIcon()).into(iconView);
		}

		if (viewGroup.getClass().getName().endsWith("SpinnerCompat"))
		{
			layout.setPadding(0, 0, 48, 0);
		}

		return view;
	}

	public void removeListener(ExperienceListController.Listener listener)
	{
		listeners.remove(listener);
	}

	public AsyncTask.Status getStatus()
	{
		if(updater == null)
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
		if(getStatus() == AsyncTask.Status.FINISHED)
		{
			updater = new ExperienceListUpdater(context, experienceController);
			updater.execute(uris);
		}
	}

}
