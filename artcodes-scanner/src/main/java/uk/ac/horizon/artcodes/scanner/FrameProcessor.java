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

import android.hardware.Camera;
import android.util.Log;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessorSetting;

@SuppressWarnings("deprecation")
public class FrameProcessor implements Camera.PreviewCallback
{
	protected final List<ImageProcessor> pipeline = new ArrayList<>();
	protected final ImageBuffers buffers;

	public FrameProcessor(ImageBuffers buffers)
	{
		this.buffers = buffers;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		buffers.setImage(data);

		try
		{
			for (ImageProcessor imageProcessor : pipeline)
			{
				imageProcessor.process(buffers);
			}

			buffers.createOverlayBitmap();
		}
		catch (Exception e)
		{
			Log.e("FrameProcessor", e.getMessage(), e);
		}

		camera.addCallbackBuffer(data);
	}

	public List<ImageProcessorSetting> getSettings()
	{
		final List<ImageProcessorSetting> settings = new ArrayList<>();
		for (ImageProcessor imageProcessor : pipeline)
		{
			imageProcessor.getSettings(settings);
		}

		return settings;
	}

	byte[] createBuffer(Scanner.CameraInfo info, int surfaceWidth, int surfaceHeight)
	{
		byte[] buffer = buffers.createBuffer(info.getImageWidth(), info.getImageHeight(), info.getImageDepth());
		buffers.setROI(createROI(info.getImageWidth(), info.getImageHeight(), surfaceWidth, surfaceHeight));
		buffers.setRotation(info.getRotation());
		buffers.setFrontFacing(info.isFrontFacing());
		return buffer;
	}

	protected Rect createROI(int imageWidth, int imageHeight, int surfaceWidth, int surfaceHeight)
	{
		return null;
	}
}
