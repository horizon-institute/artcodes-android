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

package uk.ac.horizon.aestheticodes.bindings;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;

public class PropertyBinding
{
	private static final String TEXT_POSTFIX = "_text";

	protected View view;
	protected Context context;
	protected final String name;
	protected Object object;
	protected Field field;

	PropertyBinding(Object object, String name)
	{
		this.object = object;
		this.name = name;
	}

	PropertyBinding(String name)
	{
		this.name = name;
	}

	public void setView(View view)
	{
		this.view = view;
	}

	public void setContext(Context context)
	{
		this.context = context;
	}

	protected void updateView()
	{
		if (view instanceof TextView)
		{
			final String text = getText();
			if (text != null)
			{
				((TextView) view).setText(text);
			}
		}
		else if (view instanceof ImageView)
		{
			final Object value = get();
			if (value instanceof String)
			{
				Picasso.with(context).load(value.toString()).into((ImageView) view);
			}
		}
	}

	public boolean isValid()
	{
		return true;
	}

	public void setObject(Object object)
	{
		this.object = object;
	}

	public String getText()
	{
		Object value = get();
		if (value == null)
		{
			return null;
		}

		String text = getTextString(name + TEXT_POSTFIX, value);
		if (text == null)
		{
			return value.toString();
		}
		return text;
	}

	public String getTextString(String name, Object value)
	{
		int resource = context.getResources().getIdentifier(name, "string", context.getPackageName());
		if (resource != 0)
		{
			return context.getString(resource, value);
		}
		return null;
	}

	public String getName()
	{
		return name;
	}

	public Object get()
	{
		try
		{
			if (field == null)
			{
				field = object.getClass().getDeclaredField(name);
				field.setAccessible(true);
			}
			return field.get(object);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void set(Object value)
	{
		try
		{
			field.set(object, value);
			updateView();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	public Object getProperty(String name)
	{
		try
		{
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(object);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void setProperty(String name, Object value)
	{
		try
		{
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(object, value);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	public Context getContext()
	{
		return context;
	}
}
