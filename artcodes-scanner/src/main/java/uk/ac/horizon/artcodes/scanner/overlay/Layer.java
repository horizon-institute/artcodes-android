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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;

import java.util.List;

import uk.ac.horizon.artcodes.scanner.detect.Marker;

public abstract class Layer
{
	protected Scalar detectedColour = new Scalar(255, 255, 0, 255);
	protected Scalar regionColour = new Scalar(255, 128, 0, 255);
	protected Scalar outlineColour = new Scalar(0, 0, 0, 255);

	void drawMarkers(Mat overlay, List<Marker> markers, List<MatOfPoint> contours)
	{

	}

	void drawThreshold(Mat image, Mat overlay)
	{

	}

	abstract int getFeedback();

	abstract int getIcon();

	abstract Layer getNext();

	boolean hasOutput()
	{
		return false;
	}
}
