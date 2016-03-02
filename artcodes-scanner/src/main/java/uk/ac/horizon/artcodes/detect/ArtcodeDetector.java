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

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.TileThresholder;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetector;

import uk.ac.horizon.artcodes.scanner.BR;

public class ArtcodeDetector extends Detector
{
	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e("OpenCV", "Error Initializing OpenCV");
		}
	}

	private int margin = 100;

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

	public ArtcodeDetector(Experience experience, MarkerDetectionHandler handler)
	{
		// TODO Construct pipeline
		//for (String processor : experience.getPipeline())
		//{
		//}

		if (pipeline.isEmpty())
		{
			pipeline.add(new TileThresholder());
			pipeline.add(new MarkerDetector(experience, handler));
		}
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