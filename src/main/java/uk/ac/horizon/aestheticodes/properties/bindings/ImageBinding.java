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
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import uk.ac.horizon.aestheticodes.properties.Format;

public class ImageBinding extends ViewBinding
{
	public ImageBinding(View view)
	{
		super(view);
	}

	@Override
	public void update(Object value, Format format)
	{
		if (view instanceof ImageView && value instanceof String)
		{
			Picasso.with(context).load((String)value).into((ImageView)view);
		}
	}
}
