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

public abstract class PropertySettingsItem extends SettingsItem
{
	final Property property;

	PropertySettingsItem(SettingsActivity activity, Object settings, String property)
	{
		super(activity);
		this.property = new Property(settings, property);
	}

	String getDescription()
	{
		String propertyName = property.getName();
		int resource = activity.getResources().getIdentifier(propertyName + "_desc", "string", activity.getPackageName());
		if(resource == 0)
		{
			return null;
		}
		else
		{
			return activity.getString(resource);
		}
	}

	public String getTitle()
	{
		String propertyName = property.getName();
		int resource = activity.getResources().getIdentifier(propertyName, "string", activity.getPackageName());
		if(resource == 0)
		{
			return propertyName;
		}
		else
		{
			return activity.getString(resource);
		}
	}

	@Override
	public String getDetail()
	{
		String propertyName = property.getName();
		int resource = activity.getResources().getIdentifier(propertyName + "_value", "string", activity.getPackageName());
		if(resource == 0)
		{
			return property.getString();
		}
		else
		{
			return activity.getString(resource, property.get());
		}
	}

	@Override
	public Type getType()
	{
		return Type.two_line;
	}
}
