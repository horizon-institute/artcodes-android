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

package uk.ac.horizon.artcodes.detect.marker;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;

public class MarkerAreaOrderDetector extends MarkerDetector
{
	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "detectOrdered";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
		{
			return new MarkerAreaOrderDetector(experience, handler);
		}
	}

	public MarkerAreaOrderDetector(Experience experience, MarkerDetectionHandler handler)
	{
		super(experience, handler);
	}

	@Override
	protected MarkerRegion createRegionForNode(int regionIndex, List<MatOfPoint> contours, Mat hierarchy, ContourStatus[] status, int statusIndex)
	{
		MarkerRegion region = super.createRegionForNode(regionIndex, contours, hierarchy, status, statusIndex);
		if (region != null)
		{
			region.data = Imgproc.contourArea(contours.get(region.index));
		}
		return region;
	}
	@Override
	protected MarkerRegion createRegionForNode(int regionIndex, List<MatOfPoint> contours, Mat hierarchy)
	{
		MarkerRegion region = super.createRegionForNode(regionIndex, contours, hierarchy);
		if (region != null)
		{
			region.data = Imgproc.contourArea(contours.get(region.index));
		}
		return region;
	}

	@Override
	protected void sortCode(Marker marker)
	{
		Collections.sort(marker.regions, new Comparator<MarkerRegion>()
		{
			@Override
			public int compare(MarkerRegion region1, MarkerRegion region2)
			{
				return Double.compare((Double) region1.data, (Double) region2.data);
			}
		});
	}
}
