/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the marker code and the index of the root component.
 *
 * @author pszsa1
 */
public class MarkerCode
{
	private int index;
	private List<Integer> code;
	private int occurrences = 1;

	//constructors
	public MarkerCode()
	{
		super();
	}

	public MarkerCode(List<Integer> code)
	{
		super();
		this.code = code;
	}

	public MarkerCode(String code)
	{
		super();
		setCode(code);
	}

	public int getOccurrences()
	{
		return occurrences;
	}

	public void setOccurrences(int value)
	{
		this.occurrences = value;
	}

	private static List<Integer> getCodeArrayFromString(String code)
	{
		String tmpCodes[] = code.split(":");
		List<Integer> codes = new ArrayList<Integer>();
		for (String tmpCode : tmpCodes)
		{
			codes.add(Integer.valueOf(tmpCode));
		}
		return codes;
	}

	public int getComponentIndex()
	{
		return index;
	}

	public void setComponentIndex(int componentIndex)
	{
		index = componentIndex;
	}

	public List<Integer> getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		setCode(getCodeArrayFromString(code));
	}

	public void setCode(List<Integer> code)
	{
		this.code = code;
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

	// Object methods for use in Map, HashMap etc.
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
}