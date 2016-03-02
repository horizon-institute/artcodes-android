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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.adapter.ExperienceSortedListAdapter;
import uk.ac.horizon.artcodes.databinding.ListBinding;

public class ExperienceStarFragment extends ArtcodeFragmentBase
{
	private ExperienceAdapter adapter;

	@NonNull
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListBinding binding = ListBinding.inflate(inflater, container, false);
		adapter = new ExperienceSortedListAdapter(getActivity(), getServer());
		binding.setAdapter(adapter);
		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		GoogleAnalytics.trackScreen("View Starred");
		getActivity().setTitle(R.string.nav_starred);
		adapter.loadStarted();
		getServer().loadStarred(adapter);
	}
}
