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

package uk.ac.horizon.aestheticodes.properties.bindings;

import android.util.Log;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Property;

public class PropertyBinding extends ViewBinding
{
	public PropertyBinding(String property)
	{
		super(null);
		this.view = property;
	}

	@Override
	public boolean init(Property property)
	{
		if (view instanceof String)
		{
			view = property.getProperties().get((String) view);
		}
		return true;
	}

	@Override
	public void set(Property property)
	{
		if(view instanceof Property)
		{
			Property aproperty = (Property)view;
			aproperty.save();
		}
	}

	@Override
	public void update(Object value, Format format)
	{
		if(view instanceof Property)
		{
			Property property = (Property)view;
			property.formatAs(format);
			property.set(value);
		}
	}
}
