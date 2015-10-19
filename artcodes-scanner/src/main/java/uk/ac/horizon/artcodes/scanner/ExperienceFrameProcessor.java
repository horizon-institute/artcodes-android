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

package uk.ac.horizon.artcodes.scanner;

import android.util.Log;

import org.opencv.core.Mat;

import uk.ac.horizon.artcodes.model.MarkerSettings;
import uk.ac.horizon.artcodes.scanner.camera.FrameProcessor;
import uk.ac.horizon.artcodes.scanner.overlay.Overlay;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;

public class ExperienceFrameProcessor extends FrameProcessor
{
	private final Overlay overlay;
	private final MarkerSettings settings;

	public ExperienceFrameProcessor(MarkerSettings settings, Overlay overlay)
	{
		this.overlay = overlay;
		this.settings = settings;
	}

	@Override
	public void process(Mat frame)
	{
		try
		{
			for (ImageProcessor imageProcessor : settings.pipeline)
			{
				frame = imageProcessor.process(frame);
			}

			//if (overlay.hasOutput(frame))
			//{
			//	rotate(frame, frame);
			//}
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}

		// TODO Test if camera needs to be focused
//				if (CameraController.deviceNeedsManualAutoFocus && framesSinceLastMarker > 2 && System.currentTimeMillis() - timeOfLastAutoFocus >= 5000)
//				{
//					timeOfLastAutoFocus = System.currentTimeMillis();
//					camera.performManualAutoFocus(new Camera.AutoFocusCallback()
//					{
//						@Override
//						public void onAutoFocus(boolean b, Camera camera)
//						{
//						}
//					});
//				}
	}

	protected Mat crop(Mat image, int surfaceWidth, int surfaceHeight)
	{
		final int size = Math.min(image.width(), image.height());

		final int colStart = (image.width() - size) / 2;
		final int rowStart = (image.height() - size) / 2;

		final float surfaceMax = Math.max(surfaceHeight, surfaceWidth);

		float sizeRatio = surfaceMax / Math.max(image.width(), image.height());
		Log.i("", "Size ratio = " + sizeRatio);

		int viewfinderSize = (int) (Math.max(colStart, rowStart) * sizeRatio);
		Log.i("", "Size = " + size + ", Viewfinder size = " + viewfinderSize);
		overlay.setViewfinderSize(viewfinderSize);

		return image.submat(rowStart, rowStart + size, colStart, colStart + size);
	}
}