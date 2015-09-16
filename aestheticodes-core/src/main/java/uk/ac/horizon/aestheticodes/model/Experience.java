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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.aestheticodes.controllers.MarkerCodeFactory;
import uk.ac.horizon.aestheticodes.controllers.MarkerCodeFactoryAreaOrderExtension;
import uk.ac.horizon.aestheticodes.controllers.MarkerCodeFactoryAreaOrientationExtension;
import uk.ac.horizon.aestheticodes.controllers.MarkerCodeFactoryOrientationAreaExtension;
import uk.ac.horizon.aestheticodes.controllers.MarkerCodeFactoryTouchingExtension;

public class Experience
{
    public enum Operation
    {
        create, retrieve, update, deleted, add, remove
    }

    public enum Threshold
    {
        temporalTile, resize
    }

    private final Map<String, Marker> markers = new HashMap<>();

    private String id;
    private String name;
    private String icon;
    private String image;
    private String description;
    private int version = 1;
    private String ownerID;
    private String callback;

    private String location;
    private Position position;
    private Long startDate;
    private Long endDate;

    private String origin;
    private String originalID;
    private int originalVersion;

    private Operation op = null;
    private int minRegions = 5;
    private int maxRegions = 5;
    private int maxEmptyRegions = 0;
    private int maxRegionValue = 6;
    private int validationRegions = 0;
    private int validationRegionValue = 1;
    private int checksumModulo = 3;
    private boolean embeddedChecksum = false;
    private Threshold threshold = Threshold.temporalTile;

    // addition values
    private String startUpURL = null;
    private boolean relaxedEmbeddedChecksumIgnoreMultipleHollowSegments = false;
    private boolean relaxedEmbeddedChecksumIgnoreNonHollowDots = false;

    public String getStartUpURL()
    {
        return this.startUpURL;
    }

    public void setStartUpURL(String startUpURL)
    {
        this.startUpURL = startUpURL;
    }

    public boolean isRelaxedEmbeddedChecksumIgnoreMultipleHollowSegments()
    {
        return this.relaxedEmbeddedChecksumIgnoreMultipleHollowSegments;
    }

    public boolean isRelaxedEmbeddedChecksumIgnoreNonHollowDots()
    {
        return this.relaxedEmbeddedChecksumIgnoreNonHollowDots;
    }

    public void setRelaxedEmbeddedChecksumIgnoreMultipleHollowSegments(boolean b)
    {
        this.relaxedEmbeddedChecksumIgnoreMultipleHollowSegments = b;
    }

    public void setRelaxedEmbeddedChecksumIgnoreNonHollowDots(boolean b)
    {
        this.relaxedEmbeddedChecksumIgnoreNonHollowDots = b;
    }

    public Experience()
    {
    }

    public void add(Marker marker)
    {
        markers.put(marker.getCode(), marker);
    }

    /**
     * Delete a marker from the list of markers.
     *
     * @param code The code of the marker to delete.
     * @return True if a marker was deleted, false if the given code was not found.
     */
    public boolean deleteMarker(String code)
    {
        if (this.markers.containsKey(code))
        {
            this.markers.remove(code);
            return true;
        }
        else
        {
            return false;
        }
    }

    public String getCallback()
    {
        return callback;
    }

    public void setCallback(String callback)
    {
        this.callback = callback;
    }

    public int getChecksumModulo()
    {
        return checksumModulo;
    }

