/*
 * Artcodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.artcodes.ui;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class RefreshView extends SwipeRefreshLayout
{
	private int pending = 0;
	private boolean measured = false;

	public RefreshView(Context context)
	{
		super(context);
	}

	public RefreshView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!measured)
		{
			measured = true;
			setRefreshing(hasPending());
		}
	}

	@Override
	public void setRefreshing(boolean refreshing)
	{
		if (measured)
		{
			super.setRefreshing(refreshing);
		}
	}

	public boolean hasPending()
	{
		return pending > 0;
	}

	public void addPending()
	{
		pending++;
		setRefreshing(true);
	}

	public void removePending()
	{
		pending--;
		if (pending == 0)
		{
			setRefreshing(false);
		}
	}
}
