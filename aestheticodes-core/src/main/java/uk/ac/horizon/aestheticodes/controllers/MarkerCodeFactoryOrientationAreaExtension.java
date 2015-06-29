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

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class MarkerCodeFactoryOrientationAreaExtension extends MarkerCodeFactoryAreaOrientationExtension
{


    @Override
    protected void sortCode(MarkerCode.MarkerDetails details)
    {

        // sort by left to right
        Collections.sort(details.regions, new Comparator<Map<String, Object>>()
        {
            @Override
            public int compare(Map<String, Object> region1, Map<String, Object> region2)
            {
                return ((Double) region1.get("x")).compareTo((Double) region2.get("x"));
            }
        });

        // label
        int count = 0;
        for (Map<String, Object> region : details.regions)
        {
            region.put(REGION_LABEL, (char)(65+count++));
        }

        // sort by area
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
