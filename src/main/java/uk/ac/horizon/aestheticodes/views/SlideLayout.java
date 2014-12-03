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

package uk.ac.horizon.aestheticodes.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;

public class SlideLayout extends LinearLayout
{
	private Animator animator;
	private boolean visible;
	private Animation inAnimation;
	private Animation outAnimation;

	public SlideLayout(Context context)
	{
		super(context);
	}

	public SlideLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void show()
	{
		setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			animator.start();
		}
		visible = true;
	}

	public void setInAnimation(Animation inAnimation)
	{
		this.inAnimation = inAnimation;
	}

	public void setOutAnimation(Animation outAnimation)
	{
		this.outAnimation = outAnimation;
	}

	@Override
	public void setVisibility(int visibility)
	{
		if (getVisibility() != visibility)
		{
			if (visibility == VISIBLE)
			{
				if (inAnimation != null) startAnimation(inAnimation);
			}
			else if ((visibility == INVISIBLE) || (visibility == GONE))
			{
				if (outAnimation != null) startAnimation(outAnimation);
			}
		}

		super.setVisibility(visibility);
	}

	//		markerSettings.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
//		{
//			@Override
//			public boolean onPreDraw()
//			{
//				markerSettings.getViewTreeObserver().removeOnPreDrawListener(this);
//				markerSettings.setVisibility(View.GONE);
//
//				final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//				final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//				markerSettings.measure(widthSpec, heightSpec);
//
//				animator = slideAnimator(0, markerSettings.getMeasuredHeight());
//				return true;
//			}
//		});

	public void hide()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			int finalHeight = getHeight();
			ValueAnimator animator = slideAnimator(finalHeight, 0);
			animator.addListener(new Animator.AnimatorListener()
			{
				@Override
				public void onAnimationEnd(Animator animator)
				{
					setVisibility(View.GONE);
				}

				@Override
				public void onAnimationStart(Animator animator)
				{
				}

				@Override
				public void onAnimationCancel(Animator animator)
				{
				}

				@Override
				public void onAnimationRepeat(Animator animator)
				{
				}
			});
			animator.start();
		}
		else
		{
			setVisibility(View.GONE);
		}
		visible = false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private ValueAnimator slideAnimator(int start, int end)
	{
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator)
			{
				//Update Height
				int value = (Integer) valueAnimator.getAnimatedValue();

				ViewGroup.LayoutParams layoutParams = getLayoutParams();
				layoutParams.height = value;
				setLayoutParams(layoutParams);
			}
		});
		return animator;
	}
}
