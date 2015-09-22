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

package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditInfoBinding;
import uk.ac.horizon.artcodes.ui.ExperienceEditor;

public class ExperienceEditInfoFragment extends ExperienceEditFragment
{
	private ExperienceEditInfoBinding binding;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceEditInfoBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_info;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		binding.setExperience(getExperience());
		binding.setExperienceEditor(new ExperienceEditor(getActivity(), getExperience()));
	}
}
