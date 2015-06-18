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

package uk.ac.horizon.artcodes.scanner.marker;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.model.Experience;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerCodeFactory implements MarkerDrawer
{
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private static final Scalar regionColour = new Scalar(255, 128, 0, 255);
	private static final Scalar outlineColour = new Scalar(0, 0, 0, 255);

	/**
	 * Possible error states that can be set when creating a MarkerCode
	 */
	public enum DetectionError
	{
		noSubContours,
		tooManyEmptyRegions,
		nestedRegions,
		numberOfRegions,
		numberOfDots,
		checksum,
		validationRegions,
		OK,
		unknown
	}

	public void generateExtraFrameDetails(Mat thresholdedImage, List<MatOfPoint> contours, Mat hierarchy)
	{
	}

	public MarkerCode createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionError[] error, int errorIndex)
	{
		MarkerCode.MarkerDetails markerDetails = this.createMarkerDetailsForNode(nodeIndex, contours, hierarchy, experience, error, errorIndex);
		if (markerDetails != null)
		{
			return new MarkerCode(this.getCodeFor(markerDetails), markerDetails, this);
		}
		else
		{
			return null;
		}
	}

	public List<MarkerCode> findMarkers(Mat image, Mat drawImage, Experience experience)
	{
		final ArrayList<MatOfPoint> contours = new ArrayList<>();
		final Mat hierarchy = new Mat();
		try
		{
			// holds all the markers identified in the camera.
			List<MarkerCode> foundMarkers = new ArrayList<>();
			// Find blobs using connect component.
			Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
			for (int i = 0; i < contours.size(); i++)
			{
				MarkerCodeFactory.DetectionError[] error = new MarkerCodeFactory.DetectionError[1];
				final MarkerCode code = createMarkerForNode(i, contours, hierarchy, experience, error, 0);
				if (code != null)
				{
					// if marker found then add in the list.
					foundMarkers.add(code);

					code.draw(drawImage, contours, hierarchy);
				}
			}

			return foundMarkers;
		}
		finally
		{
			contours.clear();
			hierarchy.release();
		}
	}

	protected MarkerCode.MarkerDetails createMarkerDetailsForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionError[] error, int errorIndex)
	{
		MarkerCode.MarkerDetails markerDetails = this.parseRegionsAt(nodeIndex, contours, hierarchy, experience, error, errorIndex);
		if (markerDetails != null)
		{
			this.sortCode(markerDetails);
			if (!this.validate(markerDetails, experience, error, errorIndex))
			{
				markerDetails = null;
			}
		}
		return markerDetails;
	}

	protected static final int REGION_INVALID = -1;
	protected static final int REGION_EMPTY = 0;

	//indexes of leaf nodes in contour tree hierarchy.
	protected static final int NEXT_NODE = 0;
	protected static final int FIRST_NODE = 2;

	protected MarkerCode.MarkerDetails parseRegionsAt(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionError[] error, int errorIndex)
	{
		int currentRegionIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE];
		if (currentRegionIndex < 0)
		{
			error[errorIndex] = DetectionError.noSubContours;
			return null; // There are no regions.
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
				if (++emptyRegions > experience.getMaxEmptyRegions())
				{
					error[errorIndex] = DetectionError.tooManyEmptyRegions;
					return null; // Too many empty regions.
				}
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

				error[errorIndex] = DetectionError.nestedRegions;
				return null; // Too many levels or dots.
			}

			if (++regionCount > experience.getMaxRegions())
			{
				error[errorIndex] = DetectionError.numberOfRegions;
				return null; // Too many regions.
			}

			// Add region value to code:
			if (regions == null)
			{
				regions = new ArrayList<>();
			}
			Map<String, Object> region = new HashMap<>();
			region.put(MarkerCode.MarkerDetails.REGION_INDEX, currentRegionIndex);
			region.put(MarkerCode.MarkerDetails.REGION_VALUE, regionValue);
			regions.add(region);
		}

		// Marker should have at least one non-empty branch. If all branches are empty then return false.
		if ((regionCount - emptyRegions) < 1)
		{
			error[errorIndex] = DetectionError.tooManyEmptyRegions;
			return null;
		}

		if (regions != null)
		{
			MarkerCode.MarkerDetails details = new MarkerCode.MarkerDetails();
			details.markerIndex = nodeIndex;
			details.regions = regions;
			details.embeddedChecksumRegionIndex = embeddedChecksumRegionIndex;
			details.embeddedChecksum = embeddedChecksumValue;
			return details;
		}

		return null;
	}

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

	/**
	 * Override this method to change the sorted order of the code.
	 */
	protected void sortCode(MarkerCode.MarkerDetails details)
	{
		Collections.sort(details.regions, new Comparator<Map<String, Object>>()
		{
			@Override
			public int compare(Map<String, Object> region1, Map<String, Object> region2)
			{
				return ((Integer) region1.get(MarkerCode.MarkerDetails.REGION_VALUE)).compareTo((Integer) region2.get(MarkerCode.MarkerDetails.REGION_VALUE));
			}
		});
	}

	/**
	 * Override this method to change validation method.
	 */
	protected boolean validate(MarkerCode.MarkerDetails details, Experience experience, DetectionError[] error, int errorIndex)
	{
		//NSMutableString *strError = [[NSMutableString alloc] init];
		//NSArray *code = [details.regions valueForKey:REGION_VALUE];
		List<Integer> code = new ArrayList<>();
		for (Map<String, Object> region : details.regions)
		{
			code.add((Integer) region.get(MarkerCode.MarkerDetails.REGION_VALUE));
		}

		boolean result = experience.isValidMarker(code, details.embeddedChecksum);

        /*if ([strError rangeOfString:@"Too many dots"].location != NSNotFound)
        {
            error[0] = numberOfDots;
        }
        else if ([strError rangeOfString:@"checksum"].location != NSNotFound)
        {
            error[0] = checksum;
        }
        else if ([strError rangeOfString:@"Validation regions"].location != NSNotFound)
        {
            error[0] = validationRegions;
        }*/

		return result;
	}

	/**
	 * Override this method if your marker-code string representation is more complicated than an ordered list of numbers separated by colons.
	 */
	protected String getCodeFor(MarkerCode.MarkerDetails details)
	{
		StringBuilder builder = new StringBuilder(details.regions.size() * 2);
		for (Map<String, Object> region : details.regions)
		{
			builder.append(region.get(MarkerCode.MarkerDetails.REGION_VALUE));
			builder.append(':');
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}

	@Override
	public void draw(MarkerCode marker, Mat image, List<MatOfPoint> contours, Mat hierarchy)
	{

		for (MarkerCode.MarkerDetails details : marker.getMarkerDetails())
		{
			if (regionColour != null)
			{
				// draw regions
				for (Map<String, Object> region : details.regions)
				{
					int currentRegionIndex = (Integer) region.get(MarkerCode.MarkerDetails.REGION_INDEX);
					Imgproc.drawContours(image, contours, currentRegionIndex, outlineColour, 4);
					Imgproc.drawContours(image, contours, currentRegionIndex, regionColour, 2);
				}
			}

			if (detectedColour != null)
			{
				// draw marker outline
				Imgproc.drawContours(image, contours, details.markerIndex, outlineColour, 7);
				Imgproc.drawContours(image, contours, details.markerIndex, detectedColour, 5);
			}
		}
	}
}