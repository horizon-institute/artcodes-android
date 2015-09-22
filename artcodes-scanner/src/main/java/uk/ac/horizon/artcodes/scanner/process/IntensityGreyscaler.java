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

import org.opencv.core.Mat;

public class IntensityGreyscaler implements ImageProcessor
{
	public IntensityGreyscaler()
	{
		Log.i("", "Creating IntensityGreyscaler");
	}

	@Override
	public Mat process(Mat image, boolean detected)
	{
//		if (image == null || image.rows() != colorImage.rows() || image.cols() != colorImage.cols())
//		{
//			image = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
//		}
//		Imgproc.cvtColor(colorImage, image, Imgproc.COLOR_BGR2GRAY);
		return image;
	}
}