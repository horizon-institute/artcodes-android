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

import android.widget.SeekBar;
import uk.ac.horizon.aestheticodes.controllers.adapters.IntAdapter;

public class SliderBinding<T> extends EditorBinding<SeekBar, T>
{
	private final int min;

	public SliderBinding(SeekBar view, final IntAdapter<T> adapter)
	{
		super(view, adapter);
		this.min = adapter.getMin();
		int max = adapter.getMax();
		view.setProgress(max - min);
		view.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				adapter.setValue(controller, progress + min);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});
	}

//	@Override
//	public void disconnect()
//	{
//		view.setOnSeekBarChangeListener(null);
//	}

	public Integer getIntValue(Object object)
	{
		if (object instanceof Integer)
		{
			return (Integer) object;
		}
		return null;
	}

	@Override
	public void updateView(Object value)
	{
		if(value != null)
		{
			view.setProgress(getIntValue(value) - min);
		}
	}
}
