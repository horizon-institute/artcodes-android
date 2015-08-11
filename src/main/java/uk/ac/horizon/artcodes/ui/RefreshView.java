package uk.ac.horizon.artcodes.ui;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class RefreshView extends SwipeRefreshLayout
{
	private int pending = 0;

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
		setRefreshing(hasPending());
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
		if(pending == 0)
		{
			setRefreshing(false);
		}
	}
}
