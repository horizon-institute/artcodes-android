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

package uk.ac.horizon.artcodes.detect;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;

import org.opencv.core.Rect;

import java.util.HashMap;
import java.util.Map;

import uk.ac.horizon.artcodes.detect.marker.MarkerAreaOrderDetector;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetector;
import uk.ac.horizon.artcodes.detect.marker.MarkerEmbeddedChecksumDetector;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;
import uk.ac.horizon.artcodes.process.RedFilter;
import uk.ac.horizon.artcodes.process.TileThresholder;
import uk.ac.horizon.artcodes.scanner.BR;

public class ArtcodeDetector extends Detector
{
	private static final Map<String, ImageProcessorFactory> factoryRegistry = new HashMap<>();

	static
	{
		register(new MarkerDetector.Factory());
		register(new MarkerEmbeddedChecksumDetector.Factory());
		register(new MarkerAreaOrderDetector.Factory());
		register(new TileThresholder.Factory());
		register(new RedFilter.Factory());
		//register(new RGBFilter.BlueFactory());
		//register(new RGBFilter.GreenFactory());
	}

	private int margin = 100;

	public ArtcodeDetector(Experience experience, MarkerDetectionHandler handler)
	{
		for (String processorName : experience.getPipeline())
		{
			ImageProcessor processor = getProcessor(processorName, experience, handler);
			if (processor != null)
			{
				pipeline.add(processor);
			}
		}

		if (pipeline.isEmpty())
		{
			pipeline.add(new TileThresholder());
			pipeline.add(new MarkerDetector(experience, handler));
		}
	}

	@BindingAdapter("height")
	public static void bindHeight(View view, Integer height)
	{
		if (height != null)
		{
			view.getLayoutParams().height = height;
			view.getParent().requestLayout();
		}
	}

	@BindingAdapter("width")
	public static void bindWidth(View view, Integer width)
	{
		if (width != null)
		{
			view.getLayoutParams().width = width;
			view.getParent().requestLayout();
		}
	}

	private static void register(ImageProcessorFactory factory)
	{
		factoryRegistry.put(factory.getName(), factory);
	}

	private static ImageProcessor getProcessor(String string, Experience experience, MarkerDetectionHandler handler)
	{
		ImageProcessorFactory factory = factoryRegistry.get(string);
		if (factory != null)
		{
			try
			{
				return factory.create(experience, handler);
			}
			catch (Exception e)
			{
				Log.w("detector", e.getMessage(), e);
			}
		}

		return null;
	}

	@Bindable
	public int getMargin()
	{
		return margin;
	}

	@Override
	protected Rect createROI(int imageWidth, int imageHeight, int surfaceWidth, int surfaceHeight)
	{
		final int size = Math.min(imageWidth, imageHeight);

		final int colStart = (imageWidth - size) / 2;
		final int rowStart = (imageHeight - size) / 2;

		final float surfaceMax = Math.max(surfaceHeight, surfaceWidth);

		float sizeRatio = surfaceMax / Math.max(imageWidth, imageHeight);
		Log.i("Detector", "Size ratio = " + sizeRatio);

		margin = (int) (Math.max(colStart, rowStart) * sizeRatio);
		notifyPropertyChanged(BR.margin);
		Log.i("Detector", "Size = " + size + ", margin = " + margin);

		return new Rect(colStart, rowStart, size, size);
	}
}