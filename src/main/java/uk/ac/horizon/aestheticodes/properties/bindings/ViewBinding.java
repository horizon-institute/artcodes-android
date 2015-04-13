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

import android.content.Context;
import android.view.View;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Property;

public abstract class ViewBinding
{
	protected Context context;
	protected Object view;

	public ViewBinding(int view)
	{
		this.view = view;
	}

	public ViewBinding(View view)
	{
		this.view = view;
	}

	public boolean init(Property property)
	{
		if(view instanceof Integer)
		{
			view = property.getProperties().findView((Integer)view);
		}

		context = property.getProperties().getContext();

		return view != null;
	}

	public void set(Property property)
	{

	}

	public void setError(String error)
	{

	}

	public boolean hasViewID(int viewID)
	{
		if(view instanceof Integer)
		{
			return ((Integer)view) == viewID;
		}
		else if(view instanceof View)
		{
			return ((View)view).getId() == viewID;
		}
		return false;
	}

	public abstract void update(Object value, Format format);
}
