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

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CmyGreyscaler extends Greyscaler
{
	private final float cMultiplier, mMultiplier, yMultiplier;
	private final int singleChannel;

	public CmyGreyscaler(double hueShift, double cMultiplier, double mMultiplier, double yMultiplier, boolean invert)
	{
		super();
		Log.i(KEY, "Creating CmyGreyscaler");

		this.cMultiplier = (float) cMultiplier;
		this.mMultiplier = (float) mMultiplier;
		this.yMultiplier = (float) yMultiplier;
		if (cMultiplier == 1 && mMultiplier == 0 && yMultiplier == 0)
		{
			this.singleChannel = 2;
		} else if (cMultiplier == 0 && mMultiplier == 1 && yMultiplier == 0)
		{
			this.singleChannel = 1;
		} else if (cMultiplier == 0 && mMultiplier == 0 && yMultiplier == 1)
		{
			this.singleChannel = 0;
		} else
		{
			this.singleChannel = -1;
		}
	}

	@Override
	protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
	{
		if (greyscaleImage == null || greyscaleImage.rows() != colorImage.rows() || greyscaleImage.cols() != colorImage.cols())
		{
			Log.i(KEY, "Creating new Mat buffer (7)");
			greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
		}

		int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
		if (this.colorPixelBuffer == null || this.colorPixelBuffer.length < desiredBufferSize)
		{
			Log.i(KEY, "Creating new byte[" + desiredBufferSize + "] buffer (8)");
			this.colorPixelBuffer = new byte[colorImage.rows() * colorImage.cols() * colorImage.channels()];
		}

		colorImage.get(0, 0, this.colorPixelBuffer);
		if (this.singleChannel == -1)
		{
			for (int c = 0, g = 0; c < desiredBufferSize; c += 3, ++g)
			{
				this.colorPixelBuffer[g] = (byte) ((1 - this.colorPixelBuffer[c + 2] / 255.0f) * this.cMultiplier * 255 + (1 - this.colorPixelBuffer[c + 1] / 255.0f) * this.mMultiplier * 255 + (1 - this.colorPixelBuffer[c] / 255.0f) * this.yMultiplier * 255);
			}
		} else
		{
			for (int c = 0, g = 0; c < desiredBufferSize; c += 3, ++g)
			{
				this.colorPixelBuffer[g] = (byte) (255 - this.colorPixelBuffer[c + this.singleChannel]);
			}
		}
		greyscaleImage.put(0, 0, this.colorPixelBuffer);
		return greyscaleImage;
	}

	@Override
	protected boolean useIntensityShortcut()
	{
		return false;
	}
}