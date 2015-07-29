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

package uk.ac.horizon.artcodes.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditThresholdBinding;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;
import uk.ac.horizon.artcodes.scanner.camera.FrameProcessor;
import uk.ac.horizon.artcodes.scanner.overlay.Overlay;
import uk.ac.horizon.artcodes.scanner.process.HueShifter;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.IntensityGreyscaler;
import uk.ac.horizon.artcodes.scanner.process.Inverter;
import uk.ac.horizon.artcodes.scanner.process.RGBGreyscaler;

import java.util.ArrayList;
import java.util.List;

public class ExperienceEditThresholdFragment extends ExperienceEditFragment
{
	private static List<List<Object>> getColorPresets()
	{
		List<List<Object>> presets = new ArrayList<>();
		List<Object> l;
		List<ImageProcessor> greyscalers = new ArrayList<>();
		// use Double here as serialization can go wrong if you let the compiler use implicit constructors.
		Double zero = 0.0, one = 1.0;

		l = new ArrayList<>(5);
		l.add("CMYK");
		l.add(one);
		l.add(zero);
		l.add(zero);
		l.add(zero);
		presets.add(l);
		l = new ArrayList<>(5);
		l.add("CMYK");
		l.add(zero);
		l.add(one);
		l.add(zero);
		l.add(zero);
		presets.add(l);
		l = new ArrayList<>(5);
		l.add("CMYK");
		l.add(zero);
		l.add(zero);
		l.add(one);
		l.add(zero);
		presets.add(l);
		l = new ArrayList<>(5);
		l.add("CMYK");
		l.add(zero);
		l.add(zero);
		l.add(zero);
		l.add(one);
		presets.add(l);

		l = new ArrayList<>(4);
		l.add("CMY");
		l.add(one);
		l.add(zero);
		l.add(zero);
		presets.add(l);
		l = new ArrayList<>(4);
		l.add("CMY");
		l.add(zero);
		l.add(one);
		l.add(zero);
		presets.add(l);
		l = new ArrayList<>(4);
		l.add("CMY");
		l.add(zero);
		l.add(zero);
		l.add(one);
		presets.add(l);

		return presets;
	}

	private class ThresholdingProcessor extends FrameProcessor
	{
		private final String TAG = "GreyImgThread";
		boolean running = true;
		ImageProcessor imageProcessor = null;
		private long timeOfLastAutoFocus = 0;
		private Bitmap result;
		private Mat drawImage;

		@Override
		public void process(Mat image)
		{
			timeOfLastAutoFocus = System.currentTimeMillis();
			result = null;

			while (running)
			{
				try
				{
					synchronized (lockObject)
					{
						if (hasChanged)
						{
							hasChanged = false;
							// TODO imageProcessor = experience.get().getThreshold();
						}
					}
					if (imageProcessor != null)
					{
						Mat greyImage = null;// TODO imageProcessor.greyscaleImage(image);
						rotate(greyImage, greyImage);
						Imgproc.cvtColor(greyImage, drawImage, Imgproc.COLOR_GRAY2BGRA);
						greyImage.release();

						if (result == null)
						{
							result = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
						}
						Utils.matToBitmap(drawImage, result);
						//updateImage(result);
					}
				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage(), e);
				}

//				// Test if camera needs to be focused
//				if (CameraController.deviceNeedsManualAutoFocus && System.currentTimeMillis() - timeOfLastAutoFocus >= 5000)
//				{
//					timeOfLastAutoFocus = System.currentTimeMillis();
//					camera.performManualAutoFocus(new Camera.AutoFocusCallback()
//					{
//						@Override
//						public void onAutoFocus(boolean b, Camera camera)
//						{
//						}
//					});
//				}
			}
		}
	}

	private final Object lockObject = new Object();
	private final List<ImageProcessor> thresholds = new ArrayList<>();
	private ExperienceEditThresholdBinding binding;
	// TODO Use binding variables
	private CameraAdapter camera;
	private Overlay overlay;

	private Inverter inverter = new Inverter();
	private HueShifter hueShifter = new HueShifter();

	private boolean hasChanged;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(getActivity(), R.layout.experience_edit_threshold);
		camera = new CameraAdapter(getActivity());
		binding.setCamera(camera);
		overlay = new Overlay();
		// TODO overlay.setThresholdLayer(new ThesholdLayer);
		binding.setOverlay(overlay);

		thresholds.add(new IntensityGreyscaler());
		thresholds.add(new RGBGreyscaler(RGBGreyscaler.Channel.red));
		thresholds.add(new RGBGreyscaler(RGBGreyscaler.Channel.green));
		thresholds.add(new RGBGreyscaler(RGBGreyscaler.Channel.blue));
		// TODO Add cmyk
		// TODO Add cmy?

		camera.setFrameProcessor(new FrameProcessor()
		{
			@Override
			public void process(Mat image)
			{

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
					//experience.setInvertGreyscale(b);
					hasChanged = true;
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
				//experience.setHueShift(i);
				hasChanged = true;
				//hueShiftLabel.setText(getString(R.string.greyscaleHueSeekBarLabel, i));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// make sure the preview thread got the last change:
				synchronized (lockObject)
				{
					hasChanged = true;
				}
			}
		});

		List<CharSequence> names = new ArrayList<>();
		//Collections.addAll(names, getResources().getStringArray(R.array.colourPresetNames));

		// presets (the order corresponds to the order of the "colourPresetNames" array in values.xml):
		final List<List<Object>> presets = getColorPresets();

		// find the index of the current preset
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
						hasChanged = true;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView)
			{
			}
		});

		// TODO getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		return binding.getRoot();
	}

}