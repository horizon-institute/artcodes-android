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
import android.widget.CompoundButton;
import android.widget.SeekBar;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditColourBinding;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;
import uk.ac.horizon.artcodes.scanner.camera.FrameProcessor;
import uk.ac.horizon.artcodes.scanner.overlay.Overlay;
import uk.ac.horizon.artcodes.scanner.overlay.ThresholdLayer;
import uk.ac.horizon.artcodes.scanner.process.HueShifter;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.IntensityGreyscaler;
import uk.ac.horizon.artcodes.scanner.process.Inverter;
import uk.ac.horizon.artcodes.scanner.process.RGBGreyscaler;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

public class ExperienceEditColourFragment extends ExperienceEditFragment
{
	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e("", "Error Initializing OpenCV");
		}
	}

	private final Object lockObject = new Object();
	private final List<ImageProcessor> presets = new ArrayList<>();
	private final HueShifter hueShifter = new HueShifter();
	private ExperienceEditColourBinding binding;
	private CameraAdapter camera;
	private Overlay overlay;
	private Inverter inverter;
	private ImageProcessor preset;

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_colour;
	}

	private void updatePipeline()
	{
		getExperience().getProcessors().clear();
		if (hueShifter.getHueShift() != 0)
		{
			getExperience().getProcessors().add(hueShifter);
		}
		if (preset != null)
		{
			getExperience().getProcessors().add(preset);
		}
		if (inverter != null)
		{
			getExperience().getProcessors().add(inverter);
		}
		getExperience().getProcessors().add(new TileThresholder());
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceEditColourBinding.inflate(inflater, container, false);
		camera = new CameraAdapter(getActivity());
		binding.setCamera(camera);
		overlay = new Overlay();
		overlay.setThresholdLayer(new ThresholdLayer());
		binding.setOverlay(overlay);

		presets.add(new IntensityGreyscaler());
		presets.add(new RGBGreyscaler(RGBGreyscaler.Channel.red));
		presets.add(new RGBGreyscaler(RGBGreyscaler.Channel.green));
		presets.add(new RGBGreyscaler(RGBGreyscaler.Channel.blue));
		// TODO Add cmyk
		// TODO Add cmy?

		camera.setFrameProcessor(new FrameProcessor()
		{
			@Override
			public void process(Mat image)
			{
				try
				{
					if (hueShifter.getHueShift() != 0)
					{
						image = hueShifter.process(image, false);
					}
					if (preset != null)
					{
						image = preset.process(image, false);
					}
					if (inverter != null)
					{
						image = inverter.process(image, false);
					}

					if (overlay.hasOutput(image))
					{
						rotate(image, image);
					}

					overlay.drawThreshold(image);
				} catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}
			}
		});

		//invertSwitch.setChecked(experience.getInvertGreyscale());
		binding.invertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				synchronized (lockObject)
				{
					if (b)
					{
						inverter = new Inverter();
					} else
					{
						inverter = null;
					}
					updatePipeline();
				}
			}
		});

		//hueShiftLabel.setText(getString(R.string.greyscaleHueSeekBarLabel, (int) experience.getHueShift()));
		//hueSlider.setProgress((int) experience.getHueShift());
		binding.hueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b)
			{
				hueShifter.setHueShift(i);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				updatePipeline();
			}
		});

		int index = 0;
//		if (experience.getGreyscaleOptions() != null)
//		{
//			index = presets.indexOf(experience.getGreyscaleOptions());
//			if (index < 0) // if the current preset is not in the list add an "other" option
//			{
//				names.add("Other");
//				presets.add(this.experience.getGreyscaleOptions());
//				colourPresetSpinner.setSelection(presets.size() - 1);
//			}
//		}

		// setup the adapter for the spinner
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.colourPresetNames, android.R.layout.simple_spinner_item);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//colourPresetSpinner.setAdapter(adapter);
		//colourPresetSpinner.setSelection(index);

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
}