/*
 * Aestheticodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.aestheticodes.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import uk.ac.horizon.aestheticodes.core.R;
import uk.ac.horizon.aestheticodes.controllers.CameraController;

import java.util.HashSet;
import java.util.Set;

public final class ViewfinderView extends View
{
	public interface SizeChangedListener
	{
		public void sizeHasChanged();
	}

	private final Paint paint;
	private final int maskColor;
	private final Set<SizeChangedListener> sizeChangeListeners = new HashSet<>();
	private CameraController camera;
	private Bitmap result;
	private Rect frame;

	public ViewfinderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// Initialize these once for performance rather than calling them every time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskColor = getResources().getColor(R.color.viewfinder_mask);
	}

	public void addSizeChangedListener(SizeChangedListener listener)
	{
		sizeChangeListeners.add(listener);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		final int width = this.getWidth();
		final int height = this.getHeight();

		frame = null;
		if (camera != null)
		{
			frame = camera.getFrame(width, height);
		}

		if (frame == null)
		{
			frame = new Rect();
		}

		// Draw the exterior (i.e. outside the framing rect) darkened: top, left, right & bottom rectangles.
		paint.setColor(maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		// Bottom handled by view
		// canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (result != null)
		{
			canvas.drawBitmap(result, null, frame, null);
		}
	}

	public void setResult(Bitmap result)
	{
		if(result == null && this.result == null)
		{
			return;
		}
		this.result = result;
		if(frame == null)
		{
			invalidate();
		}
		else
		{
			invalidate(frame);
		}
	}

	public boolean removeSizeChangedListener(SizeChangedListener listener)
	{
		return sizeChangeListeners.remove(listener);
	}

	public void setCamera(CameraController camera)
	{
		this.camera = camera;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		for (SizeChangedListener listener : sizeChangeListeners)
		{
			listener.sizeHasChanged();
		}
	}
}
