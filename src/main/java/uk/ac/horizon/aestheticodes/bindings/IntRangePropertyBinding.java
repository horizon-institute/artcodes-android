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
import android.view.ViewParent;

public class IntRangePropertyBinding extends PropertyBinding
{
	private String maxName;
	private Object min;
	private Object max;

	public IntRangePropertyBinding(String name, String maxName, Object min, Object max)
	{
		super(name);
		this.maxName = maxName;
		this.min = min;
		this.max = max;
	}

	public Integer getIntValue(Object object)
	{
		if (object instanceof Integer)
		{
			return (Integer) object;
		}
		else if (object instanceof String)
		{
			//Object value = binding.get//
		}
		return null;
	}

	@Override
	public String getText()
	{
		Object value = get();
		if(value instanceof Integer)
		{
			int resource = context.getResources().getIdentifier(getName() + "_text", "plurals", context.getPackageName());
			if (resource != 0)
			{
				return context.getResources().getQuantityString(resource, (Integer) value, value);
			}
		}
		return super.getText();
	}

	@Override
	protected void updateView()
	{
		super.updateView();

		ViewParent parent = view.getParent();
		if(parent instanceof View)
		{

		}
	}
}
