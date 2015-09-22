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

package uk.ac.horizon.artcodes.scanner.overlay;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import uk.ac.horizon.artcodes.scanner.R;

public class ThresholdLayer extends Layer
{
	@Override
	public int getIcon()
	{
		return R.drawable.ic_filter_b_and_w_white_24dp;
	}

	@Override
	public Layer getNext()
	{
		return new ThresholdNullLayer();
	}

	@Override
	public boolean hasOutput()
	{
		return true;
	}

	@Override
	void drawThreshold(Mat image, Mat overlay)
	{
		Imgproc.cvtColor(image, overlay, Imgproc.COLOR_GRAY2BGRA);
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_threshold_on;
	}
}
