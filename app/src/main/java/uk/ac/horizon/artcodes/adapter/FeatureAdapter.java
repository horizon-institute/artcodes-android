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
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.google.common.collect.Ordering;

import uk.ac.horizon.artcodes.databinding.FeatureItemBinding;

public class FeatureAdapter extends ListAdapter<FeatureItemBinding> {
	private static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER =
			Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();
	private final SortedList<FeatureItem> features;

	public FeatureAdapter(Context context) {
		super(context);
		features = new SortedList<>(FeatureItem.class, new SortedListAdapterCallback<FeatureItem>(adapter) {
			@Override
			public boolean areContentsTheSame(FeatureItem oldItem, FeatureItem newItem) {
				return oldItem.getName().equals(newItem.getName());
			}

			@Override
			public boolean areItemsTheSame(FeatureItem item1, FeatureItem item2) {
				return item1.getName().equals(item2.getName());
			}

			@Override
			public int compare(FeatureItem o1, FeatureItem o2) {
				return CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(o1.getName(), o2.getName());
			}
		});
	}

	public void add(FeatureItem item) {
		features.add(item);
	}

	@Override
	public FeatureItemBinding createBinding(final ViewGroup parent, final int viewType) {
		return FeatureItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
	}

	@Override
	public void bind(final int position, final FeatureItemBinding binding) {
		final FeatureItem feature = features.get(position);
		binding.setFeature(feature);
		binding.featureSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> feature.setEnabled(isChecked));
	}

	@Override
	public int getViewCount() {
		return features.size();
	}
}
