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

package uk.ac.horizon.aestheticodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class defines the constraints for markers.It contains two sets
 * of parameters. The first set defines the markers which needs to be
 * identified. For example max & min branches in a marker, empty branches and
 * max leaves in a branch. The second set of parameters are used to define to
 * validate a marker. It defines the number of validation branches, leaves in a
 * validation branch and the checksumModulo modulo.
 *
 * @author pszsa1
 */
public class MarkerSettings
{
	private static final MarkerSettings settings = new MarkerSettings();

	public static MarkerSettings getSettings()
	{
		return settings;
	}

	private int minRegions = 5;
	private int maxRegions = 5;
	private int maxEmptyRegions = 0;
	private int maxRegionValue = 6;
	private int validationRegions = 2;
	private int validationRegionValue = 1;
	private int checksumModulo = 6;

	private String updateURL;

	// marker occurrence value
	private int defaultMarkerOccurrence = 1;

	private List<Mode> modes = new ArrayList<Mode>();
	private Map<String, MarkerAction> markers = new HashMap<String, MarkerAction>();

	public MarkerSettings()
	{
		modes.addAll(Arrays.asList(Mode.values()));
	}

	public List<Mode> getModes()
	{
		return modes;
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

	public boolean isValidMarker(List<Integer> markerCodes)
	{
		return isValidMarker(markerCodes, false);
	}

	public Map<String, MarkerAction> getMarkers()
	{
		return markers;
	}

	public boolean isValidMarker(List<Integer> markerCodes, boolean partial)

	{
		return hasValidNumberofRegions(markerCodes)
				&& hasValidNumberofEmptyRegions(markerCodes)
				&& hasValidNumberOfLeaves(markerCodes)
				&& hasValidationRegions(markerCodes)
				&& hasValidChecksum(markerCodes);
	}

	public boolean isValidMarker(String marker, boolean partial)
	{
		int count = marker.length() - marker.replace(".", "").length();
		int maxCodeLength = maxRegions;
		if (count >= maxCodeLength)
		{
			return false;
		}

		if (!partial)
		{
			int minCodeLength = minRegions;
			if (count < minCodeLength)
			{
				return false;
			}
		}

		String[] values = marker.split(":");
		if (values.length > maxCodeLength)
		{
			return false;
		}

		int prevValue = 0;
		int maxCodeValue = maxRegionValue;
		for (String value : values)
		{
			try
			{
				int codeValue = Integer.parseInt(value);
				if (codeValue < 1 || codeValue > maxCodeValue || codeValue < prevValue)
				{
					return false;
				}

				prevValue = codeValue;
			}
			catch (Exception e)
			{
				return false;
			}
		}

		return true;
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
		int numberOfLeaves = 0;
		for (int code : markerCodes)
		{
			numberOfLeaves += code;
		}
		if (checksumModulo > 0)
		{
			double checksum = numberOfLeaves % checksumModulo;
			if (checksum == 0)
			{
				return true;
			}
		}
		return false;
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
}
