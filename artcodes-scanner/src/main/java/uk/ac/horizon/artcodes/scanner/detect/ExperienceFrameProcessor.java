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

package uk.ac.horizon.artcodes.scanner.detect;

import android.util.Log;

import org.opencv.core.Rect;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.FrameProcessor;
import uk.ac.horizon.artcodes.scanner.ImageBuffers;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;
import uk.ac.horizon.artcodes.scanner.process.marker.MarkerDetector;

public class ExperienceFrameProcessor extends FrameProcessor
{
	public ExperienceFrameProcessor(ImageBuffers buffers, Experience experience, MarkerDetectionHandler handler)
	{
		super(buffers);
		for (String processor : experience.getPipeline())
		{
			// TODO Construct pipeline
		}

		if (pipeline.isEmpty())
		{
			pipeline.add(new TileThresholder());
			pipeline.add(new MarkerDetector(experience, handler));
		}
	}

	@Override
	protected Rect createROI(int imageWidth, int imageHeight, int surfaceWidth, int surfaceHeight)
	{
		final int size = Math.min(imageWidth, imageHeight);

		final int colStart = (imageWidth - size) / 2;
		final int rowStart = (imageHeight - size) / 2;

		final float surfaceMax = Math.max(surfaceHeight, surfaceWidth);

		float sizeRatio = surfaceMax / Math.max(imageWidth, imageHeight);
		Log.i("FrameProcessor", "Size ratio = " + sizeRatio);

		int viewfinderSize = (int) (Math.max(colStart, rowStart) * sizeRatio);
		Log.i("FrameProcessor", "Size = " + size + ", Viewfinder size = " + viewfinderSize);

		buffers.setBorderTop(viewfinderSize);
		buffers.setBorderBottom(viewfinderSize);
		return new Rect(colStart, rowStart, size, size);
	}
}