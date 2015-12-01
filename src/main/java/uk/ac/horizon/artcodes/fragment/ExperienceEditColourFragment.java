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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditColourBinding;
import uk.ac.horizon.artcodes.scanner.FrameProcessor;
import uk.ac.horizon.artcodes.scanner.ImageBuffers;
import uk.ac.horizon.artcodes.scanner.Scanner;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.RGBGreyscaler;

public class ExperienceEditColourFragment extends ExperienceEditFragment
{
	private final Object lockObject = new Object();
	private final List<ImageProcessor> presets = new ArrayList<>();
	private final ImageBuffers buffers = new ImageBuffers();
	private ExperienceEditColourBinding binding;
	private Scanner scanner;
	private ImageProcessor filter;
	private FrameProcessor frameProcessor;

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_colour;
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceEditColourBinding.inflate(inflater, container, false);
		scanner = new Scanner(getActivity());

		binding.cameraSurface.getHolder().addCallback(scanner);
		binding.setBuffers(buffers);

		//presets.add(new IntensityGreyscaler());
		presets.add(new RGBGreyscaler(RGBGreyscaler.Channel.red));
		presets.add(new RGBGreyscaler(RGBGreyscaler.Channel.green));
		presets.add(new RGBGreyscaler(RGBGreyscaler.Channel.blue));
		// TODO Add cmy/cmyk/custom?

		frameProcessor = new FrameProcessor(buffers);
		scanner.setFrameProcessor(frameProcessor);

		binding.colourPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
			{
				//String[] names = getResources().getStringArray(R.array.colourPresetNames);
				if (i < presets.size())
				{
					synchronized (lockObject)
					{
						//experience.setGreyscaleOptions(presets.get(i));
					}
				}
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