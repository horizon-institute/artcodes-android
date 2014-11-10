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

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ViewBindings
{
	private final Context context;
	private final Object object;
	private View root;
	private final List<PropertyBinding> bindings = new ArrayList<PropertyBinding>();
	private final TextWatcher watcher = new TextWatcher()
	{
		private final Handler handler = new Handler();

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
		{

		}

		final Runnable userStoppedTyping = new Runnable()
		{
			@Override
			public void run()
			{
				boolean valid = true;
				for (PropertyBinding binding : bindings)
				{
					if (!binding.isValid())
					{
						valid = false;
					}
				}

				setValid(valid);
			}
		};

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
		{
			for (PropertyBinding binding : bindings)
			{
				//binding.field.setError(null);
			}
			handler.removeCallbacksAndMessages(null);
			handler.postDelayed(userStoppedTyping, 2000);
		}

		@Override
		public void afterTextChanged(Editable editable)
		{
		}
	};

	public ViewBindings(Context context, Object object)
	{
		this.context = context;
		this.object = object;
	}

	public void setRoot(View view)
	{

	}

	public View bind(int viewID, String name)
	{
		return bind(viewID, new PropertyBinding(name));
	}

	public View bind(int viewID, PropertyBinding property)
	{
		if(root != null)
		{
			return bind(object, root.findViewById(viewID), property);
		}
		else if(context instanceof Activity)
		{
			Activity activity = (Activity)context;
			return bind(object, activity.findViewById(viewID), property);
		}
		return null;
	}

	protected void setValid(boolean valid)
	{

	}

	public boolean isValid()
	{
		for(PropertyBinding binding: bindings)
		{
			if(!binding.isValid())
			{
				return false;
			}
		}
		return true;
	}

	private View bind(Object object, View view, PropertyBinding property)
	{
		if (view != null)
		{
			property.setObject(object);
			property.setView(view);
			property.setContext(context);
			property.updateView();
			bindings.add(property);
		}
		return view;
	}

	public View bind(View root, int viewID, Object object, String propertyName)
	{
		if(root != null)
		{
			return bind(object, root.findViewById(viewID), new PropertyBinding(propertyName));
		}
		return null;
	}

	public View bind(View root, int viewID, PropertyBinding property)
	{
		if(root != null)
		{
			return bind(object, root.findViewById(viewID), property);
		}
		return null;
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

			for(PropertyBinding binding: bindings)
			{
				if(binding.getName().equals(name))
				{
					binding.updateView();
				}
			}
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

	public void bind(PropertyBinding binding)
	{
		bindings.add(binding);
	}
}
