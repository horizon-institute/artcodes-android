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


import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.detect.MarkerDetectionHandler;

public class MarkerEmbeddedChecksumDetector extends MarkerDetector
{
	public MarkerEmbeddedChecksumDetector(Experience experience, MarkerDetectionHandler handler)
	{
		super(experience, handler);
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
				if (regions == null)
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
		}

		if (isValidRegionList(regions, checksumRegion))
		{
			return new Marker(nodeIndex, regions, checksumRegion);
		}

		return null;
	}

	private MarkerRegion getChecksumRegionAtNode(int regionIndex, Mat hierarchy)
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
				// Get next dot node:
				nodes = hierarchy.get(0, currentDotIndex);
				currentDotIndex = (int) nodes[NEXT_NODE];
			}
			else
			{
				return null; // Dot is not a leaf in the hierarchy.
			}
		}

		return new MarkerRegion(regionIndex, dotCount);
	}

	private boolean isValidHollowDot(int nodeIndex, Mat hierarchy)
	{
		double[] nodes = hierarchy.get(0, nodeIndex);
		return nodes[FIRST_NODE] >= 0 && // has a child node, and
				hierarchy.get(0, (int) nodes[FIRST_NODE])[NEXT_NODE] < 0 && //the child has no siblings, and
				isValidDot((int) nodes[FIRST_NODE], hierarchy);// the child is a leaf
	}

	private boolean isValidRegionList(List<MarkerRegion> regions, MarkerRegion checksumRegion)
	{
		if (checksumRegion == null)
		{
			return isValidRegionList(regions);
		}

		// Find weighted sum of code, e.g. 1:1:2:4:4 -> 1*1 + 1*2 + 2*3 + 4*4 + 4*5 = 45
		int weightedSum = 0;
		for (int i = 0; i < regions.size(); ++i)
		{
			weightedSum += regions.get(i).value * (i + 1);
		}
		return checksumRegion.value == (weightedSum % 7 == 0 ? 7 : weightedSum % 7);
	}
}