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

package uk.ac.horizon.artcodes.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Ordering;

import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.activity.ExperienceActivity;
import uk.ac.horizon.artcodes.databinding.ExperienceItemBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder> implements LoadCallback<Experience>
{
	public class ExperienceViewHolder extends RecyclerView.ViewHolder
	{
		private ExperienceItemBinding binding;

		public ExperienceViewHolder(ExperienceItemBinding binding)
		{
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	private static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER =
			Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();
	private final SortedList<Experience> experiences;
	private final Context context;

	public ExperienceAdapter(Context context)
	{
		super();
		this.context = context;
		experiences = new SortedList<>(Experience.class, new SortedListAdapterCallback<Experience>(this)
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
				int result = CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(o1.getName(), o2.getName());
				if (result != 0)
				{
					return result;
				}
				return CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(o1.getId(), o2.getId());
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return experiences.size();
	}

	@Override
	public void onBindViewHolder(ExperienceViewHolder holder, int position)
	{
		final Experience experience = experiences.get(position);
		holder.binding.setExperience(experience);
		holder.binding.getRoot().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ExperienceActivity.start(context, experience);
			}
		});
		holder.binding.scanButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ArtcodeActivity.start(context, experience);
			}
		});
	}

	@Override
	public ExperienceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ExperienceViewHolder(ExperienceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void loaded(Experience item)
	{
		experiences.add(item);
	}
}
