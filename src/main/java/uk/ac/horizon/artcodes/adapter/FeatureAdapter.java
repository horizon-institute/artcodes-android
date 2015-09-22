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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.common.collect.Ordering;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.databinding.FeatureBinding;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder>
{
	public static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER =
			Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();
	private final SortedList<Feature> features;
	private final Context context;
	public FeatureAdapter(Context context)
	{
		super();
		this.context = context;
		features = new SortedList<>(Feature.class, new SortedListAdapterCallback<Feature>(this)
		{
			@Override
			public boolean areContentsTheSame(Feature oldItem, Feature newItem)
			{
				return oldItem.getId() == newItem.getId();
			}

			@Override
			public boolean areItemsTheSame(Feature item1, Feature item2)
			{
				return item1.getId() == item2.getId();
			}

			@Override
			public int compare(Feature o1, Feature o2)
			{
				return CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(o1.getName(), o2.getName());
			}
		});
	}

	public void add(Feature item)
	{
		features.add(item);
	}

	@Override
	public int getItemCount()
	{
		return features.size();
	}

	@Override
	public void onBindViewHolder(FeatureViewHolder holder, int position)
	{
		final Feature feature = features.get(position);
		holder.binding.setFeature(feature);
		holder.binding.featureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				feature.setEnabled(isChecked);
			}
		});
	}

	@Override
	public FeatureViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new FeatureViewHolder(FeatureBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
	}

	public class FeatureViewHolder extends RecyclerView.ViewHolder
	{
		private FeatureBinding binding;

		public FeatureViewHolder(FeatureBinding binding)
		{
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
