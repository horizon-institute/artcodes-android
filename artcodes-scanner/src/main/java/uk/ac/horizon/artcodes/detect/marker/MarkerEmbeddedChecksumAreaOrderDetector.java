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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;

/**
 * <p>This class detects Artcodes with an embedded checksum region and orders the code by region
 * surface area.</p>
 *
 * <p>The code is first detected, sorted by value, validated, and then sorted by area.</p>
 *
 * <p><b>E.g. Codes with different area orders have the same embedded checksum.</b></p>
 */
public class MarkerEmbeddedChecksumAreaOrderDetector extends MarkerEmbeddedChecksumDetector
{
	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "detectEmbeddedOrdered";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, Map<String, String> args)
		{
			return new MarkerEmbeddedChecksumAreaOrderDetector(experience, handler, args!=null&&args.containsKey("embeddedOnly"), args!=null&&args.containsKey("relaxed"));
		}
	}

	public MarkerEmbeddedChecksumAreaOrderDetector(Experience experience, MarkerDetectionHandler handler, boolean embeddedChecksumRequired, boolean relaxed)
	{
		super(experience, handler, embeddedChecksumRequired, relaxed);
	}

	protected Marker createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy)
	{
		List<MarkerRegion> regions = null;
		MarkerRegion checksumRegion = null;
		for (int currentNodeIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE]; currentNodeIndex >= 0; currentNodeIndex = (int) hierarchy.get(0, currentNodeIndex)[NEXT_NODE])
		{
			final MarkerRegion region = createRegionForNode(currentNodeIndex, contours, hierarchy);
			if (region != null)
			{
				if (this.ignoreEmptyRegions && region.value==0)
				{
					continue;
				}
				else if (regions == null)
				{
					regions = new ArrayList<>();
				}
				else if (regions.size() >= maxRegions)
				{
					return null;
				}

				regions.add(region);
			}
			else if (checksumRegion == null)
			{
				checksumRegion = getChecksumRegionAtNode(currentNodeIndex, hierarchy);
				if (checksumRegion == null)
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}

		if (regions!=null && checksumRegion!=null)
		{
			Marker marker = new MarkerWithEmbeddedChecksum(nodeIndex, regions, checksumRegion);
			sortByValue(marker);
			if (isValidRegionList(marker))
			{
				addAreaToRegions(marker, contours);
				sortByArea(marker);
				return marker;
			}
		}

		return null;
	}

	@Override
	protected void sortCode(Marker marker)
	{
		sortByArea(marker);
	}

	private void addAreaToRegions(Marker marker, List<MatOfPoint> contours)
	{
		for (MarkerRegion region : marker.regions)
		{
			region.data = new Double(Imgproc.contourArea(contours.get(region.index)));
		}
	}

	private void sortByArea(Marker marker)
	{
		Collections.sort(marker.regions, new Comparator<MarkerRegion>()
		{
			@Override
			public int compare(MarkerRegion region1, MarkerRegion region2)
			{
				return ((Double)region1.data).doubleValue() < ((Double)region2.data).doubleValue() ? -1 : (region1.data.equals(region2.data) ? 0 : 1);
			}
		});
	}
	private void sortByValue(Marker marker)
	{
		Collections.sort(marker.regions, new Comparator<MarkerRegion>()
		{
			@Override
			public int compare(MarkerRegion region1, MarkerRegion region2)
			{
				return region1.value < region2.value ? -1 : (region1.value==region2.value ? 0 : 1);
			}
		});
	}


}
