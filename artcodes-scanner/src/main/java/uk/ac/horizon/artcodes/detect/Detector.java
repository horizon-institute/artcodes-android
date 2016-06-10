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

package uk.ac.horizon.artcodes.detect;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.camera.CameraInfo;
import uk.ac.horizon.artcodes.process.ImageProcessor;

public class Detector
{
	static
	{
		if (!OpenCVLoader.initDebug())
		{
			Log.e("OpenCV", "Error Initializing OpenCV");
		}
	}

	protected final List<ImageProcessor> pipeline = new ArrayList<>();
	protected final List<DetectorSetting> settings = new ArrayList<>();
	protected final ImageBuffers buffers = new ImageBuffers();
	private ImageView overlay;
	protected DetectorCallback callback;

	public Detector()
	{
	}

	public void setCallback(DetectorCallback callback)
	{
		this.callback = callback;
	}

	public void setOverlay(ImageView overlay)
	{
		this.overlay = overlay;
	}

	public void setData(final byte[] data)
	{
		buffers.setImage(data);
		try
		{
			for (ImageProcessor imageProcessor : pipeline)
			{
				imageProcessor.process(buffers);
			}

			if(overlay != null)
			{
				final Bitmap overlayBitmap = buffers.createOverlayBitmap();
				if(overlayBitmap != null)
				{
					overlay.post(new Runnable()
					{
						@Override
						public void run()
						{

							overlay.setImageBitmap(overlayBitmap);
						}
					});
				}
			}
		}
		catch (Exception e)
		{
			Log.e("Detector", e.getMessage(), e);
		}
	}

	private void createSettings()
	{
		settings.clear();
		for (ImageProcessor imageProcessor : pipeline)
		{
			imageProcessor.getSettings(settings);
		}
	}

	public List<DetectorSetting> getSettings()
	{
		return settings;
	}

	public byte[] createBuffer(CameraInfo info, int surfaceWidth, int surfaceHeight)
	{
		byte[] buffer = buffers.createBuffer(info.getImageWidth(), info.getImageHeight(), info.getImageDepth());
		buffers.setROI(createROI(info.getImageWidth(), info.getImageHeight(), surfaceWidth, surfaceHeight));
		buffers.setRotation(info.getRotation());
		buffers.setFrontFacing(info.isFrontFacing());
		createSettings();
		return buffer;
	}

	protected Rect createROI(int imageWidth, int imageHeight, int surfaceWidth, int surfaceHeight)
	{
		if(callback != null)
		{
			callback.detectionStart(100);
		}
		return new Rect(0, 0, imageWidth, imageHeight);
	}
}
