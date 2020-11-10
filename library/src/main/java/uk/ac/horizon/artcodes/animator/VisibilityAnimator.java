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

package uk.ac.horizon.artcodes.animator;

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
	private final View center;
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

	public void setViewVisible(boolean visible)
	{
		if (visible)
		{
			view.setVisibility(View.VISIBLE);
		}
		else
		{
			view.setVisibility(View.GONE);
		}
	}

	public void hideView()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = ((center.getLeft() + center.getRight()) / 2) - view.getLeft();
			final int cy = ((center.getTop() + center.getBottom()) / 2) - view.getTop();

			Log.i("circleanim", "Circle center " + cx + ", " + cy + " width " + view.getWidth());

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
		else
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
	}

	public void showView()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int cx = ((center.getLeft() + center.getRight()) / 2) - view.getLeft();
			final int cy = ((center.getTop() + center.getBottom()) / 2) - view.getTop();

			//Log.i("circleanim", "Circle center " + cx + ", " + cy + " width " + view.getWidth());

			animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, view.getWidth());
			animator.start();
		}
		else
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
