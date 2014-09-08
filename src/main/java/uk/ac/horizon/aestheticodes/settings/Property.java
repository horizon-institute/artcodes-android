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

package uk.ac.horizon.aestheticodes.settings;

import android.util.Log;

import java.lang.reflect.Method;

public class Property
{
	private final String name;
	private final Object object;
	private Method getMethod;
	private Method setMethod;

	public Property(Object object, String name)
	{
		this.name = name;
		this.object = object;
	}

	public Object get()
	{
		if (getMethod == null)
		{
			String propertyName = name.substring(1);
			for (Method method : object.getClass().getMethods())
			{
				if (method.getName().endsWith(propertyName) && method.getParameterTypes().length == 0 && !method.getReturnType().equals(Void.TYPE))
				{
					getMethod = method;
					break;
				}
			}
		}

		if (getMethod != null)
		{
			try
			{
				return getMethod.invoke(object);
			}
			catch (Exception e)
			{
				Log.w(Property.class.getName(), e.getMessage(), e);
			}
		}
		return null;
	}

	public String getString()
	{
		Object value = get();
		if (value != null)
		{
			return value.toString();
		}
		return null;
	}

	public void set(Object value)
	{
		if (setMethod == null)
		{
			String propertyName = name.substring(1);
			for (Method method : object.getClass().getMethods())
			{
				if (method.getName().endsWith(propertyName) && method.getParameterTypes().length == 1 && method.getReturnType().equals(Void.TYPE))
				{
					setMethod = method;
					break;
				}
			}
		}

		if (setMethod != null)
		{
			try
			{
				setMethod.invoke(object, value);
			}
			catch (Exception e)
			{
				Log.w(Property.class.getName(), e.getMessage(), e);
			}
		}
	}

	public String getName()
	{
		return name;
	}
}
