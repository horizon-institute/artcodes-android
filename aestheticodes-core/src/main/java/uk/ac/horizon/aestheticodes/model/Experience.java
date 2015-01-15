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

package uk.ac.horizon.aestheticodes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Experience
{
	public static enum Operation
	{
		create, retrieve, update, deleted, remove, temp
	}

	public static enum Threshold
	{
		temporalTile, resize
	}

	private final Map<String, Marker> markers = new HashMap<String, Marker>();

	private String id;
	private String name;
	private String icon;
	private String image;
	private String description;
	private int version = 1;
	private String ownerID;

	private String originalID;
	private int originalVersion;

	private Operation op = null;

	private int minRegions = 5;
	private int maxRegions = 5;
	private int maxEmptyRegions = 0;
	private int maxRegionValue = 6;
	private int validationRegions = 2;
	private int validationRegionValue = 1;
	private int checksumModulo = 3;
	private Threshold threshold = Threshold.temporalTile;

	public Experience()
	{
	}

	public int getMinRegions()
	{
		return minRegions;
	}

	public void setMinRegions(int minRegions)
	{
		this.minRegions = minRegions;
	}

	public int getMaxRegions()
	{
		return maxRegions;
	}

	public void setMaxRegions(int maxRegions)
	{
		this.maxRegions = maxRegions;
	}

	public int getMaxEmptyRegions()
	{
		return maxEmptyRegions;
	}

	public void setMaxEmptyRegions(int maxEmptyRegions)
	{
		this.maxEmptyRegions = maxEmptyRegions;
	}

	public int getMaxRegionValue()
	{
		return maxRegionValue;
	}

	public void setMaxRegionValue(int maxRegionValue)
	{
		this.maxRegionValue = maxRegionValue;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getNextUnusedMarker()
	{
		for (int size = minRegions; size <= maxRegions; size++)
		{
			final List<Integer> marker = new ArrayList<Integer>();
			for (int index = 0; index < size; index++)
			{
				marker.add(1);
			}

			while (true)
			{
				if (isValidMarker(marker, false))
				{
					StringBuilder result = new StringBuilder();
					for (int index = 0; index < size; index++)
					{
						if (index != 0)
						{
							result.append(":");
						}
						result.append(marker.get(index));
					}

					String code = result.toString();
					if (!markers.containsKey(code))
					{
						return code;
					}
				}

				for (int i = (size - 1); i >= 0; i--)
				{
					int value = marker.get(i) + 1;
					marker.set(i, value);
					if (value <= maxRegionValue)
					{
						break;
					}
					else if (i == 0)
					{
						return null;
					}
					else
					{
						marker.set(i, marker.get(i - 1));
					}
				}
			}
		}

		return null;
	}

	public void add(Marker marker)
	{
		markers.put(marker.getCode(), marker);
	}

	public int getValidationRegions()
	{
		return validationRegions;
	}

	public void setValidationRegions(int validationRegions)
	{
		this.validationRegions = validationRegions;
	}

	public int getValidationRegionValue()
	{
		return validationRegionValue;
	}

	public void setValidationRegionValue(int validationRegionValue)
	{
		this.validationRegionValue = validationRegionValue;
	}

	public int getChecksumModulo()
	{
		return checksumModulo;
	}

	public void setChecksumModulo(int checksumModulo)
	{
		this.checksumModulo = checksumModulo;
	}

	public Threshold getThreshold()
	{
		return threshold;
	}

	public String getIcon()
	{
		return icon;
	}

	public boolean isValidMarker(List<Integer> markerCodes)
	{
		return isValidMarker(markerCodes, false);
	}

	public Map<String, Marker> getMarkers()
	{
		return markers;
	}

	/**
	 * Delete a marker from the list of markers.
	 *
	 * @param code The code of the marker to delete.
	 * @return True if a marker was deleted, false if the given code was not found.
	 */
	public boolean deleteMarker(String code)
	{
		if (this.markers.containsKey(code))
		{
			this.markers.remove(code);
			return true;
		}
		else
		{
			return false;
		}
	}

	boolean isValidMarker(List<Integer> markerCodes, boolean partial)
	{
		return markerCodes != null
				&& hasValidNumberofRegions(markerCodes)
				&& hasValidNumberofEmptyRegions(markerCodes)
				&& hasValidNumberOfLeaves(markerCodes)
				&& hasValidationRegions(markerCodes)
				&& hasValidChecksum(markerCodes);
	}

	public boolean isValidMarker(String marker, boolean partial)
	{
		String[] values = marker.split(":");
		if (!partial)
		{
			if (values.length < minRegions)
			{
				return false;
			}
		}

		if (values.length > maxRegions)
		{
			return false;
		}

		int prevValue = 0;
		List<Integer> codes = new ArrayList<Integer>();
		for (String value : values)
		{
			try
			{
				int codeValue = Integer.parseInt(value);
				if (codeValue > maxRegionValue || codeValue < prevValue)
				{
					return false;
				}

				codes.add(codeValue);

				prevValue = codeValue;
			}
			catch (Exception e)
			{
				return false;
			}
		}

		return partial || isValidMarker(codes);

	}

	/**
	 * It checks the number of validation branches as given in the preferences.
	 * The code is valid if the number of branches which contains the validation
	 * code are equal or greater than the number of validation branches
	 * mentioned in the preferences.
	 *
	 * @return true if the number of validation branches are >= validation
	 * branch value in the preference otherwise it returns false.
	 */
	private boolean hasValidationRegions(List<Integer> markerCodes)
	{
		if (validationRegions <= 0)
		{
			return true;
		}
		int validationRegionCount = 0;
		for (int code : markerCodes)
		{
			if (code == validationRegionValue)
			{
				validationRegionCount++;
			}
		}
		return validationRegionCount >= validationRegions;
	}

	/**
	 * This function divides the total number of leaves in the marker by the
	 * value given in the checksumModulo preference. Code is valid if the modulo is 0.
	 *
	 * @return true if the number of leaves are divisible by the checksumModulo value
	 * otherwise false.
	 */
	private boolean hasValidChecksum(List<Integer> markerCodes)
	{
		if (checksumModulo <= 1)
		{
			return true;
		}
		int numberOfLeaves = 0;
		for (int code : markerCodes)
		{
			numberOfLeaves += code;
		}
		return (numberOfLeaves % checksumModulo) == 0;
	}

	private boolean hasValidNumberofRegions(List<Integer> marker)
	{
		return ((marker.size() >= minRegions) && (marker.size() <= maxRegions));
	}

	private boolean hasValidNumberofEmptyRegions(List<Integer> marker)
	{
		int empty = 0;
		for (Integer value : marker)
		{
			if (value == 0)
			{
				empty++;
			}
		}
		return maxEmptyRegions == empty;
	}

	private boolean hasValidNumberOfLeaves(List<Integer> marker)
	{
		for (Integer value : marker)
		{
			//check if leaves are with in accepted range.
			if (value > maxRegionValue)
			{
				return false;
			}
		}
		return true;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getImage()
	{
		return image;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Operation getOp()
	{
		return op;
	}

	public String getOwnerID()
	{
		return ownerID;
	}

	public void setOwnerID(String ownerID)
	{
		this.ownerID = ownerID;
	}

	public void setOriginalID(String originalID)
	{
		this.originalID = originalID;
	}

	public void setOp(Operation op)
	{
		this.op = op;
	}

	public String getOriginalID()
	{
		return originalID;
	}
}
