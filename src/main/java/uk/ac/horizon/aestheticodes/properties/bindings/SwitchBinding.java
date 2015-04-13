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

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Property;

public class SwitchBinding extends ViewBinding
{
	public SwitchBinding(View view)
	{
		super(view);
	}

	@Override
	public boolean init(final Property property)
	{
		super.init(property);

		if(view instanceof SwitchCompat)
		{
			((SwitchCompat)view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					property.set(isChecked);
				}
			});
			return true;
		}
		return false;
	}

	@Override
	public void update(Object value, Format format)
	{
		if(view instanceof SwitchCompat)
		{
			if (value.equals(Boolean.TRUE))
			{
				((SwitchCompat)view).setChecked(true);
			}
			else if(value.equals(Boolean.FALSE))
			{
				((SwitchCompat)view).setChecked(false);
			}
		}
	}
}
