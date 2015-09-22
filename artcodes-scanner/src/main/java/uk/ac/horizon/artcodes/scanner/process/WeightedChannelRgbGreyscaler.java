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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

// TODO: Investigate why this crashes when allocating the weight Mat.
public class WeightedChannelRgbGreyscaler extends Greyscaler
{
	private final Mat weight;

	public WeightedChannelRgbGreyscaler(double redMultiplier, double greenMultiplier, double blueMultiplier)
	{
		super();
		Log.i(KEY, "Creating WeightedChannelRgbGreyscaler");

		weight = new Mat(1, 3, CvType.CV_32FC1, new Scalar(0));
		weight.put(0, 0, blueMultiplier);
		weight.put(0, 1, greenMultiplier);
		weight.put(0, 2, redMultiplier);
	}

	@Override
	protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
	{
		if (greyscaleImage == null || greyscaleImage.rows() != colorImage.rows() || greyscaleImage.cols() != colorImage.cols())
		{
			greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
		}
		Core.transform(colorImage, greyscaleImage, weight);
		return greyscaleImage;
	}

	@Override
	protected boolean useIntensityShortcut()
	{
		return false;
	}
}