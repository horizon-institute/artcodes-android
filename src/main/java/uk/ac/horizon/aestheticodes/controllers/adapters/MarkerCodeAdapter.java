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

package uk.ac.horizon.aestheticodes.controllers.adapters;

import android.text.InputFilter;
import android.text.Spanned;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class MarkerCodeAdapter extends TextAdapter<Marker> implements InputFilter
{
	private static final int MIN_REGIONS = 3;
	private static final int MAX_REGIONS = 20;
	private static final int MAX_VALUE = 20;

	public MarkerCodeAdapter(Experience experience)
	{
		super("code");
		this.experience = experience;
	}

	private Experience experience;
	private String original;

	@Override
	public CharSequence filter(CharSequence source, int sourceStart, int sourceEnd, Spanned destination, int destinationStart, int destinationEnd)
	{
		String sourceValue = source.subSequence(sourceStart, sourceEnd).toString();
		if (sourceValue.equals(" "))
		{
			sourceValue = ":";
		}

		String result = destination.subSequence(0, destinationStart).toString() + sourceValue +
				destination.subSequence(destinationEnd, destination.length()).toString();
		if (result.equals(""))
		{
			return sourceValue;
		}

		boolean resultValid = isValidMarker(result);
		if (!resultValid && !sourceValue.startsWith(":"))
		{
			sourceValue = ":" + sourceValue;
			resultValid = isValidMarker(destination.subSequence(0, destinationStart).toString() + sourceValue +
					destination.subSequence(destinationEnd, destination.length()).toString());
		}
		else if (!sourceValue.isEmpty() && !sourceValue.equals(result))
		{
			String[] segments = result.split(":");
			if(segments.length < 20)
			{
				if (isValidMarker(destination.subSequence(0, destinationStart).toString() + sourceValue + ":" +
						destination.subSequence(destinationEnd, destination.length()).toString()))
				{
					sourceValue = sourceValue + ":";
					resultValid = true;
				}
			}
		}

		if (resultValid && !source.subSequence(sourceStart, sourceEnd).toString().equals(sourceValue))
		{
			return sourceValue;
		}

		if (resultValid)
		{
			return null;
		}
		return "";
	}

	public boolean isValidMarker(String marker)
	{
		String[] values = marker.split(":");
		if (marker.endsWith(":"))
		{
			if (values.length == MAX_REGIONS)
			{
				return false;
			}
			else if (marker.endsWith("::"))
			{
				return false;
			}
		}

		if (values.length > MAX_REGIONS)
		{
			return false;
		}

		int prevValue = 0;
		for (int index = 0; index < values.length; index++)
		{
			String value = values[index];
			try
			{
				int codeValue = Integer.parseInt(value);
				if (codeValue > MAX_VALUE)
				{
					return false;
				}
				else if (codeValue < prevValue)
				{
					if (marker.endsWith(":") || index < values.length - 1)
					{
						return false;
					}
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


	public String getError(Object value)
	{
		if (value == null)
		{
			return "No Code";
		}
		else if (value instanceof String)
		{
			String text = (String) value;
			String error = getMarkerError(text, false);
			if(error != null)
			{
				return error;
			}

			if (experience.getMarker(text) != null && !text.equals(original))
			{
				return "Code Already Exists";
			}
		}
		return null;
	}

	public String getMarkerError(String marker, boolean partial)
	{
		String[] values = marker.split(":");
		if (!partial)
		{
			if (values.length < MIN_REGIONS)
			{
				return "Marker too Short";
			}
		}
		else if (marker.endsWith(":"))
		{
			if (values.length == MAX_REGIONS || marker.endsWith("::"))
			{
				return "Missing Region Value";
			}
		}

		if (values.length > MAX_REGIONS)
		{
			return "Marker too Long";
		}

		int prevValue = 0;
		List<Integer> codes = new ArrayList<>();
		for (int index = 0; index < values.length; index++)
		{
			String value = values[index];
			try
			{
				int codeValue = Integer.parseInt(value);
				if (codeValue > MAX_VALUE)
				{
					return value + " too Large";
				}
				else if (codeValue < prevValue)
				{
					if (!marker.endsWith(":") && index < values.length - 1)
					{
						return value + " is larger than " + prevValue;
					}
				}

				codes.add(codeValue);

				prevValue = codeValue;
			}
			catch (Exception e)
			{
				return value + " is Not a Number";
			}
		}

		return null;
		//return getMarkerError(codes, null);
	}

//	public String getMarkerError(List<Integer> markerCodes, Integer embeddedChecksum)
//	{
//		if (markerCodes == null)
//		{
//			return "No Code";
//		}
//		else if (markerCodes.size() < minRegions)
//		{
//			return "Marker too Short";
//		}
//		else if (markerCodes.size() > maxRegions)
//		{
//			return "Marker too Long";
//		}
//		else if (!hasValidNumberofEmptyRegions(markerCodes))
//		{
//			return "Incorrect Empty Regions";
//		}
//
//		for (Integer value : markerCodes)
//		{
//			//check if leaves are with in accepted range.
//			if (value > maxRegionValue)
//			{
//				return value + " is too Big";
//			}
//		}
//
//		if (embeddedChecksum == null && !hasValidChecksum(markerCodes))
//		{
//			return "Region Total not Divisable by " + checksumModulo;
//		}
//		else if (this.embeddedChecksum && embeddedChecksum != null && !hasValidEmbeddedChecksum(markerCodes, embeddedChecksum))
//		{
//			return "Region Total not Divisable by " + embeddedChecksum.toString();
//		}
//		else if (!this.embeddedChecksum && embeddedChecksum != null)
//		{
//			// Embedded checksum is turned off yet one was provided to this function (this should never happen unless the settings are changed in the middle of detection)
//			return "Embedded checksum markers are not valid.";
//		}
//
//
//		if (!hasValidationRegions(markerCodes))
//		{
//			return validationRegions + " Regions of " + validationRegionValue + " Required";
//		}
//
//		return null;
//	}


	public String getTextString(String postfix, Object value)
	{
		return value.toString();
	}

}
