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

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

import java.util.ArrayList;

import uk.ac.horizon.artcodes.detect.marker.Marker;

public interface MarkerDrawer
{
    /**
     * Draw a marker in an image or in a new image. Implementing classes handle style of drawing.
     * @param marker The marker to draw.
     * @param contours Data the marker was detected from.
     * @param hierarchy Data the marker was detected from.
     * @param boundingRect A bounding box for the marker if one has already been computed, can be null.
     * @param imageToDrawOn An image to draw on, can be null.
     * @return imageToDrawOn, if provided, otherwise a new image.
     */
    Mat drawMarker(Marker marker, ArrayList<MatOfPoint> contours, Mat hierarchy, Rect boundingRect, Mat imageToDrawOn);
}
