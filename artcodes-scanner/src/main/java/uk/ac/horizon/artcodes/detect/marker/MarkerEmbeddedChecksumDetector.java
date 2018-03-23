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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;

/**
 * <p>This class detects Artcodes with an embedded checksum region.</p>
 */
public class MarkerEmbeddedChecksumDetector extends MarkerDetector
{
	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "detectEmbedded";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
		{
			return new MarkerEmbeddedChecksumDetector(experience, handler, args!=null&&args.containsKey("embeddedOnly"), args!=null&&args.containsKey("relaxed"));
		}
	}

	private boolean embeddedChecksumRequired;
	private boolean relaxedEmbeddedChecksumIgnoreNonHollowDots;
	private boolean relaxedEmbeddedChecksumIgnoreMultipleHollowSegments;

	public MarkerEmbeddedChecksumDetector(Experience experience, MarkerDetectionHandler handler, boolean embeddedChecksumRequired, boolean relaxed)
	{
		super(experience, handler);
		this.embeddedChecksumRequired = embeddedChecksumRequired;
		this.relaxedEmbeddedChecksumIgnoreNonHollowDots = this.relaxedEmbeddedChecksumIgnoreMultipleHollowSegments = relaxed;
	}

	@Override
	protected Marker createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, ContourStatus[] status, int statusIndex)
	{
		return createMarkerForNode(nodeIndex, contours, hierarchy);
	}

	@Override
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

		if (regions!=null)
		{
			MarkerWithEmbeddedChecksum marker = new MarkerWithEmbeddedChecksum(nodeIndex, regions, checksumRegion);
			sortCode(marker);
			if (isValidRegionList(marker))
			{
				return marker;
			}
		}

		return null;
	}

	protected MarkerRegion getChecksumRegionAtNode(int regionIndex, Mat hierarchy)
	{
		// Find the first dot index:
		double[] nodes = hierarchy.get(0, regionIndex);
		int currentDotIndex = (int) nodes[FIRST_NODE];
		if (currentDotIndex < 0)
		{
			return null; // There are no dots in this region.
		}

		// Count all the dots and check if they are leaf nodes in the hierarchy:
		int dotCount = 0;
		while (currentDotIndex >= 0)
		{
			if (isValidHollowDot(currentDotIndex, hierarchy))
			{
				dotCount++;
			}
			else if (!(this.relaxedEmbeddedChecksumIgnoreNonHollowDots && this.isValidDot(currentDotIndex, hierarchy)))
			{
				return null; // Dot is not a leaf in the hierarchy.
			}

			// Get next dot node:
			nodes = hierarchy.get(0, currentDotIndex);
			currentDotIndex = (int) nodes[NEXT_NODE];
		}

		return new MarkerRegion(regionIndex, dotCount);
	}

	private boolean isValidHollowDot(int nodeIndex, Mat hierarchy)
	{
		double[] nodes = hierarchy.get(0, nodeIndex);
		return nodes[FIRST_NODE] >= 0 && // has a child node, and
				(hierarchy.get(0, (int) nodes[FIRST_NODE])[NEXT_NODE] < 0 || this.relaxedEmbeddedChecksumIgnoreMultipleHollowSegments) && //the child has no siblings, and
				isValidDot((int) nodes[FIRST_NODE], hierarchy);// the child is a leaf
	}


	@Override
	protected boolean hasValidChecksum(Marker marker, ContourStatus[] status, int statusIndex)
	{
		return hasValidChecksum(marker);
	}

	@Override
	protected boolean hasValidChecksum(Marker marker)
	{
		if (marker instanceof MarkerWithEmbeddedChecksum)
		{
			// If the detected Artcodes has an embedded checksum validate it
			MarkerWithEmbeddedChecksum markerEc = (MarkerWithEmbeddedChecksum) marker;
			if (markerEc.checksumRegion != null)
			{
				// Find weighted sum of code, e.g. 1:1:2:4:4 -> 1*1 + 1*2 + 2*3 + 4*4 + 4*5 = 45
				// Although do not use weights/values divisible by 7
				// e.g. transform values 1,2,3,4,5,6,7,8, 9,10,11,12,13,14,15... to
				//                       1,2,3,4,5,6,8,9,10,11,12,13,15,16,17
				int embeddedChecksumModValue = 7;
				int weightedSum = 0;
				int weight = 1;
				for (int i = 0; i < markerEc.regions.size(); ++i)
				{
					int value = markerEc.regions.get(i).value;
					value += (value+value/embeddedChecksumModValue)/embeddedChecksumModValue;
					if (weight%embeddedChecksumModValue==0)
					{
						++weight;
					}
					weightedSum += value * weight++;
				}
				return markerEc.checksumRegion.value == (weightedSum - 1) % 7 + 1;
			}
		}

		if (!this.embeddedChecksumRequired)
		{
			// If the detected Artcode does not have an embedded checksum and embedded checksum is
			// not required, delegate to super class
			return super.hasValidChecksum(marker);
		}
		else
		{
			// If the detected Artcode does not have an embedded checksum and embedded checksum is
			// required this is not a valid Artcode.
			return false;
		}
	}
}
