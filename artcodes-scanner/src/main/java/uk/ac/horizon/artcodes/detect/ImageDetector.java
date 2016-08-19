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

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetector;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.TileThresholder;

public class ImageDetector
{
	static
	{
		// Init OpenCV
		if (!OpenCVLoader.initDebug())
		{
			Log.e("OpenCV", "Error Initializing OpenCV");
		}
	}

	public static void detectMarkers(Bitmap image, Experience experience, MarkerDetectionHandler handler)
	{
		final ImageBuffers imageBuffers = new ImageBuffers();
		imageBuffers.createBuffer(image.getWidth(), image.getHeight(), image.getByteCount() * 8);
		imageBuffers.setImage(image);
		final TileThresholder tileThresholder = new TileThresholder();
		tileThresholder.process(imageBuffers);
		final MarkerDetector markerDetector = new MarkerDetector(experience, handler);
		markerDetector.process(imageBuffers);
	}
}
