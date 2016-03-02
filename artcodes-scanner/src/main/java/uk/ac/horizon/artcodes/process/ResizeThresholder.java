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

package uk.ac.horizon.artcodes.process;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;

public class ResizeThresholder implements ImageProcessor
{
	private transient int neighbourhood = 5;

	@Override
	public void process(ImageBuffers buffers)
	{
		Imgproc.resize(buffers.getImage(), buffers.getImage(), new Size(540, 540));

		Imgproc.GaussianBlur(buffers.getImage(), buffers.getImage(), new Size(5, 5), 0);

		// TODO if (!detected)
		//{
		neighbourhood = (neighbourhood % 50) + 5;
		//}
		//Log.i(TAG, "Neighbourhood = " + neighbourhood);
		Imgproc.adaptiveThreshold(buffers.getImage(), buffers.getImage(), 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, neighbourhood, 2);
	}

	@Override
	public void getSettings(List<DetectorSetting> settings)
	{

	}
}
