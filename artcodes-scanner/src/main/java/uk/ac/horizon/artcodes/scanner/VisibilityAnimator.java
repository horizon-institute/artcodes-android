package uk.ac.horizon.artcodes.scanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;

public class VisibilityAnimator
{
	private final View view;
	private View center;
	private Animator animator;

	public VisibilityAnimator(View view)
	{
		this.view = view;
		this.center = view;
	}

	public VisibilityAnimator(View view, View center)
	{
		this.view = view;
		this.center = center;
	}

	public void hideView()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = ((center.getLeft() + center.getRight()) / 2) - view.getLeft();
			final int cy = ((center.getTop() + center.getBottom()) / 2) - view.getTop();

			Log.i("", "Circle center " + cx + ", " + cy + " width " + view.getWidth());

			animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, view.getWidth(), 0);
			animator.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					if (view != center)
					{
						center.setVisibility(View.VISIBLE);
					}
					view.setVisibility(View.INVISIBLE);
				}
			});
			animator.start();
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
			animator.setDuration(500);
			animator.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					if (view != center)
					{
						center.setVisibility(View.VISIBLE);
					}
					view.setVisibility(View.INVISIBLE);
				}
			});
			animator.start();
		}
		else
		{
			if (view != center)
			{
				center.setVisibility(View.VISIBLE);
			}
			view.setVisibility(View.INVISIBLE);
		}
	}

	public void hideView(Animator.AnimatorListener listener)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = ((center.getLeft() + center.getRight()) / 2) - view.getLeft();
			final int cy = ((center.getTop() + center.getBottom()) / 2) - view.getTop();

			Log.i("", "Circle center " + cx + ", " + cy + " width " + view.getWidth());

			animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, view.getWidth());
			animator.addListener(listener);
			animator.start();
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
			animator.setDuration(500);
			animator.addListener(listener);
			animator.start();
		}
		else
		{
			listener.onAnimationEnd(null);
		}
	}

	public void showView()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = ((center.getLeft() + center.getRight()) / 2) - view.getLeft();
			final int cy = ((center.getTop() + center.getBottom()) / 2) - view.getTop();

			//Log.i("", "Circle center " + cx + ", " + cy + " width " + view.getWidth());

			animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, view.getWidth());
			animator.start();
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
			animator.setDuration(250);
			animator.start();
		}
		if (view != center)
		{
			center.setVisibility(View.INVISIBLE);
		}
		view.setVisibility(View.VISIBLE);
	}
}
