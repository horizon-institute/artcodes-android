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

package uk.ac.horizon.aestheticodes.properties;

import android.content.Context;
import android.text.InputFilter;

public class Format
{
	private static final String TEXT_POSTFIX = "text";

	protected String name;
	protected Context context;
	private String defaultValue;

	public String getError(Object value)
	{
		return null;
	}

	void setContext(Context context)
	{
		this.context = context;
	}

	void setName(String name)
	{
		this.name = name;
	}

	public String getDisplayString(Object value)
	{
		if (value == null)
		{
			if(defaultValue != null)
			{
				return defaultValue;
			}
			return null;
		}

		String text = getTextString(TEXT_POSTFIX, value);
		if (text == null)
		{
			return value.toString();
		}
		return text;
	}

	public Object getSaveValue(Object value)
	{
		if(value == null || (value instanceof String && ((String)value).isEmpty()))
		{
			return null;
		}
		else
		{
			return value;
		}
	}

	public String getEditString(Object value)
	{
		return getDisplayString(value);
	}

	public String getTextString(String postfix, Object value)
	{
		String stringName = name;
		if(postfix != null)
		{
			stringName = name + "_" + postfix;
		}
		int resource = context.getResources().getIdentifier(stringName, "string", context.getPackageName());
		if (resource != 0)
		{
			return context.getString(resource, value);
		}
		return null;
	}

	void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}
}
