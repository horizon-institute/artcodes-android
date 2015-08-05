package uk.ac.horizon.artcodes.ui;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class RefreshView extends SwipeRefreshLayout
{
	private boolean measured = false;
	private boolean mPreMeasureRefreshing = false;

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
			setRefreshing(mPreMeasureRefreshing);
		}
	}


	@Override
	public void setRefreshing(boolean refreshing)
	{
		if (measured)
		{
			super.setRefreshing(refreshing);
		}
		else
		{
			mPreMeasureRefreshing = refreshing;
		}
	}
}
