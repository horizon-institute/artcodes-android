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

import android.util.Log;
import uk.ac.horizon.aestheticodes.controllers.Controller;

import java.lang.reflect.Field;
import java.util.List;

public class PropertyAdapter<T> extends ValueAdapter<T>
{
	private final String name;
	private Field field;

	public PropertyAdapter(String name)
	{
		this.name = name;
	}

	protected Object adapt(Object value)
	{
		return value;
	}

	@Override
	public final Object getValue(T object)
	{
		try
		{
			Field field = getField(object);
			if(field != null)
			{
				return adapt(field.get(object));
			}
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
		return null;
	}

	public void setValue(Controller<T> controller, Object value)
	{
		try
		{
			Field field = getField(controller.getModel());
			if(field != null)
			{
				Object existing = field.get(controller.getModel());
				if((value == null && existing != null) || (value != null && !value.equals(existing)))
				{
					field.set(controller.getModel(), value);
					controller.notifyChanges(name);
				}
			}
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
	}

	@Override
	public boolean shouldUpdate(List<String> properties)
	{
		return properties.contains(name);
	}

	private Field getField(T object)
	{
		if(object == null)
		{
			return null;
		}
		if (field == null)
		{
			try
			{
				field = object.getClass().getDeclaredField(name);
				field.setAccessible(true);
			}
			catch (NoSuchFieldException e)
			{
				Log.i("", e.getMessage(), e);
			}
		}
		return field;
	}
}
