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

package uk.ac.horizon.aestheticodes.controllers.bindings;

import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import uk.ac.horizon.aestheticodes.controllers.adapters.ValueAdapter;

public class SwitchBinding<T> extends EditorBinding<SwitchCompat, T>
{
	public SwitchBinding(SwitchCompat view, final ValueAdapter<T> adapter)
	{
		super(view, adapter);
		view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				adapter.setValue(controller, isChecked);
			}
		});
	}

	@Override
	public void updateView(Object value)
	{
		if(value == null || value.equals(Boolean.FALSE))
		{
			view.setChecked(false);
		}
		else
		{
			view.setChecked(true);
		}
	}
}
