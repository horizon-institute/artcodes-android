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
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerCode
{

    public static class MarkerDetails
    {
        public static final String REGION_INDEX = "index";
        public static final String REGION_VALUE = "value";
        public int markerIndex;
        public List<Map<String, Object> > regions;
        public Integer embeddedChecksum;
        public Integer embeddedChecksumRegionIndex;

        public MarkerDetails()
        {
            regions = new ArrayList<>();
        }

        public MarkerDetails(MarkerDetails other)
        {
            this.markerIndex = other.markerIndex;
            this.regions = other.regions;
            this.embeddedChecksum = other.embeddedChecksum;
            this.embeddedChecksumRegionIndex = other.embeddedChecksumRegionIndex;
        }

        public Map<String, Object> createRegion(int index, int value)
        {
            Map<String, Object> region = new HashMap<>();
            region.put(REGION_INDEX, new Integer(index));
            region.put(REGION_VALUE, new Integer(value));

            this.regions.add(region);
            return region;
        }
    }

    interface MarkerDrawer
    {
        void draw(MarkerCode marker, Mat image, List<MatOfPoint> contours, Mat hierarchy, Scalar markerColor, Scalar outlineColor, Scalar regionColor);
    }

    private final String codeKey;
    private final List<Integer> code;
    private final List<MarkerDetails> markerDetails = new ArrayList<>();
    private final MarkerDrawer markerDrawer;
    private int occurrences = 1;
    private long firstDetected=0, lastDetected=0;

    public MarkerCode(String codeKey, MarkerDetails markerDetails, MarkerDrawer markerDrawer)
    {
        this.codeKey = codeKey;
        this.markerDetails.add(markerDetails);
        this.markerDrawer = markerDrawer;

        this.code = new ArrayList<>();
        for (Map<String, Object> region : markerDetails.regions)
        {
            this.code.add((Integer) region.get(MarkerDetails.REGION_VALUE));
        }
    }

    public void addMarkerInstance(MarkerCode other)
    {
        if (this.codeKey.equals(other.codeKey))
        {
            this.markerDetails.addAll(other.markerDetails);
            this.occurrences += other.occurrences;
        }
    }

    public List<MarkerDetails> getMarkerDetails()
    {
        return this.markerDetails;
    }

    public void draw(Mat image, List<MatOfPoint> contours, Mat hierarchy, Scalar markerColor, Scalar outlineColor, Scalar regionColor)
    {
        this.markerDrawer.draw(this, image, contours, hierarchy, markerColor, outlineColor, regionColor);
    }

    public long getFirstDetected()
    {
        return firstDetected;
    }

    public void setFirstDetected(long value)
    {
        this.firstDetected = value;
    }

    public long getLastDetected()
    {
        return lastDetected;
    }

    public void setLastDetected(long value)
    {
        this.lastDetected = value;
    }

    /// compatibility methods

    public int getOccurrences()
    {
        return occurrences;
    }

    public void setOccurrences(int value)
    {
        this.occurrences = value;
    }

    public List<Integer> getComponentIndexs()
    {
        List<Integer> indexes = new ArrayList<>();
        for (MarkerDetails details : this.markerDetails)
        {
            indexes.add((Integer) details.markerIndex);
        }
        return indexes;
    }

    public List<Integer> getCode()
    {
        return this.code;
    }

    public String getCodeKey()
    {
        return codeKey;
    }

    boolean isCodeEqual(MarkerCode marker)
    {
        return getCodeKey().equals(marker.getCodeKey());
    }

    public int hashCode()
    {
        return this.codeKey.hashCode();
    }

    public boolean equals(Object m)
    {
        return m.getClass() == this.getClass() && isCodeEqual((MarkerCode) m);
    }

}
