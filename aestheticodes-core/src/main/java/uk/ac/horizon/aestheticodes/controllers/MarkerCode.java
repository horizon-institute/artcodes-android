/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes. 
 * Copyright (C) 2015  Aestheticodes
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

package uk.ac.horizon.aestheticodes.controllers;

import org.opencv.core.Mat;
import uk.ac.horizon.aestheticodes.model.Experience;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MarkerCode
{
	static final int REGION_INVALID = -1;
	static final int REGION_EMPTY = 0;

	//indexes of leaf nodes in contour tree hierarchy.
	static final int NEXT_NODE = 0;
	static final int FIRST_NODE = 2;

	private final int index;
	private final List<Integer> code;
	private int occurrences = 1;

	public MarkerCode(List<Integer> code, int index)
	{
		super();
		this.code = code;
		this.index = index;
	}

	public int getOccurrences()
	{
		return occurrences;
	}

	public void setOccurrences(int value)
	{
		this.occurrences = value;
	}

	public int getComponentIndex()
	{
		return index;
	}

	public List<Integer> getCode()
	{
		return code;
	}

	public String getCodeKey()
	{
		if (code != null)
		{
			StringBuilder codeString = new StringBuilder();
			for (int i = 0; i < code.size(); i++)
			{
				if (i > 0)
				{
					codeString.append(":");
				}
				codeString.append(code.get(i));
			}
			return codeString.toString();
		}
		return null;
	}

	boolean isCodeEqual(MarkerCode marker)
	{
		return getCodeKey().equals(marker.getCodeKey());
	}

	public int hashCode()
	{
		int hash = 0;
		for (int i : code)
		{
			hash += i;
		}
		return hash;
	}

	public boolean equals(Object m)
	{
		return m.getClass() == this.getClass() && isCodeEqual((MarkerCode) m);
	}

    /**
     * This function determines whether the input node is a valid region. It is a valid region if it contains zero or more dots.
     *
     * @param regionIndex Region index in the hierarchy.
     * @param hierarchy   This contains the contours or components hierarchy.
     * @param regionMaxValue The maximum number of dots allowed in a region.
     * @return Returns region status.
     */
    static int getRegionValue(int regionIndex, Mat hierarchy, int regionMaxValue)
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
     * This function detects whether a particular node is an Aestheticode marker. An Aestheticode contains one dark (black) area which is called the root. A root can contain
     * one or more light (white) areas, these are called regions. Each region can contain zero or more dark (black) areas, these
     * are called dots. If a region has no dot then it is called an empty region. Colours can be inverted (e.g. a light root has dark regions and light dots).
     *
     * @param rootIndex The index of the node which needs to be identified as the root. It is used to access the hierarchy of this node from the hierarchy parameter.	 *
     * @param hierarchy This contains the contours or components hierarchy using opencv findContours function.
     * @return returns A MarkerCode if the root node is a valid Aestheticode marker otherwise returns null.
     */
    static MarkerCode findMarker(Mat hierarchy, int rootIndex, Experience experience)
    {
        int currentRegionIndex = (int) hierarchy.get(0, rootIndex)[MarkerCode.FIRST_NODE];
        if (currentRegionIndex < 0)
        {
            return null; // There are no regions.
        }

        int regions = 0;
        int emptyRegions = 0;
        List<Integer> code = null;
        Integer embeddedChecksumValue = null;

        // Loop through the regions, verifing the value of each:
        for (;currentRegionIndex >= 0; currentRegionIndex = (int) hierarchy.get(0, currentRegionIndex)[MarkerCode.NEXT_NODE])
        {
            final int regionValue = MarkerCode.getRegionValue(currentRegionIndex, hierarchy, experience.getMaxRegionValue());
            if (regionValue == MarkerCode.REGION_EMPTY)
            {
                if (++emptyRegions > experience.getMaxEmptyRegions())
                {
                    return null; // Too many empty regions.
                }
            }

            if (regionValue == MarkerCode.REGION_INVALID)
            {
                // Not a normal region so look for embedded checksum:
                if (experience.getEmbeddedChecksum() && embeddedChecksumValue == null) // if we've not found it yet:
                {
                    embeddedChecksumValue = MarkerCode.getEmbeddedChecksumValueForRegion(currentRegionIndex, hierarchy);
                    if (embeddedChecksumValue != null)
                    {
                        continue; // this is a checksum region, so continue looking for regions
                    }
                }

                return null; // Too many levels or dots.
            }

            if (++regions > experience.getMaxRegions())
            {
                return null; // Too many regions.
            }

            // Add region value to code:
            if (code == null)
            {
                code = new ArrayList<>();
            }
            code.add(regionValue);
        }

        // Marker should have at least one non-empty branch. If all branches are empty then return false.
        if ((regions - emptyRegions) < 1)
        {
            return null;
        }

        Collections.sort(code); // Sort before checking if valid because this could effect the embedded checksum
        if (experience.isValidMarker(code, embeddedChecksumValue))
        {
            return new MarkerCode(code, rootIndex);
        }

        return null;
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
                hierarchy.get(0,(int)nodes[FIRST_NODE])[NEXT_NODE] < 0 && //the child has no siblings, and
                verifyAsLeaf((int)nodes[FIRST_NODE], hierarchy);// the child is a leaf
    }

}