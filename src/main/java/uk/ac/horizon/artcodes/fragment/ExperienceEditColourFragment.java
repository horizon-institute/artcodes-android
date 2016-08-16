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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditColourBinding;
import uk.ac.horizon.artcodes.detect.Detector;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.RgbColourFilter;

public class ExperienceEditColourFragment extends ExperienceEditFragment
{
	@Override
	public int getTitleResource()
	{
		return R.string.fragment_colour;
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final ExperienceEditColourBinding binding = ExperienceEditColourBinding.inflate(inflater, container, false);

		final List<ImageProcessor> presets = new ArrayList<>();
		// TODO presets.add(new IntensityGreyscaler());
		presets.add(new RgbColourFilter.RgbColourFilter_MixChannelsImpl(RgbColourFilter.Channel.red));
		presets.add(new RgbColourFilter.RgbColourFilter_MixChannelsImpl(RgbColourFilter.Channel.green));
		presets.add(new RgbColourFilter.RgbColourFilter_MixChannelsImpl(RgbColourFilter.Channel.blue));

		final Detector detector = new Detector();
		binding.setDetector(detector);

		binding.colourPresetSpinner.setAdapter(new BaseAdapter()
		{
			@Override
			public int getCount()
			{
				return presets.size();
			}

			@Override
			public Object getItem(final int position)
			{
				return presets.get(position);
			}

			@Override
			public long getItemId(final int position)
			{
				return 0;
			}

			@Override
			public View getView(final int position, final View convertView, final ViewGroup parent)
			{
				View view = convertView;
				if(!(view instanceof TextView))
				{
					view = new TextView(parent.getContext());
				}

				ImageProcessor processor = presets.get(position);
				if(processor instanceof RgbColourFilter.RgbColourFilter_MixChannelsImpl)
				{
					((TextView)view).setText(((RgbColourFilter.RgbColourFilter_MixChannelsImpl)processor).getChannel().toString());
				}


				return view;
			}
		});
		binding.colourPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
			{
				// TODO
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView)
			{
			}
		});

		return binding.getRoot();
	}

	private void savePipeline()
	{
		// TODO
		getExperience().getPipeline().clear();

		getExperience().getPipeline().add("tile");
		getExperience().getPipeline().add("detect");
	}
}