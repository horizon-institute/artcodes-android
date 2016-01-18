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

package uk.ac.horizon.artcodes.scanner.process;

import android.util.Log;

import java.util.List;

import uk.ac.horizon.artcodes.scanner.ImageBuffers;

public class CmykGreyscaler implements ImageProcessor
{
	private final float cMultiplier, mMultiplier, yMultiplier, kMultiplier;
	private final int singleChannel;

	public CmykGreyscaler(double hueShift, double cMultiplier, double mMultiplier, double yMultiplier, double kMultiplier, boolean invert)
	{
		super();
		Log.i("greyscale", "Creating CmykGreyscaler");

		this.cMultiplier = (float) cMultiplier;
		this.mMultiplier = (float) mMultiplier;
		this.yMultiplier = (float) yMultiplier;
		this.kMultiplier = (float) kMultiplier;
		if (cMultiplier == 1 && mMultiplier == 0 && yMultiplier == 0 && kMultiplier == 0)
		{
			this.singleChannel = 2;
		}
		else if (cMultiplier == 0 && mMultiplier == 1 && yMultiplier == 0 && kMultiplier == 0)
		{
			this.singleChannel = 1;
		}
		else if (cMultiplier == 0 && mMultiplier == 0 && yMultiplier == 1 && kMultiplier == 0)
		{
			this.singleChannel = 0;
		}
		else if (cMultiplier == 0 && mMultiplier == 0 && yMultiplier == 0 && kMultiplier == 1)
		{
			this.singleChannel = 3;
		}
		else
		{
			this.singleChannel = -1;
		}
	}

	@Override
	public void process(ImageBuffers images)
	{
//		if (greyscaleImage == null || greyscaleImage.rows() != colorImage.rows() || greyscaleImage.cols() != colorImage.cols())
//		{
//			Log.i(KEY, "Creating new Mat buffer (6)");
//			greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
//		}
//
//            /*
//            It seems faster to get/set the whole image as an array.
//            Here the same array is used as both input and output.
//             */
//
//		int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
//		if (this.colorPixelBuffer == null || this.colorPixelBuffer.length < desiredBufferSize)
//		{
//			Log.i(KEY, "Creating new byte[" + desiredBufferSize + "] buffer (7)");
//			this.colorPixelBuffer = new byte[colorImage.rows() * colorImage.cols() * colorImage.channels()];
//		}
//		colorImage.get(0, 0, this.colorPixelBuffer);
//		float k, r, g, b;
//		if (this.singleChannel == -1)
//		{
//			for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j)
//			{
//				b = this.colorPixelBuffer[i] / 255.0f;
//				g = this.colorPixelBuffer[i + 1] / 255.0f;
//				r = this.colorPixelBuffer[i + 2] / 255.0f;
//				k = Math.min(1 - r, Math.min(1 - g, 1 - b));
//				this.colorPixelBuffer[j] = (byte) (k * kMultiplier * 255);
//				this.colorPixelBuffer[j] += (byte) (255 * cMultiplier * ((1 - r - k) / (1 - k)));
//				this.colorPixelBuffer[j] += (byte) (255 * mMultiplier * ((1 - g - k) / (1 - k)));
//				this.colorPixelBuffer[j] += (byte) (255 * yMultiplier * ((1 - b - k) / (1 - k)));
//			}
//		}
//		else if (this.singleChannel == 3) // k only
//		{
//			for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j)
//			{
//				this.colorPixelBuffer[j] = (byte) Math.min(255 - this.colorPixelBuffer[i], Math.min(255 - this.colorPixelBuffer[i + 1], 255 - this.colorPixelBuffer[i + 2]));
//			}
//		}
//		else // c, y or m only
//		{
//			float[] bgr = new float[3];
//			for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j)
//			{
//				bgr[0] = this.colorPixelBuffer[i] / 255.0f;
//				bgr[1] = this.colorPixelBuffer[i + 1] / 255.0f;
//				bgr[2] = this.colorPixelBuffer[i + 2] / 255.0f;
//				k = Math.min(1 - bgr[0], Math.min(1 - bgr[1], 1 - bgr[2]));
//				this.colorPixelBuffer[j] = (byte) (255 * ((1 - bgr[this.singleChannel] - k) / (1 - k)));
//			}
//		}
//		greyscaleImage.put(0, 0, this.colorPixelBuffer);
	}

	@Override
	public void getSettings(List<ImageProcessorSetting> settings)
	{
	}
}
