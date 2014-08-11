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

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.aestheticodes.settings.ThresholdBehaviour;

/**
 * This class defines the constraints for markers.It contains two sets
 * of parameters. The first set defines the markers which needs to be
 * identified. For example max & min branches in a marker, empty branches and
 * max leaves in a branch. The second set of parameters are used to define to
 * validate a marker. It defines the number of validation branches, leaves in a
 * validation branch and the checksumModulo modulo.
 */
public class MarkerSettings
{
	private static final MarkerSettings settings = new MarkerSettings();

	public static MarkerSettings getSettings()
	{
		return settings;
	}

	public void setSettings(MarkerSettings settings)
	{
		minRegions = settings.minRegions;
		maxRegions = settings.maxRegions;
		maxEmptyRegions = settings.maxEmptyRegions;
		maxRegionValue = settings.maxRegionValue;
		validationRegions = settings.validationRegions;
		validationRegionValue = settings.validationRegionValue;
		checksumModulo = settings.checksumModulo;
		markers.clear();
		markers.putAll(settings.markers);
		modes.clear();
		modes.addAll(settings.modes);

		updateURL = settings.updateURL;
		lastUpdate = settings.lastUpdate;

        if (settings.thresholdBehaviour != null)
        {
            thresholdBehaviour = settings.thresholdBehaviour;
        }

		editable = settings.editable;
	}

	private final List<Mode> modes = new ArrayList<Mode>();
	private final Map<String, MarkerAction> markers = new HashMap<String, MarkerAction>();
	private int minRegions = 5;
	private int maxRegions = 5;
	private int maxEmptyRegions = 0;
	private int maxRegionValue = 6;
	private int validationRegions = 2;
	private int validationRegionValue = 1;
	private int checksumModulo = 3;
	private boolean editable = true;
	private boolean addMarker = true;
	private Date lastUpdate;
	private transient boolean changed = false;
	private String updateURL = "http://www.wornchaos.org/settings.json";

    private String thresholdBehaviour = null;

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
		changed = true;
	}

	public int getMaxRegions()
	{
		return maxRegions;
	}

	public void setMaxRegions(int maxRegions)
	{
		this.maxRegions = maxRegions;
		changed = true;
	}

	public int getMaxEmptyRegions()
	{
		return maxEmptyRegions;
	}

	public void setMaxEmptyRegions(int maxEmptyRegions)
	{
		this.maxEmptyRegions = maxEmptyRegions;
		changed = true;
	}

	public int getMaxRegionValue()
	{
		return maxRegionValue;
	}

	public void setMaxRegionValue(int maxRegionValue)
	{
		this.maxRegionValue = maxRegionValue;
		changed = true;
	}

	public int getValidationRegions()
	{
		return validationRegions;
	}

	public void setValidationRegions(int validationRegions)
	{
		this.validationRegions = validationRegions;
		changed = true;
	}

	public int getValidationRegionValue()
	{
		return validationRegionValue;
	}

	public void setValidationRegionValue(int validationRegionValue)
	{
		this.validationRegionValue = validationRegionValue;
		changed = true;
	}

	public int getChecksumModulo()
	{
		return checksumModulo;
	}

	public void setChecksumModulo(int checksumModulo)
	{
		this.checksumModulo = checksumModulo;
		changed = true;
	}

    public ThresholdBehaviour getThresholdBehaviour()
    {
        if (this.thresholdBehaviour==null || this.thresholdBehaviour.equals("temporalTile"))
        {
            return ThresholdBehaviour.temporalTile;
        }
        else if (this.thresholdBehaviour.equals("resize"))
        {
            return ThresholdBehaviour.resize;
        }
        else
        {
            Log.w(this.getClass().getName(), "Unsupported threshold behaviour: "+this.thresholdBehaviour);
            return ThresholdBehaviour.temporalTile;
        }
    }

    public void setThresholdBehaviour(String thresholdBehaviour)
    {
        this.thresholdBehaviour = thresholdBehaviour;
        this.changed = true;
    }

	public boolean isValidMarker(List<Integer> markerCodes)
	{
		return isValidMarker(markerCodes, false);
	}

	public Map<String, MarkerAction> getMarkers()
	{
		return markers;
	}

    /**
     * Delete a marker from the list of markers.
     * @param code The code of the marker to delete.
     * @return True if a marker was deleted, false if the given code was not found.
     */
    public boolean deleteMarker(String code) {
        if (this.markers.containsKey(code)) {
            this.markers.remove(code);
            this.setChanged(true);
            return true;
        } else {
            return false;
        }
    }

	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}

	public boolean isValidMarker(List<Integer> markerCodes, boolean partial)
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

		if (!partial)
		{
			return isValidMarker(codes);
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

	public String getUpdateURL()
	{
		return updateURL;
	}

	public boolean canAddMarker()
	{
		return addMarker;
	}

	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate)
	{
		this.lastUpdate = lastUpdate;
		changed = true;
	}

	public boolean hasChanged()
	{
		return changed;
	}
}