    public void setChecksumModulo(int checksumModulo)
    {
        this.checksumModulo = checksumModulo;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean getEmbeddedChecksum()
    {
        return embeddedChecksum;
    }

    public void setEmbeddedChecksum(boolean embeddedChecksum)
    {
        this.embeddedChecksum = embeddedChecksum;
    }

    public Long getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Long endDate)
    {
        this.endDate = endDate;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getMarkerError(List<Integer> markerCodes, Integer embeddedChecksum)
    {
        return getMarkerError(markerCodes, embeddedChecksum, new MarkerCodeFactory.DetectionStatus[1], 0);
    }

    public String getMarkerError(List<Integer> markerCodes, Integer embeddedChecksum, MarkerCodeFactory.DetectionStatus[] error, int errorIndex)
    {
        if (markerCodes == null)
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.unknown;
            return "No Code";
        }
        else if (markerCodes.size() < minRegions)
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.numberOfRegions;
            return "Marker too Short";
        }
        else if (markerCodes.size() > maxRegions)
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.numberOfRegions;
            return "Marker too Long";
        }
        else if (!hasValidNumberofEmptyRegions(markerCodes))
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.tooManyEmptyRegions;
            return "Incorrect Empty Regions";
        }

        for (Integer value : markerCodes)
        {
            //check if leaves are with in accepted range.
            if (value > maxRegionValue)
            {
                error[errorIndex] = MarkerCodeFactory.DetectionStatus.numberOfDots;
                return value + " is too Big";
            }
        }

        if (embeddedChecksum == null && !hasValidChecksum(markerCodes))
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.checksum;
            return "Region Total not Divisable by " + checksumModulo;
        }
        else if (this.embeddedChecksum && embeddedChecksum != null && !hasValidEmbeddedChecksum(markerCodes, embeddedChecksum))
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.checksum;
            return "Region Total not Divisable by " + embeddedChecksum.toString();
        }
        else if (!this.embeddedChecksum && embeddedChecksum != null)
        {
            // Embedded checksum is turned off yet one was provided to this function (this should never happen unless the settings are changed in the middle of detection)
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.unknown;
            return "Embedded checksum markers are not valid.";
        }


        if (!hasValidationRegions(markerCodes))
        {
            error[errorIndex] = MarkerCodeFactory.DetectionStatus.validationRegions;
            return validationRegions + " Regions of " + validationRegionValue + " Required";
        }

        return null;
    }

    public String getMarkerError(String marker, boolean partial)
    {
        String[] values = marker.split(":");
        if (!partial)
        {
            if (values.length < minRegions)
            {
                return "Marker too Short";
            }
        }
        else if (marker.endsWith(":"))
        {
            if (values.length == maxRegions || marker.endsWith("::"))
            {
                return "Missing Region Value";
            }
        }

        if (values.length > maxRegions)
        {
            return "Marker too Long";
        }

        int prevValue = 0;
        List<Integer> codes = new ArrayList<>();
        for (int index = 0; index < values.length; index++)
        {
            String value = values[index];
            try
            {
                int codeValue = Integer.parseInt(value);
                if (codeValue > maxRegionValue)
                {
                    return value + " too Large";
                }
                else if (codeValue < prevValue)
                {
                    if (!marker.endsWith(":") && index < values.length - 1)
                    {
                        return value + " is larger than " + prevValue;
                    }
                }

                codes.add(codeValue);

                prevValue = codeValue;
            }
            catch (Exception e)
            {
                return value + " is Not a Number";
            }
        }

        return getMarkerError(codes, null);
    }

    public Map<String, Marker> getMarkers()
    {
        return markers;
    }

    public int getMaxEmptyRegions()
    {
        return maxEmptyRegions;
    }

    public void setMaxEmptyRegions(int maxEmptyRegions)
    {
        this.maxEmptyRegions = maxEmptyRegions;
    }

    public int getMaxRegionValue()
    {
        return maxRegionValue;
    }

    public void setMaxRegionValue(int maxRegionValue)
    {
        this.maxRegionValue = maxRegionValue;
    }

    public int getMaxRegions()
    {
        return maxRegions;
    }

    public void setMaxRegions(int maxRegions)
    {
        this.maxRegions = maxRegions;
    }

    public int getMinRegions()
    {
        return minRegions;
    }

    public void setMinRegions(int minRegions)
    {
        this.minRegions = minRegions;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNextUnusedMarker()
    {
        for (int size = minRegions; size <= maxRegions; size++)
        {
            final List<Integer> marker = new ArrayList<>();
            for (int index = 0; index < size; index++)
            {
                marker.add(1);
            }

            while (true)
            {
                if (isValidMarker(marker, null))
                {
                    StringBuilder result = new StringBuilder();
                    for (int index = 0; index < size; index++)
                    {
                        if (index != 0)
                        {
                            result.append(":");
                        }
                        result.append(marker.get(index));
                    }

                    String code = result.toString();
                    if (!markers.containsKey(code))
                    {
                        return code;
                    }
                }

                for (int i = (size - 1); i >= 0; i--)
                {
                    int value = marker.get(i) + 1;
                    for (int x = i; x < size; x++)
                    {
                        marker.set(x, value);
                    }
                    if (value <= maxRegionValue)
                    {
                        break;
                    }
                    else if (i == 0)
                    {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    public Operation getOp()
    {
        return op;
    }

    public void setOp(Operation op)
    {
        this.op = op;
    }

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    public String getOriginalID()
    {
        return originalID;
    }

    public void setOriginalID(String originalID)
    {
        this.originalID = originalID;
    }

    public String getOwnerID()
    {
        return ownerID;
    }

    public void setOwnerID(String ownerID)
    {
        this.ownerID = ownerID;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    public Long getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Long startDate)
    {
        this.startDate = startDate;
    }

    public Threshold getThreshold()
    {
        return threshold;
    }

    public int getValidationRegionValue()
    {
        return validationRegionValue;
    }

    public void setValidationRegionValue(int validationRegionValue)
    {
        this.validationRegionValue = validationRegionValue;
    }

    public int getValidationRegions()
    {
        return validationRegions;
    }

    public void setValidationRegions(int validationRegions)
    {
        this.validationRegions = validationRegions;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public boolean isValidMarker(List<Integer> markerCodes, Integer embeddedChecksum)
    {
        return isValidMarker(markerCodes, embeddedChecksum, new MarkerCodeFactory.DetectionStatus[1], 0);
    }

    public boolean isValidMarker(List<Integer> markerCodes, Integer embeddedChecksum, MarkerCodeFactory.DetectionStatus[] error, int errorIndex)
    {
        return getMarkerError(markerCodes, embeddedChecksum, error, errorIndex) == null;
    }

    public boolean isValidMarker(String marker, boolean partial)
    {
        String[] values = marker.split(":");
        if (!partial)
        {
            if (values.length < minRegions)
            {
                return false;
            }
        }
        else if (marker.endsWith(":"))
        {
            if (values.length == maxRegions)
            {
                return false;
            }
            else if (marker.endsWith("::"))
            {
                return false;
            }
        }

        if (values.length > maxRegions)
        {
            return false;
        }

        int prevValue = 0;
        List<Integer> codes = new ArrayList<>();
        for (int index = 0; index < values.length; index++)
        {
            String value = values[index];
            try
            {
                int codeValue = Integer.parseInt(value);
                if (codeValue > maxRegionValue)
                {
                    return false;
                }
                else if (codeValue < prevValue)
                {
                    if (marker.endsWith(":") || index < values.length - 1)
                    {
                        return false;
                    }
                }

                codes.add(codeValue);

                prevValue = codeValue;
            }
            catch (Exception e)
            {
                return false;
            }
        }

        return partial || isValidMarker(codes, null);
    }

    /**
     * This function divides the total number of leaves in the marker by the
     * value given in the checksumModulo preference. Code is valid if the modulo is 0.
     *
     * @return true if the number of leaves are divisible by the checksumModulo value
     * otherwise false.
     */
    private boolean hasValidChecksum(List<Integer> markerCodes)
    {
        if (checksumModulo <= 1)
        {
            return true;
        }
        int numberOfLeaves = 0;
        for (int code : markerCodes)
        {
            numberOfLeaves += code;
        }
        return (numberOfLeaves % checksumModulo) == 0;
    }

    private boolean hasValidEmbeddedChecksum(List<Integer> code, Integer embeddedChecksum)
    {
        // Find weighted sum of code, e.g. 1:1:2:4:4 -> 1*1 + 1*2 + 2*3 + 4*4 + 4*5 = 45
        int weightedSum = 0;
        for (int i = 0; i < code.size(); ++i)
        {
            weightedSum += code.get(i) * (i + 1);
        }
        return embeddedChecksum == (weightedSum % 7 == 0 ? 7 : weightedSum % 7);
    }

    private boolean hasValidNumberofEmptyRegions(List<Integer> marker)
    {
        int empty = 0;
        for (Integer value : marker)
        {
            if (value == 0)
            {
                empty++;
            }
        }
        return maxEmptyRegions >= empty;
    }

    /**
     * It checks the number of validation branches as given in the preferences.
     * The code is valid if the number of branches which contains the validation
     * code are equal or greater than the number of validation branches
     * mentioned in the preferences.
     *
     * @return true if the number of validation branches are >= validation
     * branch value in the preference otherwise it returns false.
     */
    private boolean hasValidationRegions(List<Integer> markerCodes)
    {
        if (validationRegions <= 0)
        {
            return true;
        }
        int validationRegionCount = 0;
        for (int code : markerCodes)
        {
            if (code == validationRegionValue)
            {
                validationRegionCount++;
            }
        }
        return validationRegionCount >= validationRegions;
    }

    private double hueShift = 0;
    private List<Object> greyscaleOptions = null;
    private boolean invertGreyscale = false;

    public double getHueShift()
    {
        return this.hueShift;
    }

    public void setHueShift(double hueShift)
    {
        this.hueShift = hueShift;
    }

    public boolean getInvertGreyscale()
    {
        return this.invertGreyscale;
    }

    public void setInvertGreyscale(boolean invertGreyscale)
    {
        this.invertGreyscale = invertGreyscale;
    }

    public List<Object> getGreyscaleOptions()
    {
        return this.greyscaleOptions;
    }

    public void setGreyscaleOptions(List<Object> greyscaleOptions)
    {
        this.greyscaleOptions = greyscaleOptions;
    }

    public Greyscaler getGreyscaler()
    {
        return Greyscaler.getGreyscaler(this.getHueShift(), this.getGreyscaleOptions(), this.getInvertGreyscale());
    }

    public MarkerCodeFactory getMarkerCodeFactory()
    {
        if (this.description != null)
        {
            if (this.description.contains("AREA4321"))
            {
                return new MarkerCodeFactoryAreaOrderExtension();
            }
            else if (this.description.contains("AO4321"))
            {
                return new MarkerCodeFactoryAreaOrientationExtension();
            }
            else if (this.description.contains("OA4321"))
            {
                return new MarkerCodeFactoryOrientationAreaExtension();
            }
            else if (this.description.contains("TOUCH4321"))
            {
                return new MarkerCodeFactoryTouchingExtension();
            }
        }

        return new MarkerCodeFactory();
    }

    /**
     * This method checks if this experience contains a code that starts with the given string.
     * This is useful for checking for partial recognition of sequential codes.
     * Note: The implementation is probably not suitable for experiences with LOTS of codes.
     *
     * @param codeSubstring
     * @return
     */
    public boolean hasCodeStartingWith(String codeSubstring)
    {
        for (String code : this.markers.keySet())
        {
            if (code.indexOf(codeSubstring) == 0)
            {
                return true;
            }
        }
        return false;
    }
}
