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

package uk.ac.horizon.artcodes.scanner.process;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the method to greyscale an image.
 * This is a base abstract class, use a subclass like Greyscaler.IntensityGreyscaler or Greyscaler.RGBGreyscaler.
 */
public abstract class Greyscaler
{
	protected static final String KEY = "GREY";

	public static Greyscaler getGreyscaler(double hueShift, List<Object> greyscaleOptions, boolean invert)
	{
		int threadCount = 10;

		if (greyscaleOptions != null)
		{
			if (greyscaleOptions.size() == 4 && greyscaleOptions.get(0).equals("RGB"))
			{
				double r = (double) greyscaleOptions.get(1), g = (double) greyscaleOptions.get(2), b = (double) greyscaleOptions.get(3);
				if (r == 1 && g == 0 && b == 0)
				{
					List<Greyscaler> greyscalers = new ArrayList<>();
					while (greyscalers.size() < threadCount)
					{
						greyscalers.add(new RGBGreyscaler(hueShift, 2, invert));
					}
					return new ThreadedGreyscaler(greyscalers);
				}
				else if (r == 0 && g == 1 && b == 0)
				{
					List<Greyscaler> greyscalers = new ArrayList<>();
					while (greyscalers.size() < threadCount)
					{
						greyscalers.add(new RGBGreyscaler(hueShift, 1, invert));
					}
					return new ThreadedGreyscaler(greyscalers);
				}
				else if (r == 0 && g == 0 && b == 1)
				{
					List<Greyscaler> greyscalers = new ArrayList<>();
					while (greyscalers.size() < threadCount)
					{
						greyscalers.add(new RGBGreyscaler(hueShift, 0, invert));
					}
					return new ThreadedGreyscaler(greyscalers);
				}
				else if (r == 0.299 && g == 0.587 && b == 0.114)
				{
					return new IntensityGreyscaler(hueShift, invert);
				}
				else
				{
					return new WeightedChannelRgbGreyscaler(hueShift, ((Number) greyscaleOptions.get(1)).doubleValue(), ((Number) greyscaleOptions.get(2)).doubleValue(), ((Number) greyscaleOptions.get(3)).doubleValue(), invert);
				}
			}
			else if (greyscaleOptions.size() == 5 && greyscaleOptions.get(0).equals("CMYK"))
			{
				List<Greyscaler> greyscalers = new ArrayList<>();
				while (greyscalers.size() < threadCount)
				{
					greyscalers.add(new CmykGreyscaler(hueShift, ((Number) greyscaleOptions.get(1)).doubleValue(), ((Number) greyscaleOptions.get(2)).doubleValue(), ((Number) greyscaleOptions.get(3)).doubleValue(), ((Number) greyscaleOptions.get(4)).doubleValue(), invert));
				}
				return new ThreadedGreyscaler(greyscalers);
			}
			else if (greyscaleOptions.size() == 4 && greyscaleOptions.get(0).equals("CMY"))
			{
				List<Greyscaler> greyscalers = new ArrayList<>();
				while (greyscalers.size() < threadCount)
				{
					greyscalers.add(new CmyGreyscaler(hueShift, ((Number) greyscaleOptions.get(1)).doubleValue(), ((Number) greyscaleOptions.get(2)).doubleValue(), ((Number) greyscaleOptions.get(3)).doubleValue(), invert));
				}
				return new ThreadedGreyscaler(greyscalers);
			}
		}
		return new IntensityGreyscaler(hueShift, invert);
	}

	/**
	 * This is a buffer that can be used by sub-classes for storing intermediate color data.
	 * If the array is not long enough (or null) recreate it at the desired length but never make it smaller.
	 * Data in this buffer may be overwritten.
	 */
	protected byte[] colorPixelBuffer = null;

	public Greyscaler()
	{
	}

	/**
	 * Create a greyscale image.
	 *
	 * @param yuvImage       A YUV NV12 image (CV_8UC1). Should not be null.
	 * @param greyscaleImage A greyscale image (CV_8UC1) to place the result in, depending on the method used this may or may not be used (can be null).
	 * @return A greyscale image. Depending on the method used this may or may not be the same buffer passed in as greyscaleImage.
	 */
	public Mat greyscaleImage(Mat yuvImage, Mat greyscaleImage)
	{
//        if (this.useIntensityShortcut() && hueShift==0)
//        {
//            if (greyscaleImage != null)
//            {
//                greyscaleImage.release();
//            }
//            // cut off the UV components
//            greyscaleImage = yuvImage.submat(0, (yuvImage.rows()/3)*2, 0, yuvImage.cols());
//        }
//        else
//        {
//            int desiredRows = (yuvImage.rows() / 3) * 2, desiredCols = yuvImage.cols();
//            if (this.threeChannelBuffer==null || this.threeChannelBuffer.rows()!=desiredRows || this.threeChannelBuffer.cols()!=desiredCols)
//            {
//                Log.i(KEY, "Creating new Mat buffer (1)");
//                this.threeChannelBuffer = new Mat(desiredRows, desiredCols, CvType.CV_8UC3);
//            }
//            Imgproc.cvtColor(yuvImage, this.threeChannelBuffer, Imgproc.COLOR_YUV2BGR_NV21);
//            this.justHueShiftImage(this.threeChannelBuffer, this.threeChannelBuffer);
//            greyscaleImage = this.justGreyscaleImage(this.threeChannelBuffer, greyscaleImage);
//        }

		return greyscaleImage;
	}

	/**
	 * Release any resources held.
	 */
	public void release()
	{
//        if (this.threeChannelBuffer != null)
//        {
//            this.threeChannelBuffer.release();
//            this.threeChannelBuffer = null;
//        }
		colorPixelBuffer = null;
	}

	/**
	 * Sub-classes should implement this.
	 *
	 * @param colorImage     A BGR image (CV_8UC3).
	 * @param greyscaleImage A greyscale image (CV_8UC1).
	 */
	protected abstract Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage);

	/**
	 * Sub-classes should implement this.
	 *
	 * @return true if the super-class should a more efficient way to create an intensity greyscale image, false if the subclass is going to use another method.
	 */
	protected abstract boolean useIntensityShortcut();
}