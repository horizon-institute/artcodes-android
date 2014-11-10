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

package uk.ac.horizon.aestheticodes.adapters;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ModeSelectTransformer implements ViewPager.PageTransformer
{
	private static final float MIN_SCALE = 0.85f;
	private static final float MIN_ALPHA = 0.3f;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void transformPage(View view, float position)
	{
		if (position < -1)
		{
			view.setAlpha(0);
		}
		else if (position <= 1)
		{
			float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
			view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
		}
		else
		{
			view.setAlpha(0);
		}
	}
}