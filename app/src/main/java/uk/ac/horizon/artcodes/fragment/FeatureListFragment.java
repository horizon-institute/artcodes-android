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
package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import uk.ac.horizon.artcodes.Features;
import uk.ac.horizon.artcodes.ScannerFeatures;
import uk.ac.horizon.artcodes.adapter.FeatureAdapter;
import uk.ac.horizon.artcodes.adapter.FeatureItem;
import uk.ac.horizon.artcodes.databinding.ListBinding;

public class FeatureListFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ListBinding binding = ListBinding.inflate(inflater, container, false);
		final FeatureAdapter adapter = new FeatureAdapter(getActivity());
		binding.setAdapter(adapter);

		try {
			for(Features feature: Features.values()) {
				adapter.add(new FeatureItem(feature, getContext()));
			}
			for(ScannerFeatures feature: ScannerFeatures.values()) {
				adapter.add(new FeatureItem(feature, getContext()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return binding.getRoot();
	}
}
