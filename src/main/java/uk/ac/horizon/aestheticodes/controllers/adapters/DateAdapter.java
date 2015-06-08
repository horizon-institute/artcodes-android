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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAdapter<T> extends PropertyAdapter<T>
{
	private final SimpleDateFormat format;

	public DateAdapter(String name)
	{
		super(name);
		format = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
	}

	public DateAdapter(String name, String format)
	{
		super(name);
		this.format = new SimpleDateFormat(format, Locale.getDefault());
	}

	@Override
	protected Object adapt(Object value)
	{
		if(value instanceof Date)
		{
			return format.format((Date)value);
		}
		else if(value instanceof Long)
		{
			return format.format(new Date((Long)value));
		}

		return super.adapt(value);
	}
}
