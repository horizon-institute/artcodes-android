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
import org.opencv.imgproc.Imgproc;

import java.util.List;

import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.detect.Marker;

public class MarkerOutlineLayer extends Layer
{
	@Override
	public void drawMarkers(Mat overlay, List<Marker> markers, List<MatOfPoint> contours)
	{
		for (Marker marker : markers)
		{
			for (Marker.MarkerDetails details : marker.getMarkerDetails())
			{
				if (detectedColour != null)
				{
					// drawMarkers marker outline
					Imgproc.drawContours(overlay, contours, details.markerIndex, outlineColour, 7);
					Imgproc.drawContours(overlay, contours, details.markerIndex, detectedColour, 5);
				}
			}
		}
	}

	@Override
	public int getIcon()
	{
		return R.drawable.ic_border_outer_white_24dp;
	}

	@Override
	public Layer getNext()
	{
		return new MarkerRegionLayer();
	}

	@Override
	public boolean hasOutput()
	{
		return true;
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_marker_outline;
	}
}
