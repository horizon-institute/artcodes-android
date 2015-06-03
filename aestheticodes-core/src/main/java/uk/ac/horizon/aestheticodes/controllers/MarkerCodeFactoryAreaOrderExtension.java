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

package uk.ac.horizon.aestheticodes.controllers;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerCodeFactoryAreaOrderExtension extends MarkerCodeFactory
{

    private static final String REGION_AREA = "area";

    @Override
    protected MarkerCode.MarkerDetails parseRegionsAt(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionError[] error, int errorIndex)
    {
        MarkerCode.MarkerDetails details = super.parseRegionsAt(nodeIndex, contours, hierarchy, experience, error, errorIndex);

        if (details != null)
        {
            for (Map<String, Object> region : details.regions)
            {
                int index = (Integer) region.get(MarkerCode.MarkerDetails.REGION_INDEX);
                double area = Imgproc.contourArea(contours.get(index));
                region.put(REGION_AREA, area);
            }
        }

        return details;
    }

    @Override
    protected void sortCode(MarkerCode.MarkerDetails details)
    {
        Collections.sort(details.regions, new Comparator<Map<String, Object>>()
        {
            @Override
            public int compare(Map<String, Object> region1, Map<String, Object> region2)
            {
                return ((Double) region1.get(REGION_AREA)).compareTo((Double) region2.get(REGION_AREA));
            }
        });
    }
}
