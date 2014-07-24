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

package uk.ac.horizon.aestheticodes.detect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import uk.ac.horizon.aestheticodes.R;

public final class ViewfinderView extends View
{
	private final Paint paint;
	private final int maskColor;

	private CameraManager cameraManager;

	public ViewfinderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// Initialize these once for performance rather than calling them every time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskColor = getResources().getColor(R.color.viewfinder_mask);
	}

	public void setCameraManager(CameraManager cameraManager)
	{
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		if (cameraManager == null)
		{
			return; // not ready yet, early draw before done configuring
		}

		final int width = canvas.getWidth();
		final int height = canvas.getHeight();
		final Rect frame = cameraManager.getFrame(width, height);
		if (frame == null)
		{
			return;
		}

		Bitmap result = cameraManager.getResult();
		if (result != null)
		{
			canvas.drawBitmap(result, null, frame, null);
		}

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
	}
}