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

package uk.ac.horizon.artcodes.drawer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import uk.ac.horizon.artcodes.detect.marker.Marker;

public class MarkerThumbnailDrawer implements MarkerDrawer
{
    private static final Scalar COLOR = new Scalar(255,255,255,255);
    private static final Scalar BACKGROUND = new Scalar(0,0,0,0);

    @Override
    public Mat drawMarker(Marker marker, ArrayList<MatOfPoint> contours, Mat hierarchy, Rect boundingRect, Mat imageToDrawOn)
    {
        if (imageToDrawOn==null)
        {
            if (boundingRect==null)
            {
                boundingRect = Imgproc.boundingRect(contours.get(marker.markerIndex));
            }
            Mat output = new Mat(boundingRect.size(), CvType.CV_8UC4, BACKGROUND);

            Imgproc.drawContours(output, contours, marker.markerIndex, COLOR, Core.FILLED, Core.LINE_8, hierarchy, 2, new Point(-boundingRect.tl().x, -boundingRect.tl().y));

            return output;
        }
        else
        {
            Imgproc.drawContours(imageToDrawOn, contours, marker.markerIndex, COLOR, Core.FILLED, Core.LINE_8, hierarchy, 2, new Point(0, 0));
            return imageToDrawOn;
        }
    }
}
