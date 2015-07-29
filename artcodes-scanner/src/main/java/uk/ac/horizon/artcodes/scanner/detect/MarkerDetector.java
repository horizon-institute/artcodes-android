/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.scanner.detect;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerDetector
{
	protected static final int REGION_INVALID = -1;
	protected static final int REGION_EMPTY = 0;
	//indexes of leaf nodes in contour tree hierarchy.
	protected static final int NEXT_NODE = 0;
	protected static final int FIRST_NODE = 2;

	/**
	 * This function determines whether the input node is a valid region. It is a valid region if it contains zero or more dots.
	 *
	 * @param regionIndex    Region index in the hierarchy.
	 * @param hierarchy      This contains the contours or components hierarchy.
	 * @param regionMaxValue The maximum number of dots allowed in a region.
	 * @return Returns region status.
	 */
	private static int getRegionValue(int regionIndex, Mat hierarchy, int regionMaxValue)
	{
		// Find the first dot index:
		double[] nodes = hierarchy.get(0, regionIndex);
		int currentDotIndex = (int) nodes[FIRST_NODE];
		if (currentDotIndex < 0)
		{
			return REGION_EMPTY; // There are no dots in this region.
		}

		// Count all the dots and check if they are leaf nodes in the hierarchy:
		int dotCount = 0;
		while (currentDotIndex >= 0)
		{
			if (verifyAsLeaf(currentDotIndex, hierarchy))
			{
				dotCount++;
				// Get next dot node:
				nodes = hierarchy.get(0, currentDotIndex);
				currentDotIndex = (int) nodes[NEXT_NODE];

				if (dotCount > regionMaxValue)
				{
					return REGION_INVALID; // Too many dots.
				}
			}
			else
			{
				return REGION_INVALID; // Dot is not a leaf in the hierarchy.
			}
		}

		return dotCount;
	}

	/**
	 * This functions determines if the node is a valid leaf. It is a valid leaf if it does not have any child nodes.
	 */
	private static Boolean verifyAsLeaf(int nodeIndex, Mat hierarchy)
	{
		double[] nodes = hierarchy.get(0, nodeIndex);
		return nodes[FIRST_NODE] < 0;
	}

	private static Integer getEmbeddedChecksumValueForRegion(int regionIndex, Mat hierarchy)
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
			if (verifyAsDoubleLeaf(currentDotIndex, hierarchy))
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

		return dotCount;
	}

	private static Boolean verifyAsDoubleLeaf(int nodeIndex, Mat hierarchy)
	{
		double[] nodes = hierarchy.get(0, nodeIndex);
		return nodes[FIRST_NODE] >= 0 && // has a child node, and
				hierarchy.get(0, (int) nodes[FIRST_NODE])[NEXT_NODE] < 0 && //the child has no siblings, and
				verifyAsLeaf((int) nodes[FIRST_NODE], hierarchy);// the child is a leaf
	}

	public Marker createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience)
	{
		Marker.MarkerDetails markerDetails = createMarkerDetailsForNode(nodeIndex, contours, hierarchy, experience);
		if (markerDetails != null)
		{
			return new Marker(this.getCodeFor(markerDetails), markerDetails);
		}
		else
		{
			return null;
		}
	}

	public List<Marker> findMarkers(Mat image, Overlay overlay, Experience experience)
	{
		final ArrayList<MatOfPoint> contours = new ArrayList<>();
		final Mat hierarchy = new Mat();
		try
		{
			overlay.drawThreshold(image);

			// holds all the markers identified in the camera.
			List<Marker> foundMarkers = new ArrayList<>();
			// Find blobs using connect component.
			Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
			for (int i = 0; i < contours.size(); i++)
			{
				final Marker code = createMarkerForNode(i, contours, hierarchy, experience);
				if (code != null)
				{
					foundMarkers.add(code);
				}
			}

			overlay.drawMarkers(foundMarkers, contours);

			return foundMarkers;
		}
		finally
		{
			contours.clear();
			hierarchy.release();
		}
	}

	protected Marker.MarkerDetails createMarkerDetailsForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience)
	{
		Marker.MarkerDetails markerDetails = parseRegionsAt(nodeIndex, contours, hierarchy, experience);
		if (markerDetails != null)
		{
			this.sortCode(markerDetails);
			if (!validate(markerDetails, experience))
			{
				markerDetails = null;
			}
		}
		return markerDetails;
	}

	/**
	 * Override this method if your marker-code string representation is more complicated than an ordered list of numbers separated by colons.
	 */
	protected String getCodeFor(Marker.MarkerDetails details)
	{
		StringBuilder builder = new StringBuilder(details.regions.size() * 2);
		for (Map<String, Object> region : details.regions)
		{
			builder.append(region.get(Marker.MarkerDetails.REGION_VALUE));
			builder.append(':');
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}

	protected Marker.MarkerDetails parseRegionsAt(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience)
	{
		int currentRegionIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE];
		if (currentRegionIndex < 0)
		{
			// There are no regions.
			return null;
		}

		int regionCount = 0;
		int emptyRegions = 0;
		List<Map<String, Object>> regions = null;
		Integer embeddedChecksumValue = null, embeddedChecksumRegionIndex = null;

		// Loop through the regions, verifing the value of each:
		for (; currentRegionIndex >= 0; currentRegionIndex = (int) hierarchy.get(0, currentRegionIndex)[NEXT_NODE])
		{
			final int regionValue = getRegionValue(currentRegionIndex, hierarchy, experience.getMaxRegionValue());
			if (regionValue == REGION_EMPTY)
			{
				// Too many empty regions.
				return null;
			}

			if (regionValue == REGION_INVALID)
			{
				// Not a normal region so look for embedded checksum:
				if (experience.getEmbeddedChecksum() && embeddedChecksumValue == null) // if we've not found it yet:
				{
					embeddedChecksumValue = getEmbeddedChecksumValueForRegion(currentRegionIndex, hierarchy);
					if (embeddedChecksumValue != null)
					{
						embeddedChecksumRegionIndex = currentRegionIndex;
						continue; // this is a checksum region, so continue looking for regions
					}
				}

				// Too many levels or dots.
				return null;
			}

			if (++regionCount > experience.getMaxRegions())
			{
				// Too many regions.
				return null;
			}

			// Add region value to code:
			if (regions == null)
			{
				regions = new ArrayList<>();
			}
			Map<String, Object> region = new HashMap<>();
			region.put(Marker.MarkerDetails.REGION_INDEX, currentRegionIndex);
			region.put(Marker.MarkerDetails.REGION_VALUE, regionValue);
			regions.add(region);
		}

		// Marker should have at least one non-empty branch. If all branches are empty then return false.
		if ((regionCount - emptyRegions) < 1)
		{
			return null;
		}

		if (regions != null)
		{
			Marker.MarkerDetails details = new Marker.MarkerDetails();
			details.markerIndex = nodeIndex;
			details.regions = regions;
			details.embeddedChecksumRegionIndex = embeddedChecksumRegionIndex;
			details.embeddedChecksum = embeddedChecksumValue;
			return details;
		}

		return null;
	}

	/**
	 * Override this method to change the sorted order of the code.
	 */
	protected void sortCode(Marker.MarkerDetails details)
	{
		Collections.sort(details.regions, new Comparator<Map<String, Object>>()
		{
			@Override
			public int compare(Map<String, Object> region1, Map<String, Object> region2)
			{
				return ((Integer) region1.get(Marker.MarkerDetails.REGION_VALUE)).compareTo((Integer) region2.get(Marker.MarkerDetails.REGION_VALUE));
			}
		});
	}

	/**
	 * Override this method to change validation method.
	 */
	protected boolean validate(Marker.MarkerDetails details, Experience experience)
	{
		List<Integer> code = new ArrayList<>();
		for (Map<String, Object> region : details.regions)
		{
			code.add((Integer) region.get(Marker.MarkerDetails.REGION_VALUE));
		}

		return experience.isValidCode(code, details.embeddedChecksum);
	}
}