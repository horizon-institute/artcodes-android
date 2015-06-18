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

package uk.ac.horizon.artcodes.model;/*
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

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Experience extends BaseObservable
{
	@SuppressWarnings("unused")
	public enum Operation
	{
		create, retrieve, update, deleted, add, remove
	}

	public enum Threshold
	{
		temporalTile, resize
	}

	private final List<Marker> markers = new ArrayList<>();
	private final List<Availability> availabilities = new ArrayList<>();

	private String id;
	private String name;
	private String icon;
	private String image;
	private String description;
	private int version = 1;
	//private String ownerID;
	private String author;
	private String callback;
	private String markerFactory;

	private Long updated;
	private Long created;

	private String originalID;

	private Operation op = null;
	private int minRegions = 5;
	private int maxRegions = 5;
	private int maxEmptyRegions = 0;
	private int maxRegionValue = 6;
	private int validationRegions = 0;
	private int validationRegionValue = 1;
	private int checksumModulo = 3;
	private boolean embeddedChecksum = false;
	private Threshold threshold = Threshold.temporalTile;

	public Experience()
	{
	}

	public List<Availability> getAvailabilities()
	{
		return availabilities;
	}

	public Long getUpdated()
	{
		return updated;
	}

	public void setUpdated(Long updated)
	{
		this.updated = updated;
	}

	public String getCallback()
	{
		return callback;
	}

	public void setCallback(String callback)
	{
		this.callback = callback;
	}

	@Bindable
	public int getChecksumModulo()
	{
		return checksumModulo;
	}

	public String getChecksumText()
	{
		return Integer.toString(checksumModulo);
	}

	public void setChecksumModulo(int checksumModulo)
	{
		this.checksumModulo = checksumModulo;
	}

	@Bindable
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean getEmbeddedChecksum()
	{
		return embeddedChecksum;
	}

	public void setEmbeddedChecksum(boolean embeddedChecksum)
	{
		this.embeddedChecksum = embeddedChecksum;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public List<Marker> getMarkers()
	{
		return markers;
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

	public int getMaxRegions()
	{
		return maxRegions;
	}

	public void setMaxRegions(int maxRegions)
	{
		this.maxRegions = maxRegions;
	}

	public int getMinRegions()
	{
		return minRegions;
	}

	public void setMinRegions(int minRegions)
	{
		this.minRegions = minRegions;
	}

	@Bindable
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Marker getMarker(String code)
	{
		for (Marker marker : markers)
		{
			if (code.equals(marker.getCode()))
			{
				return marker;
			}
		}
		return null;
	}

	public String getNextUnusedMarker()
	{
		if (markers.isEmpty())
		{
			return "1:1:1:1:1";
		}
		List<Integer> code = null;
		while (true)
		{
			code = getNextCode(code);
			if (code == null)
			{
				return null;
			}

			if (isValidMarker(code, null))
			{
				StringBuilder result = new StringBuilder();
				for (int index = 0; index < code.size(); index++)
				{
					if (index != 0)
					{
						result.append(":");
					}
					result.append(code.get(index));
				}

				String markerCode = result.toString();
				if (getMarker(markerCode) == null)
				{
					return markerCode;
				}
			}
		}
	}

	List<Integer> getNextCode(List<Integer> code)
	{
		if (code == null)
		{
			int size = minRegions;
			code = new ArrayList<>();
			for (int index = 0; index < size; index++)
			{
				code.add(1);
			}
			return code;
		}

		int size = code.size();
		for (int i = (size - 1); i >= 0; i--)
		{
			int number = code.get(i);
			int value = number + 1;
			code.set(i, value);
			if (value <= maxRegionValue)
			{
				break;
			}
			else if (i == 0)
			{
				if (size == maxRegions)
				{
					return null;
				}
				else
				{
					size++;
					code = new ArrayList<>();
					for (int index = 0; index < size; index++)
					{
						code.add(1);
					}
					return code;
				}
			}
			else
			{
				number = code.get(i - 1);
				value = number + 1;
				code.set(i, value);
			}
		}

		return code;
	}

	public void update()
	{
		int maxValue = 3;
		int minRegion = 100;
		int maxRegion = 3;
		for (Marker marker : markers)
		{
			String[] values = marker.getCode().split(":");
			minRegion = Math.min(minRegion, values.length);
			maxRegion = Math.max(maxRegion, values.length);
			for (String value : values)
			{
				try
				{
					int codeValue = Integer.parseInt(value);
					maxValue = Math.max(maxValue, codeValue);
				}
				catch (Exception e)
				{
				}
			}
		}

		this.maxRegionValue = maxValue;
		this.minRegions = minRegion;
		this.minRegions = maxRegion;

		Collections.sort(markers, Marker.comparator);
	}

	public Operation getOp()
	{
		return op;
	}

	public void setOp(Operation op)
	{
		this.op = op;
	}

	public String getOriginalID()
	{
		return originalID;
	}

	public void setOriginalID(String originalID)
	{
		this.originalID = originalID;
	}

	public Threshold getThreshold()
	{
		return threshold;
	}

	public int getValidationRegionValue()
	{
		return validationRegionValue;
	}

	public void setValidationRegionValue(int validationRegionValue)
	{
		this.validationRegionValue = validationRegionValue;
	}

	public int getValidationRegions()
	{
		return validationRegions;
	}

	public void setValidationRegions(int validationRegions)
	{
		this.validationRegions = validationRegions;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public boolean isValidMarker(List<Integer> markerCodes, Integer embeddedChecksum)
	{
		if (markerCodes == null)
		{
			return false; // No Code
		}
		else if (markerCodes.size() < minRegions)
		{
			return false; // Too Short
		}
		else if (markerCodes.size() > maxRegions)
		{
			return false; // Too long
		}
		else if (!hasValidNumberofEmptyRegions(markerCodes))
		{
			return false; // Incorrect Empty Regions
		}

		for (Integer value : markerCodes)
		{
			//check if leaves are with in accepted range.
			if (value > maxRegionValue)
			{
				return false; // value is too Big
			}
		}

		if (embeddedChecksum == null && !hasValidChecksum(markerCodes))
		{
			return false; // Region Total not Divisable by checksumModulo
		}
		else if (this.embeddedChecksum && embeddedChecksum != null && !hasValidEmbeddedChecksum(markerCodes, embeddedChecksum))
		{
			return false; // Region Total not Divisable by embeddedChecksum
		}
		else if (!this.embeddedChecksum && embeddedChecksum != null)
		{
			// Embedded checksum is turned off yet one was provided to this function (this should never happen unless the settings are changed in the middle of detection)
			return false; // Embedded checksum markers are not valid.
		}

		return hasValidationRegions(markerCodes);
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

	private boolean hasValidEmbeddedChecksum(List<Integer> code, Integer embeddedChecksum)
	{
		// Find weighted sum of code, e.g. 1:1:2:4:4 -> 1*1 + 1*2 + 2*3 + 4*4 + 4*5 = 45
		int weightedSum = 0;
		for (int i = 0; i < code.size(); ++i)
		{
			weightedSum += code.get(i) * (i + 1);
		}
		return embeddedChecksum == (weightedSum % 7 == 0 ? 7 : weightedSum % 7);
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
		return maxEmptyRegions >= empty;
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
}
