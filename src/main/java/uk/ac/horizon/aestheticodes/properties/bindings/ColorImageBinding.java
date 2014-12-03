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

package uk.ac.horizon.aestheticodes.properties.bindings;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.properties.Format;

public class ColorImageBinding extends ViewBinding
{
	private class FABColorTarget implements Target
	{
		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
		{
			if(view instanceof ImageView)
			{
				final ImageView imageView = (ImageView)view;
				// TODO Animate?
				imageView.setImageBitmap(bitmap);
			}
			Log.i(FABColorTarget.class.getName(), "View = " + view);
			Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener()
			{
				public void onGenerated(Palette palette)
				{
					if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context instanceof Activity)
					{
						((Activity)context).getWindow().setStatusBarColor(palette.getDarkVibrantColor(context.getResources().getColor(R.color.apptheme_primary_dark)));
					}

					if (colorView != null)
					{
						colorView.getBackground().setColorFilter(new LightingColorFilter(Color.BLACK, palette.getDarkVibrantColor(context.getResources().getColor(R.color.apptheme_primary))));
					}
				}
			});
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable)
		{

		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable)
		{

		}
	}

	private View colorView;

	public ColorImageBinding(int viewID, int colorViewID)
	{
		super(viewID);
	}

	@Override
	public void update(Object value, Format format)
	{
		if (value instanceof String)
		{
			Picasso.with(context).load((String)value).into(new FABColorTarget());
		}
	}
}
