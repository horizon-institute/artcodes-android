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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerCodeFactoryTouchingExtension extends MarkerCodeFactory
{
    protected static final String REGION_TOUCHING = "touching";
    protected static final int LINE_WIDTH = 10;

    protected static class TouchingMarkerDetails extends MarkerCode.MarkerDetails
    {
        private final List<List<MatOfPoint>> allOverlapContours;
        public TouchingMarkerDetails(List<List<MatOfPoint>> allOverlapContours, MarkerCode.MarkerDetails details)
        {
            super(details);
            this.allOverlapContours = allOverlapContours;
        }

        public int touchCount()
        {
            int count = 0;
            for (Map<String, Object> regionDetails : this.regions)
            {
                count += ((List<Integer>)regionDetails.get(REGION_TOUCHING)).size();
            }
            return count;
        }
    }

    @Override
    protected String getCodeFor(MarkerCode.MarkerDetails details)
    {
        int count = 0;
        StringBuilder builder = new StringBuilder(details.regions.size()*4-1);
        for (Map<String, Object> regionDetails : details.regions)
        {
            if (builder.length()!=0)
            {
                builder.append(':');
            }
            builder.append(regionDetails.get(MarkerCode.MarkerDetails.REGION_VALUE));
            builder.append('-');
            builder.append(((List<Integer>)regionDetails.get(REGION_TOUCHING)).size());
            count += ((List<Integer>)regionDetails.get(REGION_TOUCHING)).size();
        }
        builder.append(" (T: ");
        builder.append(count);
        builder.append(')');
        return builder.toString();
    }

    @Override
    protected MarkerCode.MarkerDetails parseRegionsAt(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionStatus[] error, int errorIndex)
    {
        MarkerCode.MarkerDetails details =  super.parseRegionsAt(nodeIndex, contours, hierarchy, experience, error, errorIndex);

        if (details != null)
        {
            Rect markerBoundingBox = Imgproc.boundingRect(contours.get(details.markerIndex));
            Size size = markerBoundingBox.size();
            List<List<MatOfPoint>> allOverlapContours = new ArrayList<>();

            boolean[] drawnMat = new boolean[details.regions.size()];
            Mat[] mats = new Mat[details.regions.size()];

            // create bounding boxes for all regions
            Rect[] boundingBoxes = new Rect[details.regions.size()];
            for (int i=0; i<details.regions.size(); ++i)
            {
                Map<String, Object> regionDetails = details.regions.get(i);
                boundingBoxes[i] = Imgproc.boundingRect(contours.get((Integer)regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX)));
                drawnMat[i] = false;
                regionDetails.put(REGION_TOUCHING, new ArrayList<Integer>());
            }

            Mat temp = new Mat(markerBoundingBox.size(), CvType.CV_8UC1);
            for (int i=0; i<details.regions.size(); ++i)
            {
                for (int j=i+1; j<details.regions.size(); ++j)
                {
                    if (!drawnMat[i])
                    {
                        mats[i] = new Mat(size, CvType.CV_8UC1, new Scalar(0));
                        Map<String, Object> regionDetails = details.regions.get(i);
                        Imgproc.drawContours(mats[i], contours, (Integer)regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX), new Scalar(255), LINE_WIDTH, 8, hierarchy, 0, new Point(-markerBoundingBox.tl().x, -markerBoundingBox.tl().y));
                        drawnMat[i] = true;
                    }
                    if (!drawnMat[j])
                    {
                        mats[j] = new Mat(size, CvType.CV_8UC1, new Scalar(0));
                        Map<String, Object> regionDetails = details.regions.get(j);
                        Imgproc.drawContours(mats[j], contours, (Integer)regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX), new Scalar(255), LINE_WIDTH, 8, hierarchy, 0,new Point(-markerBoundingBox.tl().x, -markerBoundingBox.tl().y));
                        drawnMat[j] = true;
                    }
                    Core.bitwise_and(mats[i], mats[j], temp);
                    int count = Core.countNonZero(temp);
                    if (count>0)
                    {
                        ((ArrayList<Integer>)details.regions.get(i).get(REGION_TOUCHING)).add(j);
                        ((ArrayList<Integer>)details.regions.get(j).get(REGION_TOUCHING)).add(i);
                    }


                    List<MatOfPoint> overlapContours = new ArrayList<>();
                    Mat h = new Mat();
                    Imgproc.findContours(temp, overlapContours, h, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                    allOverlapContours.add(overlapContours);
                }
                if (drawnMat[i]) {
                    mats[i].release();
                }
            }

            mats = null;
            boundingBoxes = null;

            details = new TouchingMarkerDetails(allOverlapContours, details);
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
                int result = ((Integer) region1.get(MarkerCode.MarkerDetails.REGION_VALUE)).compareTo((Integer) region2.get(MarkerCode.MarkerDetails.REGION_VALUE));
                if (result == 0)
                {
                    return new Integer(((List<Integer>)region1.get(REGION_TOUCHING)).size()).compareTo(new Integer(((List<Integer>)region2.get(REGION_TOUCHING)).size()));
                }
                else
                {
                    return result;
                }
            }
        });
    }

    @Override
    protected boolean validate(MarkerCode.MarkerDetails details, Experience experience, DetectionStatus[] error, int errorIndex)
    {
        if (super.validate(details, experience, error, errorIndex))
        {
            if (((TouchingMarkerDetails)details).touchCount() % 3 == 0 && ((TouchingMarkerDetails)details).touchCount() > 0)
            {
                return true;
            }
            else
            {
                error[errorIndex] = DetectionStatus.extensionSpecificError;
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void draw(MarkerCode marker, Mat image, List<MatOfPoint> contours, Mat hierarchy, Scalar markerColor, Scalar outlineColor, Scalar regionColor)
    {
        for (MarkerCode.MarkerDetails markerDetails : marker.getMarkerDetails())
        {
            if (markerDetails instanceof TouchingMarkerDetails)
            {
                for (Map<String, Object> region : markerDetails.regions)
                {
                    int regionIndex = (Integer) region.get(MarkerCode.MarkerDetails.REGION_INDEX);
                    Imgproc.drawContours(image, contours, regionIndex, new Scalar(255, 255, 0, 127), LINE_WIDTH, 8, hierarchy, 0, new Point(0, 0));
                }

                Rect markerBoundingBox = Imgproc.boundingRect(contours.get(markerDetails.markerIndex));
                TouchingMarkerDetails tMarkerDetails = (TouchingMarkerDetails) markerDetails;
                for (List<MatOfPoint> overlapContours : tMarkerDetails.allOverlapContours)
                {
                    if (overlapContours != null && overlapContours.size() > 0)
                    {
                        Imgproc.drawContours(image, overlapContours, -1, new Scalar(255, 0, 0, 255), -1, 8, new Mat(), 0, markerBoundingBox.tl());
                    }
                }
            }
        }
    }

    @Override
    public String[] getDebugMessagesForErrorType(DetectionStatus errorType, Experience experience)
    {
        if (errorType==DetectionStatus.extensionSpecificError)
        {
            return new String[] {"Wrong number of touching regions", "The total value of touching regions must be more than 0 and a multiple of 3"};
        }
        return super.getDebugMessagesForErrorType(errorType, experience);
    }
}
