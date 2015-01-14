/*
 * uk.ac.horizon.aestheticodes.Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  uk.ac.horizon.aestheticodes.Aestheticodes
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

import android.util.Patterns;

public class URLFormat extends Format
{
	private static final String HTTP = "http://";
	private boolean required = false;

	@Override
	public Object getSaveValue(Object value)
	{
		if(value == null)
		{
			return null;
		}
		else if(value instanceof String)
		{
			String text = (String)value;
			if(text.isEmpty())
			{
				return null;
			}
			else if(!text.contains("://"))
			{
				return HTTP + text;
			}
		}

		return value;
	}

	@Override
	public String getDisplayString(Object value)
	{
		String text = super.getDisplayString(value);
		if(text != null && text.startsWith(HTTP))
		{
			return text.substring(HTTP.length());
		}
		return text;
	}

	@Override
	public String getError(Object value)
	{
		if(value instanceof String)
		{
			String text = (String)getSaveValue((String)value);
			if(text == null)
			{
				if(required)
				{
					return "Required";
				}
			}
			else if (!Patterns.WEB_URL.matcher(text).matches())
			{
				return "Invalid URL";
			}
		}
		if(required && value == null)
		{
			return "Required";
		}
		return null;
	}
}
