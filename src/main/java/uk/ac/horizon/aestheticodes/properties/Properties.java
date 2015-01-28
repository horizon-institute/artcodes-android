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

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class Properties
{
	private final Context context;
	private final Object object;
	private final View root;
	private final Map<String, Property> properties = new HashMap<String, Property>();

	public Properties(Context context, Object object)
	{
		this.context = context;
		this.object = object;
		this.root = null;
	}

	public Properties(Context context, Object object, View root)
	{
		this.context = context;
		this.object = object;
		this.root = root;
	}

	public Context getContext()
	{
		return context;
	}

	public Property get(String name)
	{
		if(properties.containsKey(name))
		{
			return properties.get(name);
		}

		Property property = new Property(this, name);
		properties.put(name, property);

		return property;
	}

	public boolean isValid()
	{
		for(Property property: properties.values())
		{
			if(!property.isValid())
			{
				return false;
			}
		}
		return true;
	}

	public void valid(boolean valid)
	{

	}

	public void load()
	{
		for(Property property:properties.values())
		{
			property.load();
		}
	}

	public Object save()
	{
		for(Property property: properties.values())
		{
			property.save();
		}
		return object;
	}

	public View findView(int viewID)
	{
		if(root != null)
		{
			return root.findViewById(viewID);
		}
		else if(context instanceof Activity)
		{
			Activity activity = (Activity)context;
			return activity.findViewById(viewID);
		}
		return null;
	}

	Object getObject()
	{
		return object;
	}
}
