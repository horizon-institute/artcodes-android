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
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerCodeFactory implements MarkerCode.MarkerDrawer {


    /** Possible error states that can be set when creating a MarkerCode */
    public enum DetectionStatus
    {
        unknown                 (0, "debug_unknown",                   new Scalar(0,0,0,0)),
        noSubContours           (1, "debug_noSubContours",             new Scalar(0,0,0,0)),
        tooManyEmptyRegions     (2, "debug_tooManyEmptyRegions",       new Scalar(255 * 1,    255 * 0,    255 * 0, 255)),
        nestedRegions           (3, "debug_nestedRegions",             new Scalar(255 * 1,    255 * 0.75, 255 * 0, 255)),
        numberOfRegions         (4, "debug_numberOfRegions",           new Scalar(255 * 1,    255 * 1,    255 * 0, 255)),
        numberOfDots            (5, "debug_numberOfDots",              new Scalar(255 * 0,    255 * 1,    255 * 0, 255)),
        checksum                (6, "debug_checksum",                  new Scalar(255 * 0,    255 * 1,    255 * 1, 255)),
        validationRegions       (7, "debug_validationRegions",         new Scalar(255 * 0,    255 * 0,    255 * 1, 255)),
        extensionSpecificError  (8, "debug_extensionSpecificError",    new Scalar(255 * 0.75, 255 * 0,    255 * 1, 255)),
        OK                      (9, "debug_OK",                        new Scalar(255 * 1,    255 * 0,    255 * 1, 255));

        private int index;
        private String key;
        private Scalar color;

        DetectionStatus(int index, String key, Scalar color) {
            this.index = index;
            this.key = key;
            this.color = color;
        }

        public int getIndex() {
            return index;
        }
        public String getStringKey() {
            return key;
        }
        public Scalar getColor() {
            return color;
        }
    }

    public void generateExtraFrameDetails(Mat thresholdedImage, List<MatOfPoint> contours, Mat hierarchy)
    {
    }

    public MarkerCode createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionStatus[] error, int errorIndex)
    {
        MarkerCode.MarkerDetails markerDetails = this.createMarkerDetailsForNode(nodeIndex, contours, hierarchy, experience, error, errorIndex);
        if (markerDetails != null)
        {
            error[errorIndex] = DetectionStatus.OK;
            return new MarkerCode(this.getCodeFor(markerDetails), markerDetails, this);
        }
        else
        {
            return null;
        }
    }

    protected MarkerCode.MarkerDetails createMarkerDetailsForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionStatus[] error, int errorIndex)
    {
        MarkerCode.MarkerDetails markerDetails = this.parseRegionsAt(nodeIndex, contours, hierarchy, experience, error, errorIndex);
        if (markerDetails!=null)
        {
            this.sortCode(markerDetails);
            if (!this.validate(markerDetails, experience, error, errorIndex))
            {
                markerDetails = null;
            }
        }
        return markerDetails;
    }

    protected static final int REGION_INVALID = -1;
    protected static final int REGION_EMPTY = 0;

    //indexes of leaf nodes in contour tree hierarchy.
    protected static final int NEXT_NODE = 0;
    protected static final int FIRST_NODE = 2;

    protected MarkerCode.MarkerDetails parseRegionsAt(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, Experience experience, DetectionStatus[] error, int errorIndex)
    {
        int currentRegionIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE];
        if (currentRegionIndex < 0)
        {
            error[errorIndex] = DetectionStatus.noSubContours;
            return null; // There are no regions.
        }

        int regionCount = 0;
        int emptyRegions = 0;
        List<Map<String, Object> > regions = null;
        Integer embeddedChecksumValue = null, embeddedChecksumRegionIndex = null;

        // Loop through the regions, verifing the value of each:
        for (;currentRegionIndex >= 0; currentRegionIndex = (int) hierarchy.get(0, currentRegionIndex)[NEXT_NODE])
        {
            final int regionValue = getRegionValue(currentRegionIndex, hierarchy, experience.getMaxRegionValue());
            if (regionValue == REGION_EMPTY)
            {
                if (++emptyRegions > experience.getMaxEmptyRegions())
                {
                    error[errorIndex] = DetectionStatus.tooManyEmptyRegions;
                    return null; // Too many empty regions.
                }
            }

            if (regionValue == REGION_INVALID)
            {
                // Not a normal region so look for embedded checksum:
                if (experience.getEmbeddedChecksum() && embeddedChecksumValue == null) // if we've not found it yet:
                {
                    embeddedChecksumValue = getEmbeddedChecksumValueForRegion(currentRegionIndex, hierarchy, experience);
                    if (embeddedChecksumValue != null)
                    {
                        embeddedChecksumRegionIndex = currentRegionIndex;
                        continue; // this is a checksum region, so continue looking for regions
                    }
                }

                error[errorIndex] = DetectionStatus.nestedRegions;
                return null; // Too many levels or dots.
            }

            if (++regionCount > experience.getMaxRegions())
            {
                error[errorIndex] = DetectionStatus.numberOfRegions;
                return null; // Too many regions.
            }

            // Add region value to code:
            if (regions == null)
            {
                regions = new ArrayList<>();
            }
            Map<String, Object> region = new HashMap<>();
            region.put(MarkerCode.MarkerDetails.REGION_INDEX, currentRegionIndex);
            region.put(MarkerCode.MarkerDetails.REGION_VALUE, regionValue);
            regions.add(region);
        }

        // Marker should have at least one non-empty branch. If all branches are empty then return false.
        if ((regionCount - emptyRegions) < 1)
        {
            error[errorIndex] = DetectionStatus.tooManyEmptyRegions;
            return null;
        }

        if (regions != null)
        {
            MarkerCode.MarkerDetails details = new MarkerCode.MarkerDetails();
            details.markerIndex = nodeIndex;
            details.regions = regions;
            details.embeddedChecksumRegionIndex = embeddedChecksumRegionIndex;
            details.embeddedChecksum = embeddedChecksumValue;
            return details;
        }

        return null;
    }

    /**
     * This function determines whether the input node is a valid region. It is a valid region if it contains zero or more dots.
     *
     * @param regionIndex Region index in the hierarchy.
     * @param hierarchy   This contains the contours or components hierarchy.
     * @param regionMaxValue The maximum number of dots allowed in a region.
     * @return Returns region status.
     */
    private static int getRegionValue(int regionIndex, Mat hierarchy, int regionMaxValue)
    {
        // Find the first dot index:
        double[] nodes = hierarchy.get(0, regionIndex);
        int currentDotIndex = (int) nodes[FIRST_NODE];
        if (currentDotIndex < 0)
        {
            return REGION_EMPTY; // There are no dots in this region.
        }

        // Count all the dots and check if they are leaf nodes in the hierarchy:
        int dotCount = 0;
        while (currentDotIndex >= 0)
        {
            if (verifyAsLeaf(currentDotIndex, hierarchy))
            {
                dotCount++;
                // Get next dot node:
                nodes = hierarchy.get(0, currentDotIndex);
                currentDotIndex = (int) nodes[NEXT_NODE];

                if (dotCount > regionMaxValue)
                {
                    return dotCount; // Too many dots.
                }
            }
            else
            {
                return REGION_INVALID; // Dot is not a leaf in the hierarchy.
            }
        }

        return dotCount;
    }

    /**
     * This functions determines if the node is a valid leaf. It is a valid leaf if it does not have any child nodes.
     */
    private static Boolean verifyAsLeaf(int nodeIndex, Mat hierarchy)
    {
        double[] nodes = hierarchy.get(0, nodeIndex);
        return nodes[FIRST_NODE] < 0;
    }

    private static Integer getEmbeddedChecksumValueForRegion(int regionIndex, Mat hierarchy, Experience experience)
    {
        // Find the first dot index:
        double[] nodes = hierarchy.get(0, regionIndex);
        int currentDotIndex = (int) nodes[FIRST_NODE];
        if (currentDotIndex < 0)
        {
            return null; // There are no dots in this region.
        }

        // Count all the dots and check if they are leaf nodes in the hierarchy:
        int dotCount = 0;
        while (currentDotIndex >= 0)
        {
            if (verifyAsDoubleLeaf(currentDotIndex, hierarchy, experience))
            {
                dotCount++;
            }
            else if (!experience.isRelaxedEmbeddedChecksumIgnoreNonHollowDots())
            {
                return -1; // Wrong number of levels.
            }
            // Get next dot node:
            nodes = hierarchy.get(0, currentDotIndex);
            currentDotIndex = (int) nodes[NEXT_NODE];
        }

        return dotCount;
    }

    private static Boolean verifyAsDoubleLeaf(int nodeIndex, Mat hierarchy, Experience experience)
    {
        double[] nodes = hierarchy.get(0, nodeIndex);
        return nodes[FIRST_NODE] >= 0 && // has a child node, and
                (hierarchy.get(0,(int)nodes[FIRST_NODE])[NEXT_NODE] < 0 || experience.isRelaxedEmbeddedChecksumIgnoreMultipleHollowSegments()) && //the child has no siblings, and
                verifyAsLeaf((int)nodes[FIRST_NODE], hierarchy);// the child is a leaf
    }

    /** Override this method to change the sorted order of the code. */
    protected void sortCode(MarkerCode.MarkerDetails details)
    {
        Collections.sort(details.regions, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> region1, Map<String, Object> region2) {
                return ((Integer)region1.get(MarkerCode.MarkerDetails.REGION_VALUE)).compareTo((Integer)region2.get(MarkerCode.MarkerDetails.REGION_VALUE));
            }
        });
    }

    /** Override this method to change validation method. */
    protected boolean validate(MarkerCode.MarkerDetails details, Experience experience, DetectionStatus[] error, int errorIndex)
    {
        //NSMutableString *strError = [[NSMutableString alloc] init];
        //NSArray *code = [details.regions valueForKey:REGION_VALUE];
        List<Integer> code = new ArrayList<>();
        for (Map<String, Object> region : details.regions)
        {
            code.add((Integer) region.get(MarkerCode.MarkerDetails.REGION_VALUE));
        }

        boolean result = experience.isValidMarker(code, details.embeddedChecksum, error, errorIndex);

        /*if ([strError rangeOfString:@"Too many dots"].location != NSNotFound)
        {
            error[0] = numberOfDots;
        }
        else if ([strError rangeOfString:@"checksum"].location != NSNotFound)
        {
            error[0] = checksum;
        }
        else if ([strError rangeOfString:@"Validation regions"].location != NSNotFound)
        {
            error[0] = validationRegions;
        }*/

        return result;
    }

    /** Override this method if your marker-code string representation is more complicated than an ordered list of numbers separated by colons. */
    protected String getCodeFor(MarkerCode.MarkerDetails details)
    {
        StringBuilder builder = new StringBuilder(details.regions.size()*2);
        for (Map<String, Object> region : details.regions)
        {
            builder.append(region.get(MarkerCode.MarkerDetails.REGION_VALUE));
            builder.append(':');
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    @Override
    public void draw(MarkerCode marker, Mat image, List<MatOfPoint> contours, Mat hierarchy, Scalar markerColor, Scalar outlineColor, Scalar regionColor) {

        for (MarkerCode.MarkerDetails details : marker.getMarkerDetails())
        {
            if (regionColor != null)
            {
                // draw regions
                for (Map<String, Object> region : details.regions)
                {
                    int currentRegionIndex = (Integer) region.get(MarkerCode.MarkerDetails.REGION_INDEX);
                    Imgproc.drawContours(image, contours, currentRegionIndex, outlineColor, 4);
                    Imgproc.drawContours(image, contours, currentRegionIndex, regionColor, 2);
                }
            }

            if (markerColor != null)
            {
                // draw marker outline
                Imgproc.drawContours(image, contours, details.markerIndex, outlineColor, 7);
                Imgproc.drawContours(image, contours, details.markerIndex, markerColor, 5);
            }
        }
    }

    /// Marker Debug methods:

    // TODO: Move these string to the values file.
    protected static final String[] DEBUG_ERROR_STRINGS = new String[] {"?", "No sub-contours", "Too many empty regions", "Nested regions", "Wrong number of regions", "Wrong number of dots", "Does not match checksum", "Does not match validation regions", "Extension specific error", "Marker found"};
    protected static final String[] DEBUG_ADDITIONAL_HELP_STRINGS = new String[] {"?", "", "There must not be more than %d empty regions", "Nested regions shown in red", "There must be %s regions, check no lines are broken", "There must be a maximum of %d dots in each region", "Check the number of dots or checksum setting", "Check number of dots found", "", ""};

    public String[] getDebugMessagesForErrorType(DetectionStatus errorType, Experience experience)
    {
        String additionalMessage = DEBUG_ADDITIONAL_HELP_STRINGS[errorType.getIndex()];

        // Some of the error strings tell the user about the number of dots/etc. required.
        if (errorType== DetectionStatus.tooManyEmptyRegions)
        {
            additionalMessage = String.format(additionalMessage, experience.getMaxEmptyRegions());
        }
        else if (errorType== DetectionStatus.numberOfRegions)
        {
            additionalMessage = String.format(additionalMessage,
                    (experience.getMinRegions()==experience.getMaxRegions()) ?
                            (experience.getMinRegions()+"") :
                            String.format("%d to %d", experience.getMinRegions(), experience.getMaxRegions()));
        }
        else if (errorType== DetectionStatus.numberOfDots)
        {
            additionalMessage = String.format(additionalMessage, experience.getMaxRegionValue());
        }

        return new String[] {DEBUG_ERROR_STRINGS[errorType.getIndex()], additionalMessage};
    }

    public void drawErrorDebug(int contourIndex, DetectionStatus errorType, Mat drawImage, List<MatOfPoint> contours, Mat hierarchy, Experience experience)
    {

        Imgproc.drawContours(drawImage, contours, contourIndex, errorType.getColor(), -1, 8, hierarchy, 1, new Point(0, 0));

        if (errorType== DetectionStatus.tooManyEmptyRegions || errorType== DetectionStatus.nestedRegions || errorType== DetectionStatus.numberOfRegions || errorType== DetectionStatus.numberOfDots || errorType== DetectionStatus.checksum || errorType== DetectionStatus.validationRegions)
        {
            int regionCount = 0, dotTotal = 0;
            for (int regionIndex = (int) hierarchy.get(0, contourIndex)[FIRST_NODE]; regionIndex>-1 && regionCount<experience.getMaxRegions()*10; regionIndex = (int)hierarchy.get(0, regionIndex)[NEXT_NODE])
            {
                int dotCount = 0;
                int firstDotIndex = (int) hierarchy.get(0, regionIndex)[FIRST_NODE];
                for (int dotIndex = firstDotIndex; dotIndex>-1 && dotCount<experience.getMaxRegionValue()*10; dotIndex = (int) hierarchy.get(0, dotIndex)[NEXT_NODE])
                {
                    ++dotCount;
                    if (errorType== DetectionStatus.nestedRegions &&hierarchy.get(0, dotIndex)[2] != -1) // if dot has children it is nested
                    {
                        Imgproc.drawContours(drawImage, contours, dotIndex, new Scalar(255,0,0,255), -1, 8, hierarchy, 1, new Point(0, 0));
                    }
                }
                dotTotal += dotCount;

                Point labelPoint = null;
                if (firstDotIndex > -1)
                {
                    // if a region has dots the label is placed on the first dot
                    labelPoint = contours.get(firstDotIndex).toArray()[0];
                }
                else
                {
                    // otherwise it is placed on the region edge.
                    labelPoint = contours.get(regionIndex).toArray()[0];
                }

                String str = null;
                if (errorType== DetectionStatus.numberOfRegions || (errorType== DetectionStatus.tooManyEmptyRegions && dotCount==0))
                {
                    str = ++regionCount+"";
                }
                else if (errorType== DetectionStatus.numberOfDots || errorType== DetectionStatus.checksum || errorType== DetectionStatus.validationRegions)
                {
                    str = dotCount+"";
                }

                if (str!=null)
                {
                    Core.putText(drawImage, str, labelPoint, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0, 255), 3);
                    Core.putText(drawImage, str, labelPoint, Core.FONT_HERSHEY_SIMPLEX, 0.5, errorType.getColor(), 2);
                }
            }
        }
    }
}
