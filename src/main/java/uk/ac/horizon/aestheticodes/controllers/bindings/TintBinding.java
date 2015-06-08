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


import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.adapters.ValueAdapter;

public class TintBinding<T> extends ViewBinding<View, T>
{
	public TintBinding(View view, ValueAdapter<T> adapter)
	{
		super(view, adapter);
	}

	@Override
	public void updateView(Object value)
	{
		if (value instanceof String && !((String) value).isEmpty())
		{
			Log.i("", "Loading image " + value);
			Ion.with(view.getContext()).load((String) value).withBitmap().asBitmap().setCallback(new FutureCallback<Bitmap>()
			{
				@Override
				public void onCompleted(Exception e, Bitmap result)
				{
					Palette.from(result).generate(new Palette.PaletteAsyncListener()
					{
						public void onGenerated(Palette palette)
						{
							ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{palette.getVibrantColor(view.getContext().getResources().getColor(R.color.apptheme_primary))});
							view.setBackgroundTintList(csl);
						}
					});

				}
			});
		}
	}
}
