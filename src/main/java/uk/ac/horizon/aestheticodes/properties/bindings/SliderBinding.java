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

import android.view.View;
import android.widget.SeekBar;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.IntFormat;
import uk.ac.horizon.aestheticodes.properties.Property;

public class SliderBinding extends ViewBinding
{
	public SliderBinding(View view)
	{
		super(view);
	}

	private int min;
	private IntFormat intFormat;

	@Override
	public void update(Object value, Format format)
	{
		if(view instanceof SeekBar)
		{
			SeekBar slider = (SeekBar) view;
			if(format instanceof IntFormat)
			{
				intFormat = (IntFormat)format;
				int max = intFormat.getMax();
				min = intFormat.getMin();
				slider.setMax(max - min);
				slider.setProgress(intFormat.getIntValue(value) - min);
			}
		}
	}

	@Override
	public boolean init(final Property property)
	{
		super.init(property);

		if(view instanceof SeekBar)
		{
			SeekBar slider = (SeekBar)view;
			slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
					property.set(intFormat.getSaveValue(progress + min));
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
			return true;
		}
		return false;
	}
}
