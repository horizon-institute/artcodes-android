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
package uk.ac.horizon.artcodes.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.util.Log;
import uk.ac.horizon.artcodes.scanner.BR;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

public class Experience extends BaseObservable
{
	private final List<Action> actions = new ArrayList<>();
	private final List<Availability> availabilities = new ArrayList<>();
	private final List<ImageProcessor> processors = new ArrayList<>();
	private String id;
	private String name;
	private String icon;
	private String image;
	private String description;
	private String author;
	private String callback;
	private String originalID;

	// Transient properties now calculated before use
	private transient int minRegions = 5;
	private transient int maxRegions = 5;
	private transient int maxRegionValue = 6;

	private int checksumModulo = 3;
	private boolean embeddedChecksum = false;
	private boolean editable = false;
	private String detector;

	public Experience()
	{
	}

	public List<Action> getActions()
	{
		return actions;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public List<Availability> getAvailabilities()
	{
		return availabilities;
	}

	public String getCallback()
	{
		return callback;
	}

	public void setCallback(String callback)
	{
		this.callback = callback;
	}

	public int getChecksumModulo()
	{
		return checksumModulo;
	}

	public void setChecksumModulo(int checksumModulo)
	{
		this.checksumModulo = checksumModulo;
	}

	public String getChecksumText()
	{
		return Integer.toString(checksumModulo);
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

	public String getDetector()
	{
		return detector;
	}

	public boolean getEmbeddedChecksum()
	{
		return embeddedChecksum;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public void setEmbeddedChecksum(boolean embeddedChecksum)
	{
		this.embeddedChecksum = embeddedChecksum;
	}

	@Bindable
	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
		notifyPropertyChanged(BR.icon);
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@Bindable
	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
		notifyPropertyChanged(BR.image);
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

	public String getNextUnusedCode()
	{
		update();
		List<Integer> code = null;
		while (true)
		{
			code = getNextCode(code);
			if (code == null)
			{
				return null;
			}

			if (isValidCode(code, null))
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
				// In sequence too?
				if (!hasCode(markerCode, Action.Match.any))
				{
					return markerCode;
				}
			}
		}
	}

	public String getOriginalID()
	{
		return originalID;
	}

	public void setOriginalID(String originalID)
	{
		this.originalID = originalID;
	}

	public boolean isSharable()
	{
		return id != null && (id.startsWith("http:") || id.startsWith("https:"));
	}

	public List<ImageProcessor> getProcessors()
	{
		return processors;
	}

	public boolean isValidCode(List<Integer> markerCodes, Integer embeddedChecksum)
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

		for (Integer value : markerCodes)
		{
			//check if leaves are using in accepted range.
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

		return true;
	}

	public void update()
	{
		int maxValue = 3;
		int minRegionCount = 100;
		int maxRegionCount = 3;
		for (Action action : actions)
		{
			for (String code : action.getCodes())
			{
				String[] values = code.split(":");
				minRegionCount = Math.min(minRegionCount, values.length);
				maxRegionCount = Math.max(maxRegionCount, values.length);
				for (String value : values)
				{
					try
					{
						int codeValue = Integer.parseInt(value);
						maxValue = Math.max(maxValue, codeValue);
					}
					catch (Exception e)
					{
						Log.w("", e.getMessage(), e);
					}
				}
			}
		}

		this.maxRegionValue = maxValue;
		this.minRegions = minRegionCount;
		this.maxRegions = maxRegionCount;
		Log.i("", "Regions " + minRegionCount + "-" + maxRegionCount + " using max of " + maxValue);

		// TODO Collections.sort(actions, Marker.comparator);
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

	private boolean hasCode(String code, Action.Match... matches)
	{
		for (Action action : actions)
		{
			for (Action.Match match : matches)
			{
				if (match == action.getMatch())
				{
					if (action.getCodes().contains(code))
					{
						return true;
					}
				}
			}
		}
		return false;
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

	public enum Status
	{
		loaded, modified, saving
	}
}