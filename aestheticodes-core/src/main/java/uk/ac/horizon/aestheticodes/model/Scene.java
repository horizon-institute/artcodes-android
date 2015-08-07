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

package uk.ac.horizon.aestheticodes.model;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.List;

public class Scene {

    private List<MatOfPoint> contours;
    private Mat hierarchy;
    private boolean isRotated = false;

    public Scene() {}
    public Scene(List<MatOfPoint> contours, Mat hierarchy) {
        this.contours = contours;
        this.hierarchy = hierarchy;
    }

    public Scene setContours(List<MatOfPoint> contours) {
        this.contours = contours;
        return this;
    }

    public Scene setHierarchy(Mat hierarchy) {
        this.hierarchy = hierarchy;
        return this;
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }

    public Mat getHierarchy() {
        return hierarchy;
    }

    /**
     * @return Is the scene rotated from its correct orientation?
     */
    public boolean isRotated()
    {
        return this.isRotated;
    }

    /**
     *
     * @param isRotated Is the scene rotated from its correct orientation?
     */
    public void setRotated(boolean isRotated)
    {
        this.isRotated = isRotated;
    }

    public void release()
    {
        if (contours!=null)
        {
            this.contours.clear();
        }
        if (hierarchy!=null)
        {
            this.hierarchy.release();
        }
    }
}

