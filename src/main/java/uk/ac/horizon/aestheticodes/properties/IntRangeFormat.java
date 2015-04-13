/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
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

import android.util.Log;

public class IntRangeFormat extends IntFormat
{
	private Property minProperty;
	private Property maxProperty;

	public IntRangeFormat(Property minProperty, Property maxProperty, Object min, Object max)
	{
		super(min, max);
		this.minProperty = minProperty;
		this.maxProperty = maxProperty;
	}

	@Override
	public Object getSaveValue(Object value)
	{
		if(value == null)
		{
			return null;
		}
		else if(value instanceof Integer)
		{
			int intValue = (Integer)value;
			int minValue = getIntValue(minProperty);
			int maxValue = getIntValue(maxProperty);
			Log.i("", "Setting value " + intValue + " of " + minValue + "-" + maxValue);
			if(intValue < minValue)
			{
				minProperty.set(intValue);
			}
			if(intValue > maxValue)
			{
				maxProperty.set(intValue);
			}
		}

		return value;
	}

	@Override
	public String getDisplayString(Object value)
	{
		int minValue = getIntValue(minProperty);
		int maxValue = getIntValue(maxProperty);
		if(minValue == maxValue)
		{
			int resource = context.getResources().getIdentifier(name + "_text", "plurals", context.getPackageName());
			if(resource == 0)
			{
				resource = context.getResources().getIdentifier(name + "_text", "string", context.getPackageName());
				if(resource == 0)
				{
					return Integer.toString(minValue);
				}
				else
				{
					return context.getString(resource, minValue);
				}

			}
			else
			{
				return context.getResources().getQuantityString(resource, minValue, Integer.toString(minValue));
			}
		}
		else
		{
			String valueString = minValue + "-" + maxValue;
			int resource = context.getResources().getIdentifier(name + "_text", "plurals", context.getPackageName());
			if(resource == 0)
			{
				resource = context.getResources().getIdentifier(name + "_text", "string", context.getPackageName());
				if(resource == 0)
				{
					return valueString;
				}
				else
				{
					return context.getString(resource, valueString);
				}
			}
			else
			{
				return context.getResources().getQuantityString(resource, maxValue, valueString);
			}
		}
	}
}
