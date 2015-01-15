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
import uk.ac.horizon.aestheticodes.properties.Property;

public class ColorImageBinding extends ViewBinding implements Target
{
	private Object colorView;

	public ColorImageBinding(int viewID, int colorViewID)
	{
		super(viewID);
		this.colorView = colorViewID;
	}

	@Override
	public boolean init(Property property)
	{
		super.init(property);
		if (colorView instanceof Integer)
		{
			colorView = property.getProperties().findView((Integer) colorView);
		}
		return view != null && colorView != null;
	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable)
	{

	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
	{
		Log.i("", "Image received");
		if (view instanceof ImageView)
		{
			Log.i("", "Setting view");
			final ImageView imageView = (ImageView) view;
			// TODO Animate?
			imageView.setImageBitmap(bitmap);
		}
		Log.i("", "View = " + view);
		Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener()
		{
			public void onGenerated(Palette palette)
			{
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context instanceof Activity)
				{
					((Activity) context).getWindow().setStatusBarColor(palette.getDarkVibrantColor(context.getResources().getColor(R.color.apptheme_primary_dark)));
				}

				if (colorView instanceof View)
				{
					((View) colorView).getBackground().setColorFilter(new LightingColorFilter(Color.BLACK, palette.getVibrantColor(context.getResources().getColor(R.color.apptheme_primary))));
				}
			}
		});
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable)
	{

	}

	@Override
	public void update(Object value, Format format)
	{
		if (value instanceof String && !((String) value).isEmpty())
		{
			Picasso.with(context).cancelRequest(this);
			Picasso.with(context).load((String) value).into(this);
		}
	}
}
