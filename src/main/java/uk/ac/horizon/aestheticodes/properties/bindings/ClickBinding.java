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

package uk.ac.horizon.aestheticodes.properties.bindings;

import android.view.View;
import android.view.ViewParent;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Property;

public abstract class ClickBinding extends ViewBinding implements View.OnClickListener
{
	private boolean parent = false;

	public ClickBinding(int view)
	{
		super(view);
	}

	public ClickBinding(int view, boolean parent)
	{
		super(view);
		this.parent = parent;
	}

	@Override
	public boolean init(Property property)
	{
		super.init(property);

		if(view instanceof View)
		{
			if(parent)
			{
				ViewParent parent = ((View) view).getParent();
				if (parent instanceof View)
				{
					((View) parent).setOnClickListener(ClickBinding.this);
					return true;
				}
			}
			else
			{
				((View)view).setOnClickListener(ClickBinding.this);
			}
		}
		return false;
	}

	@Override
	public void update(Object value, Format format)
	{

	}
}
