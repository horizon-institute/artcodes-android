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
	 * This function determines whether the input node is a valid branch. It is a valid branch if it contains zero or more dark regions.
	 *
	 * @param branchIndex Node index from the hierarchy.
	 * @param hierarchy   This contains the contours or components hierarchy.
	 * @return Returns branch status.
	 */
	static int getRegionValue(int branchIndex, Mat hierarchy, int regionMaxValue)
	{
		double[] nodes = hierarchy.get(0, branchIndex);
		int currentLeafIndex = (int) nodes[FIRST_NODE];
		if (currentLeafIndex < 0)
		{
			return 0;
		}

		int leafCount = 0;
		//loop until there is a leaf node.
		while (currentLeafIndex >= 0)
		{
			if (verifyLeaf(currentLeafIndex, hierarchy))
			{
				leafCount++;
				//get next leaf node.
				nodes = hierarchy.get(0, currentLeafIndex);
				currentLeafIndex = (int) nodes[NEXT_NODE];

				if (leafCount > regionMaxValue)
				{
					return REGION_INVALID;
				}
			}
			else
			{
				return REGION_INVALID;
			}
		}

		return leafCount;
	}

	/**
	 * This function detects whether a particular node is a d-touch marker. A d-touch contains one dark (black) region which is called the root. A root can have
	 * one or more light (white) regions. These light regions are called branches. Each branch can contain zero or more dark (black) regions and these dark regions
	 * are called leaves. If a branch has no leaf then it is called an empty branch. In conclusion Root==> 1 or more branches ==> 0 or more leaves.
	 *
	 * @param rootIndex The index of the node which needs to be identified as the root. It is used to access the hierarchy of this node from the hierarchy parameter.	 *
	 * @param hierarchy This contains the contours or components hierarchy using opencv findContours function.
	 * @return returns true if the root node is a valid d-touch marker otherwise returns false.
	 */
	static MarkerCode findMarker(Mat hierarchy, int rootIndex, Experience experience)
	{
		double[] nodes = hierarchy.get(0, rootIndex);
		int currentRegionIndex = (int) nodes[MarkerCode.FIRST_NODE];
		if (currentRegionIndex < 0)
		{
			return null;
		}

		int regions = 0;
		int emptyRegions = 0;
		List<Integer> codes = null;

		//loop until there is a branch node.
		while (currentRegionIndex >= 0)
		{
			//verify current branch.
			final int regionValue = MarkerCode.getRegionValue(currentRegionIndex, hierarchy, experience.getMaxRegionValue());
			//if branch is valid or empty.
			if (regionValue == MarkerCode.REGION_INVALID)
			{
				return null;
			}

			regions++;
			if (regions > experience.getMaxRegions())
			{
				return null;
			}
			if (regionValue == MarkerCode.REGION_EMPTY)
			{
				emptyRegions++;
				if (emptyRegions > experience.getMaxEmptyRegions())
				{
					return null;
				}
			}
			else
			{
				if (codes == null)
				{
					codes = new ArrayList<>();
				}
				codes.add(regionValue);
			}
			//get next node.
			nodes = hierarchy.get(0, currentRegionIndex);
			currentRegionIndex = (int) nodes[MarkerCode.NEXT_NODE];
		}

		//Marker should have at least one non-empty branch. If all branches are empty then return false.
		if ((emptyRegions - regions) == 0)
		{
			return null;
		}

		if (experience.isValidMarker(codes))
		{
			Collections.sort(codes);
			return new MarkerCode(codes, rootIndex);
		}

		return null;
	}

	/**
	 * This functions determines if the node is a valid leaf. It is a valid leaf if it does not have any child nodes.
	 */
	private static Boolean verifyLeaf(int leafIndex, Mat hierarchy)
	{
		//Get nodes of branch index.
		double[] nodes = hierarchy.get(0, leafIndex);
		//check if there is no child node.
		return nodes[FIRST_NODE] < 0;
	}
}