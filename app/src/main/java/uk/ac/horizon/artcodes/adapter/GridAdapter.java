/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.adapter;

import android.content.Context;
import androidx.databinding.ViewDataBinding;
import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import uk.ac.horizon.artcodes.R;

public abstract class GridAdapter<T extends ViewDataBinding> extends ListAdapter<T>
{
	protected final GridLayoutManager layoutManager;
	private boolean fabPadding = false;

	public GridAdapter(final Context context)
	{
		super(context);
		layoutManager = new GridLayoutManager(context, 2)
		{
			private final int columnWidth = context.getResources().getDimensionPixelSize(R.dimen.columnWidth);
			private boolean columnWidthChanged = true;

			@Override
			public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
			{
				if (columnWidthChanged && columnWidth > 0)
				{
					int totalSpace;
					if (getOrientation() == VERTICAL)
					{
						totalSpace = getWidth() - getPaddingRight() - getPaddingLeft();
					}
					else
					{
						totalSpace = getHeight() - getPaddingTop() - getPaddingBottom();
					}
					int spanCount = Math.max(1, totalSpace / columnWidth);
					setSpanCount(spanCount);
					columnsChanged();
					columnWidthChanged = false;
				}
				super.onLayoutChildren(recycler, state);
			}
		};
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
		{
			@Override
			public int getSpanSize(int position)
			{
				return getSpan(position);
			}
		});
	}

	@Override
	public RecyclerView.LayoutManager getLayoutManager()
	{
		return layoutManager;
	}

	protected int getSpan(int position)
	{
		return 1;
	}

	public void enableFABPadding()
	{
		this.fabPadding = true;
	}

	protected void columnsChanged()
	{

	}

	@Override
	public RecyclerView.ItemDecoration getDecoration()
	{
		return new RecyclerView.ItemDecoration()
		{
			final int spacing = context.getResources().getDimensionPixelSize(R.dimen.padding);
			final int fabSpacing = context.getResources().getDimensionPixelSize(R.dimen.fabPad);

			@Override
			public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
			{
				if (parent.getPaddingLeft() != spacing)
				{
					final int bottomSpacing = fabPadding ? fabSpacing : spacing;
					parent.setPadding(spacing, 0, 0, bottomSpacing);
					parent.setClipToPadding(false);
				}

				outRect.top = spacing;
				//outRect.bottom = halfSpace;
				//outRect.left = halfSpace;
				outRect.right = spacing;
			}
		};
	}
}
