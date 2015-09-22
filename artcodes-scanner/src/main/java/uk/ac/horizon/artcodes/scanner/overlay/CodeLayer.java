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

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.detect.Marker;

public class CodeLayer extends Layer
{
	@Override
	public void drawMarkers(Mat overlay, List<Marker> markers, List<MatOfPoint> contours)
	{
		for (Marker marker : markers)
		{
			for (Marker.MarkerDetails details : marker.getMarkerDetails())
			{
				Rect bounds = Imgproc.boundingRect(contours.get(details.markerIndex));
				String markerCode = marker.getCodeKey();

				Core.putText(overlay, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
				Core.putText(overlay, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
			}
		}
	}

	@Override
	public int getIcon()
	{
		return 0;
	}

	@Override
	public Layer getNext()
	{
		return new CodeNullLayer();
	}

	@Override
	public boolean hasOutput()
	{
		return true;
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_code;
	}
}
