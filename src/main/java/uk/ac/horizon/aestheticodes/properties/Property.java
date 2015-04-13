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
import android.view.View;
import uk.ac.horizon.aestheticodes.properties.bindings.ViewBinding;
import uk.ac.horizon.aestheticodes.properties.bindings.ViewBindingFactory;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class Property
{
	private final String name;
	private final List<ViewBinding> bindings = new ArrayList<>();
	private final Properties properties;
	private Field field;
	private Method get;
	private Object value;
	private Format format;

	public Property(Properties bindings, String name)
	{
		this.properties = bindings;
		this.name = name;
		formatAs(new Format());
	}

	public String getName()
	{
		return name;
	}

	public Property bindTo(View view)
	{
		if (view != null)
		{
			final ViewBinding viewBinding = ViewBindingFactory.createBinding(view);
			if(viewBinding != null && viewBinding.init(this))
			{
				bindings.add(viewBinding);
			}
		}
		return this;
	}

	public Property bindTo(int viewID)
	{
		bindTo(properties.findView(viewID));
		return this;
	}

	public Property formatAs(Format format)
	{
		this.format = format;
		format.setContext(properties.getContext());
		format.setName(name);
		return this;
	}

	public Property defaultTo(String defaultValue)
	{
		format.setDefaultValue(defaultValue);
		return this;
	}

	public Property bindTo(ViewBinding viewBinding)
	{
		if (viewBinding.init(this))
		{
			bindings.add(viewBinding);
		}
		return this;
	}

	public boolean isValid()
	{
		return format.getError(value) == null;
	}

	private String getMethodName()
	{
		return "get" + name.substring(0,1).toUpperCase() + name.substring(1);
	}

	public void load()
	{
		try
		{
			if (field == null && get == null)
			{
				try
				{
					field = properties.getObject().getClass().getDeclaredField(name);
					field.setAccessible(true);
				}
				catch(NoSuchFieldException e)
				{
					get = properties.getObject().getClass().getDeclaredMethod(getMethodName());
					get.setAccessible(true);
				}
			}

			if(field != null)
			{
				set(field.get(properties.getObject()));
			}
			else if(get != null)
			{
				set(get.invoke(properties.getObject()));
			}
			refresh();
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
		{
			Log.e("", e.getMessage(), e);
		}
	}

	public void save()
	{
		for(ViewBinding binding: bindings)
		{
			binding.set(this);
		}
		try
		{
			field.set(properties.getObject(), value);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	public Object get()
	{
		if (field == null && get == null)
		{
			load();
		}
		return value;
	}

	public void set(Object value)
	{
		String error = format.getError(value);
		if(error == null)
		{
			if(value == null || !value.equals(this.value))
			{
				this.value = value;
				refresh();
			}
		}
		else
		{
			for (ViewBinding binding : bindings)
			{
				binding.setError(error);
			}
		}
	}

	private void refresh()
	{
		for (ViewBinding binding : bindings)
		{
			binding.update(get(), format);
		}
	}

	public void unbind(int viewID)
	{
		final List<ViewBinding> removals = new ArrayList<>();
		for(ViewBinding binding: bindings)
		{
			if(binding.hasViewID(viewID))
			{
				removals.add(binding);
			}
		}

		bindings.removeAll(removals);
	}

	public Properties getProperties()
	{
		return properties;
	}

	public Format getFormat()
	{
		return format;
	}
}
