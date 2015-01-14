/*
 * uk.ac.horizon.aestheticodes.Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  uk.ac.horizon.aestheticodes.Aestheticodes
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
import uk.ac.horizon.aestheticodes.properties.Format;

public class VisibilityBinding extends ViewBinding
{
	private final Object offValue;

	public VisibilityBinding(int viewID)
	{
		super(viewID);
		this.offValue = Boolean.FALSE;
	}

	public VisibilityBinding(int viewID, Object offValue)
	{
		super(viewID);
		this.offValue = offValue;
	}

	@Override
	public void update(Object value, Format format)
	{
		if(view instanceof View)
		{
			if (value == offValue)
			{
				((View)view).setVisibility(View.GONE);
			}
			else if (value != null && value.equals(offValue))
			{
				((View)view).setVisibility(View.GONE);
			}
			else
			{
				((View)view).setVisibility(View.VISIBLE);
			}
		}
	}
}
