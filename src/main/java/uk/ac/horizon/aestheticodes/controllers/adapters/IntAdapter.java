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

package uk.ac.horizon.aestheticodes.controllers.adapters;

import android.view.View;
import android.widget.SeekBar;
import uk.ac.horizon.aestheticodes.controllers.bindings.Binding;
import uk.ac.horizon.aestheticodes.controllers.bindings.SliderBinding;

public class IntAdapter<T> extends PropertyAdapter<T>
{
	private final int min;
	private final int max;

	public IntAdapter(String name, int min, int max)
	{
		super(name);
		this.min = min;
		this.max = max;
	}

	public int getMin()
	{
		return min;
	}

	@Override
	public Binding<T> createBinding(View view)
	{
		if(view instanceof SeekBar)
		{
			return new SliderBinding<>((SeekBar)view, this);
		}
		return super.createBinding(view);
	}

	public int getMax()
	{
		return max;
	}
}
