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

package uk.ac.horizon.aestheticodes.properties;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerFormat extends Format implements InputFilter
{
	private final Experience experience;
	private final String original;

	public MarkerFormat(Experience experience, String original)
	{
		this.experience = experience;
		this.original = original;
	}

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

		boolean resultValid = experience.isValidMarker(result, true);
		if (!resultValid && !sourceValue.startsWith(":"))
		{
			sourceValue = ":" + sourceValue;
			resultValid = experience.isValidMarker(destination.subSequence(0, destinationStart).toString() + sourceValue +
					destination.subSequence(destinationEnd, destination.length()).toString(), true);
		}
		else if (!sourceValue.isEmpty() && !sourceValue.equals(result))
		{
			String[] segments = result.split(":");
			if(segments.length < experience.getMinRegions())
			{
				if (experience.isValidMarker(destination.subSequence(0, destinationStart).toString() + sourceValue + ":" +
						destination.subSequence(destinationEnd, destination.length()).toString(), true))
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

	@Override
	public String getError(Object value)
	{
		if (value == null)
		{
			return "No Code";
		}
		else if (value instanceof String)
		{
			String text = (String) value;
			String error = experience.getMarkerError(text, false);
			if(error != null)
			{
				return error;
			}

			if (experience.getMarkers().containsKey(text) && !text.equals(original))
			{
				return "Code Already Exists";
			}
		}
		return null;
	}

	@Override
	public String getTextString(String postfix, Object value)
	{
		return value.toString();
	}
}
