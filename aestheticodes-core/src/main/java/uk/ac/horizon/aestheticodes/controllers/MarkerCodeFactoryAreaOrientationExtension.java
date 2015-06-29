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
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerCodeFactoryAreaOrientationExtension extends MarkerCodeFactory
{

    public static final String REGION_AREA = "area", REGION_LABEL = "label";

    protected class Line
    {
        protected double m, c, xAxisCrossPoint;
        protected boolean isHorizontal, isVerticle, pointsToXAxis, pointsToYAxis;

        public Line(Point p1, Point p2)
        {
            this.isVerticle   = p1.x==p2.x;
            this.isHorizontal = p1.y==p2.y;
            this.pointsToXAxis = (p1.x < p2.x && p1.y > p2.y) || (p1.x > p2.x && p1.y > p2.y) || (p1.x == p2.x && p1.y > p2.y);
            this.pointsToYAxis = (p1.x > p2.x && p1.y < p2.y) || (p1.x > p2.x && p1.y > p2.y) || (p1.x > p2.x && p1.y == p2.y);
            if (!this.isHorizontal && !this.isVerticle)
            {
                this.m = ((double)p1.y-(double)p2.y)/((double)p1.x-(double)p2.x);
                // y=mx+c ... y-mx=c
                this.c = (double)p1.y-this.m*(double)p1.x;
            }
            else if (this.isHorizontal)
            {
                this.m = 0;
                this.c = (double)p1.y;
            }
            else if (this.isVerticle)
            {
                this.m = Double.POSITIVE_INFINITY;
                this.c = 0;
                this.xAxisCrossPoint = (double)p1.x;
            }
        }

        public Line(Point p, double m)
        {
            this.isHorizontal = m==0;
            this.isVerticle   = m==Double.POSITIVE_INFINITY;
            this.m = m;
            if (this.isVerticle)
            {
                this.xAxisCrossPoint = p.x;
                this.c = 0;
            }
            else if (this.isHorizontal)
            {
                this.c = (double)p.y;
            }
            else
            {
                this.c = (double)p.y-this.m*(double)p.x;
            }
        }

        public Point getIntersectionWith(Line otherLine)
        {
            if (this.m == otherLine.m)
            {
                return null;
            }
            else if (this.isVerticle || otherLine.isVerticle)
            {
                Line verticleLine = this.isVerticle ? this : otherLine;
                Line nonVerticleLine = this.isVerticle ? otherLine : this;

                if (nonVerticleLine.isHorizontal)
                {
                    return new Point(verticleLine.xAxisCrossPoint, nonVerticleLine.c);
                }
                else
                {
                    double x = verticleLine.xAxisCrossPoint;
                    return new Point(x, nonVerticleLine.m*x+nonVerticleLine.c);
                }
            }
            else if (this.isHorizontal || otherLine.isHorizontal)
            {
                Line horizontalLine = this.isHorizontal ? this : otherLine;
                Line nonHorizontalLine = this.isHorizontal ? otherLine : this;

                double y = horizontalLine.c;
                return new Point((y-nonHorizontalLine.c)/nonHorizontalLine.m, y);
            }

            //m1x+c1 = m2x+c2 ... m1x-m2x = +c2-c1 ... (m1-m2)x = +c2-c1 ... x = (c2-c1)/(m1-m2)
            double x = (otherLine.c-this.c)/(this.m-otherLine.m);
            //NSLog(@"getIntersectionWith: x = %1.1f = (%1.1f-%1.1f)/(%1.1f-%1.1f)", x, otherLine.c, self.c, self.m, otherLine.m);
            return new Point(x, this.m*x+this.c);
        }
    }

    protected class AreaOrientationMarkerDetails extends MarkerCode.MarkerDetails
    {
        public Point centerPoint, regionCenterPoint;
        public Line xAxis, yAxis, xAxisOpposite, yAxisOpposite;

        public AreaOrientationMarkerDetails(Point centerPoint, Point regionCenterPoint, MarkerCode.MarkerDetails details)
        {
            super(details);
            this.centerPoint = centerPoint;
            this.regionCenterPoint = regionCenterPoint;
        }
    }

    @Override
    protected String getCodeFor(MarkerCode.MarkerDetails details)
    {
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> regionDetails : details.regions)
        {
            if (builder.length()!=0)
            {
                builder.append(':');
            }
            builder.append(regionDetails.get(MarkerCode.MarkerDetails.REGION_VALUE));
            builder.append(regionDetails.get(REGION_LABEL));
        }
        return builder.toString();
    }

    @Override
    protected MarkerCode.MarkerDetails parseRegionsAt(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionError[] error, int errorIndex)
    {
        MarkerCode.MarkerDetails details = super.parseRegionsAt(nodeIndex, contours, hierarchy, experience, error, errorIndex);

        if (details != null)
        {
            List<Integer> regionsWithMaxDots = new ArrayList<>();
            int maxDots = -1;

            if (details.embeddedChecksum!=null)
            {
                regionsWithMaxDots.add(details.embeddedChecksumRegionIndex);
                maxDots = Integer.MAX_VALUE;
            }

            // find/add areas
            for (Map<String, Object> regionDetails : details.regions)
            {
                // find area
                MatOfPoint region = contours.get((Integer) regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX));
                double area = Imgproc.contourArea(region);
                regionDetails.put(REGION_AREA, area);

                //max dots
                if (regionDetails.get(MarkerCode.MarkerDetails.REGION_VALUE).equals(0))
                {
                    regionsWithMaxDots.clear();
                    regionsWithMaxDots.add((Integer) regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX));
                    maxDots = Integer.MAX_VALUE;
                }
                else if (regionDetails.get(MarkerCode.MarkerDetails.REGION_VALUE).equals(maxDots))
                {
                    regionsWithMaxDots.add((Integer) regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX));
                }
                else if ((Integer) regionDetails.get(MarkerCode.MarkerDetails.REGION_VALUE) > maxDots)
                {
                    regionsWithMaxDots.clear();
                    regionsWithMaxDots.add((Integer) regionDetails.get(MarkerCode.MarkerDetails.REGION_INDEX));
                    maxDots = (Integer) regionDetails.get(MarkerCode.MarkerDetails.REGION_VALUE);
                }
            }

            // find orientation
            Moments mo = Imgproc.moments(contours.get(details.markerIndex));
            Point centerOfMarker = new Point(mo.get_m10()/mo.get_m00() , mo.get_m01()/mo.get_m00());
            double cx=0, cy=0;
            for (Integer regionIndex : regionsWithMaxDots)
            {
                mo = Imgproc.moments(contours.get(regionIndex));
                cx += mo.get_m10()/mo.get_m00();
                cy += mo.get_m01()/mo.get_m00();
            }
            Point centerOfRegion = new Point(cx/regionsWithMaxDots.size(), cy/regionsWithMaxDots.size());
            AreaOrientationMarkerDetails markerDetail = new AreaOrientationMarkerDetails(centerOfMarker, centerOfRegion, details);
            details = markerDetail;
            Line lineOfGravity = new Line(centerOfMarker, centerOfRegion);

            Rect boundingBox = Imgproc.boundingRect(contours.get(details.markerIndex));
            defineAxisForBoundingBox(boundingBox, lineOfGravity, markerDetail);

            Point origin = markerDetail.xAxis.getIntersectionWith(markerDetail.yAxis);
            for (Map<String, Object> regionDetail : markerDetail.regions)
            {
                MatOfPoint regionContour = contours.get((Integer) regionDetail.get(MarkerCode.MarkerDetails.REGION_INDEX));
                double minX = 0;
                Point minPoint=null, minIntersectionPoint=null;
                List<Point> listOfPoints = regionContour.toList();
                for (int i=0; i<listOfPoints.size(); ++i)
                {
                    Line lineToXAxis = new Line(listOfPoints.get(i), lineOfGravity.m);
                    Point intersectionPoint = lineToXAxis.getIntersectionWith(markerDetail.xAxis);
                    double x = Math.sqrt(Math.pow((double) origin.x - (double) intersectionPoint.x, 2) + Math.pow((double) origin.y - (double) intersectionPoint.y, 2));
                    if (i==0 || x<minX)
                    {
                        minX = x;
                        minPoint = listOfPoints.get(i);
                        minIntersectionPoint = intersectionPoint;
                    }
                }
                regionDetail.put("x", minX);
                regionDetail.put("sortPoint", minPoint);
                regionDetail.put("sortIntersectionPoint", minIntersectionPoint);
            }
        }

        return details;
    }


    private void defineAxisForBoundingBox(Rect bb, Line line, AreaOrientationMarkerDetails markerDetail)
    {
        Point tr = new Point(bb.br().x, bb.tl().y);
        Point bl = new Point(bb.tl().x, bb.br().y);
        Point tl = bb.tl();
        Point br = bb.br();

        Point xAxisPoint, yAxisPoint, xAxisPointOpp, yAxisPointOpp;
        Point origin = null;

        if (line.isVerticle)
        {
            if (line.pointsToXAxis)
            {
                xAxisPoint = tl;
                yAxisPoint = br;
                xAxisPointOpp = yAxisPointOpp = bl;
                origin = tr;
            }
            else
            {
                xAxisPoint = br;
                yAxisPoint = tl;
                xAxisPointOpp = yAxisPointOpp = tr;
                origin = bl;
            }
        }
        else if (line.isHorizontal)
        {
            if (line.pointsToYAxis)
            {
                xAxisPoint = bl;
                xAxisPointOpp = br;
                yAxisPoint = tr;
                yAxisPointOpp = br;
                origin = tl;
            }
            else
            {
                xAxisPoint = tr;
                xAxisPointOpp = tl;
                yAxisPoint = bl;
                yAxisPointOpp = tl;
                origin = br;
            }
        }
        else if (line.pointsToXAxis)
        {
            if (line.pointsToYAxis)
            {
                yAxisPoint = tr;
                yAxisPointOpp = bl;
                xAxisPoint = tl;
                xAxisPointOpp = br;
            }
            else
            {
                yAxisPoint = br;
                yAxisPointOpp = tl;
                xAxisPoint = tr;
                xAxisPointOpp = bl;
            }
        }
        else
        {
            if (line.pointsToYAxis)
            {
                xAxisPoint = bl;
                xAxisPointOpp = tr;
                yAxisPoint = tl;
                yAxisPointOpp = br;
            }
            else
            {
                xAxisPoint = br;
                xAxisPointOpp = tl;
                yAxisPoint = bl;
                yAxisPointOpp = tr;
            }
        }

        Line xAxis, xAxisOpp, yAxis, yAxisOpp;
        Point endOfXAxis, endOfYAxis;
        if (line.isVerticle || line.isHorizontal)
        {
            xAxis =     new Line(origin, xAxisPoint);
            xAxisOpp =  new Line(yAxisPoint, xAxisPointOpp);
            yAxis =     new Line(origin, yAxisPoint);
            yAxisOpp =  new Line(xAxisPoint, yAxisPointOpp);
        }
        else
        {
            xAxis = new Line(xAxisPoint, -1.0/line.m);
            xAxisOpp = new Line(xAxisPointOpp, -1.0/line.m);
            yAxis = new Line(yAxisPoint, line.m);
            yAxisOpp = new Line(yAxisPointOpp, line.m);
        }

        if (markerDetail != null)
        {
            markerDetail.xAxis = xAxis;
            markerDetail.yAxis = yAxis;
            markerDetail.xAxisOpposite = xAxisOpp;
            markerDetail.yAxisOpposite = yAxisOpp;
        }
    }

    @Override
    protected void sortCode(MarkerCode.MarkerDetails details)
    {
        // sort by area
        Collections.sort(details.regions, new Comparator<Map<String, Object>>()
        {
            @Override
            public int compare(Map<String, Object> region1, Map<String, Object> region2)
            {
                return ((Double) region1.get(REGION_AREA)).compareTo((Double) region2.get(REGION_AREA));
            }
        });

        // label
        int count = 0;
        for (Map<String, Object> region : details.regions)
        {
            region.put(REGION_LABEL, (char)(65+count++));
        }

        // sort by left to right
        Collections.sort(details.regions, new Comparator<Map<String, Object>>()
        {
            @Override
            public int compare(Map<String, Object> region1, Map<String, Object> region2)
            {
                return ((Double) region1.get("x")).compareTo((Double) region2.get("x"));
            }
        });
    }

    @Override
    public void draw(MarkerCode marker, Mat image, List<MatOfPoint> contours, Mat hierarchy, Scalar markerColor, Scalar outlineColor, Scalar regionColor)
    {
        Scalar[] colours = new Scalar[5];
        colours[0] = new Scalar(255*0, 255*0, 255*1, 255); // red
        colours[1] = new Scalar(255*0, 255*0.75, 255*1, 255); // orange
        colours[2] = new Scalar(255*0, 255*1, 255*0, 255); // green
        colours[3] = new Scalar(255 * 1, 255 * 0, 255 * 0, 255); // blue
        colours[4] = new Scalar(255 * 1, 255 * 0, 255 * 1, 255); // purple

        for (MarkerCode.MarkerDetails details : marker.getMarkerDetails())
        {
            AreaOrientationMarkerDetails markerDetails = (AreaOrientationMarkerDetails) details;

            Imgproc.drawContours(image, contours, markerDetails.markerIndex, outlineColor, 3, 8, hierarchy, 0, new Point(0, 0));
            Imgproc.drawContours(image, contours, markerDetails.markerIndex, markerColor, 2, 8, hierarchy, 0, new Point(0, 0));
            Point p = Imgproc.boundingRect(contours.get(markerDetails.markerIndex)).tl();
            p.y+=18;

            int count = 0;
            for (Map<String, Object> regionDetail : markerDetails.regions)
            {
                // draw region
                Imgproc.drawContours(image, contours, (Integer) regionDetail.get(MarkerCode.MarkerDetails.REGION_INDEX), colours[count % 5], 1, 8, hierarchy, 0, new Point(0, 0));

                double areaAsPercentageOfSmallestRegionsArea = (Double) regionDetail.get(REGION_AREA) / (Double) markerDetails.regions.get(0).get(REGION_AREA);
                DecimalFormat df = new DecimalFormat("#.##");
                String str = df.format(areaAsPercentageOfSmallestRegionsArea);
                Core.putText(image, str, p, Core.FONT_HERSHEY_SIMPLEX, 0.5, outlineColor, 3);
                Core.putText(image, str, p, Core.FONT_HERSHEY_SIMPLEX, 0.5, colours[count++ % 5], 2);
                p.x += 3 + 3 + (str.length()-1)*12;
            }

            // draw line of gravity
            //ACXAOMarkerDetails* markerDetail = self.markerDetails[nodeIndex];
            Core.line(image, markerDetails.centerPoint, markerDetails.regionCenterPoint, markerColor, 3);

            // draw (& label) the redefined x & y axis
            Point origin = markerDetails.xAxis.getIntersectionWith(markerDetails.yAxis);
            Point endOfXAxis = markerDetails.xAxis.getIntersectionWith(markerDetails.yAxisOpposite);
            Point endOfYAxis = markerDetails.yAxis.getIntersectionWith(markerDetails.xAxisOpposite);

            Core.line(image, origin, endOfXAxis, markerColor, 3);
            Core.line(image, origin, endOfYAxis, markerColor, 3);

            Core.putText(image, "x", endOfXAxis, Core.FONT_HERSHEY_SIMPLEX, 0.5, outlineColor, 3);
            Core.putText(image, "x", endOfXAxis, Core.FONT_HERSHEY_SIMPLEX, 0.5, markerColor, 2);
            Core.putText(image, "y", endOfYAxis, Core.FONT_HERSHEY_SIMPLEX, 0.5, outlineColor, 3);
            Core.putText(image, "y", endOfYAxis, Core.FONT_HERSHEY_SIMPLEX, 0.5, markerColor, 2);

            // draw lines between x axis and the points on the region edge they were sorted by
            count = 0;
            for (Map<String, Object> regionDetail : markerDetails.regions)
            {
                Point regionSortPoint = (Point) regionDetail.get("sortPoint");
                Point pointOnXAxis = (Point) regionDetail.get("sortIntersectionPoint");

                Core.line(image, regionSortPoint, pointOnXAxis, colours[count++%5], 2);
            }
        }
    }
}
