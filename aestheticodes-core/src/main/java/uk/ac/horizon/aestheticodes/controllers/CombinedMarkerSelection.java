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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Scene;

public class CombinedMarkerSelection implements MarkerSelection
{
    private static final int REQUIRED = 5;

    private final Map<String, MarkerCode> occurrences = new HashMap<>();
    private String current = null;

    private final List<String> history = new ArrayList<>();
    private long lastAddedToHistory = 0;
    private boolean shouldClearHistoryOnReset = true;

    public void reset(MarkerDetector.Listener callback)
    {
        occurrences.clear();
        if (shouldClearHistoryOnReset)
        {
            history.clear();
        }
        if(current != null)
        {
            current = null;
            callback.markerChanged(null, null, this.historySize(), null);
        }
    }

    public void addMarkers(List<MarkerCode> markers, MarkerDetector.Listener callback, Experience experience, Scene scene)
    {
        // Take a copy of the history
        String oldHistory = joinStr(history, ",");

        long time = System.currentTimeMillis();

        List<MarkerCode> justAddedToHistory = new ArrayList<>();

        // Process markers detected on this frame
        for (MarkerCode markerCode : markers)
        {
            //increase occurrence if this marker is already in the list.
            MarkerCode existing = occurrences.get(markerCode.getCodeKey());
            if (existing != null)
            {
                // add to history (if it has passed the required occurrences on this frame)
                if (existing.getOccurrences() < REQUIRED && existing.getOccurrences() + markerCode.getOccurrences() >= REQUIRED)
                {
                    if (this.history.isEmpty() || System.currentTimeMillis()-this.lastAddedToHistory>=1000 || !existing.getCodeKey().equals(this.history.get(this.history.size()-1)))
                    {
                        history.add(existing.getCodeKey());
                        justAddedToHistory.add(markerCode);
                        this.lastAddedToHistory = time;
                        existing.setFirstDetected(time);
                    }
                }

                // Existing marker occurence: increase its occurence count
                existing.setOccurrences(markerCode.getOccurrences() + existing.getOccurrences());
                existing.setLastDetected(time);
            }
            else
            {
                // New marker occurence: add it to data structure
                occurrences.put(markerCode.getCodeKey(), markerCode);
            }
        }

        // Workout which markers have been detected and which have timed out:
        List<String> detected = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();
        for(MarkerCode marker: occurrences.values())
        {
            if(!markers.contains(marker))
            {
                marker.setOccurrences(Math.min(marker.getOccurrences() - 1, REQUIRED * 5));
                if (marker.getOccurrences() <= 0)
                {
                    toRemove.add(marker.getCodeKey());
                    continue;
                }
            }

            if (marker.getOccurrences() >= REQUIRED)
            {
                detected.add(marker.getCodeKey());
            }
        }
        for (String markerToRemove : toRemove)
        {
            occurrences.remove(markerToRemove);
        }

        String found = getMarkerFrom(detected, justAddedToHistory, experience);

        // create thumbnails

        List<Integer> thumbnails = new ArrayList<>();
        for (MarkerCode newMarker : justAddedToHistory)
        {
            Integer index = newMarker.getComponentIndexs().get(0);
            if (index!=null)
            {
                thumbnails.add(index);
            }
        }

        justAddedToHistory.clear();

        boolean historyHasChanged = !oldHistory.equals(joinStr(history, ","));

        if (found == null)
        {
            if(current != null || historyHasChanged || !thumbnails.isEmpty())
            {
                current = null;
                callback.markerChanged(null, thumbnails, this.historySize(), scene);
            }
        }
        else
        {
            if(current == null || !current.equals(found) || historyHasChanged || !thumbnails.isEmpty())
            {
                current = found;
                if (experience.getMarkers().get(current)!=null)
                    shouldClearHistoryOnReset = experience.getMarkers().get(current).getResetHistoryOnOpen();
                callback.markerChanged(found, thumbnails, this.historySize(), scene);
            }
        }
    }

    private String getMarkerFrom(final List<String> detected, List<? extends Object> justAddedToHistory, final Experience experience)
    {
        String result = getGroupMarkerFrom(detected, occurrences, experience);
        if (result==null)
        {
            result = getSequentialMarkerFrom(history, justAddedToHistory, experience);
            if (result==null)
            {
                result = getStandardMarkerFrom(detected, occurrences, experience);
            }
        }
        else
        {
            // prune history anyway:
            getSequentialMarkerFrom(history, justAddedToHistory, experience);
        }
        return result;
    }

    /**
     * Search for group codes (or "pattern groups") in the detected codes. This will only return
     * group codes set in the experience.
     * @param detected
     * @param experience
     * @return
     */
    private static String getGroupMarkerFrom(final List<String> detected, final Map<String, MarkerCode> occurrences, final Experience experience) {
        if (experience != null) {
            // Search for pattern groups
            // By getting every combination of the currently detected markers and checking if they exist in the experience (biggest groups first, groups must include at least 2 markers)
            if (detected != null && detected.size() > 1) {
                List<Set<List<String>>> combinations = new ArrayList<>();
                combinationsOfStrings(detected, detected.size(), combinations);
                for (int i = combinations.size() - 1; i >= 1; --i) {
                    List<String> mostRecentGroup = null;
                    String mostRecentGroupStr = null;
                    for (List<String> code : combinations.get(i)) {
                        String codeStr = joinStr(code, "+");
                        if (experience.getMarkers().get(codeStr)!=null && markerDetectionTimesOverlap(code, occurrences) && getMostRecentDetectionTime(code, mostRecentGroup, occurrences)>getMostRecentDetectionTime(mostRecentGroup, code, occurrences)) {
                            mostRecentGroup = code;
                            mostRecentGroupStr = codeStr;
                        }
                    }
                    if (mostRecentGroup!=null)
                    {
                        return mostRecentGroupStr;
                    }
                }
            }
        }
        return null;
    }

    private static long getMostRecentDetectionTime(List<String> codes, List<String> excluding, Map<String, MarkerCode> occurrences)
    {
        long mostRecentTime = 0;
        if (codes!=null)
        {
            for (String codeStr : codes)
            {
                if (excluding == null || !excluding.contains(codeStr))
                {
                    MarkerCode code = occurrences.get(codeStr);
                    if (code != null && code.getLastDetected() > mostRecentTime)
                    {
                        mostRecentTime = code.getLastDetected();
                    }
                }
            }
        }
        return mostRecentTime;
    }

    private static boolean markerDetectionTimesOverlap(List<String> codes, Map<String, MarkerCode> occurrences)
    {
        for (int i=0; i<codes.size()-1; ++i)
        {
            MarkerCode code1 = occurrences.get(codes.get(i));
            boolean overlapFound = false;
            for (int j=i+1; j<codes.size(); ++j)
            {
                MarkerCode code2 = occurrences.get(codes.get(j));
                overlapFound = doTimesOverlap(code1.getFirstDetected(), code1.getLastDetected(), code2.getFirstDetected(), code2.getLastDetected());
                if (overlapFound)
                {
                    break;
                }
            }
            if (!overlapFound)
            {
                return false;
            }
        }
        return true;
    }

    private static boolean doTimesOverlap(long firstDetected1, long lastDetected1, long firstDetected2, long lastDetected2)
    {
        return (firstDetected1 < lastDetected2)  &&  (lastDetected1 > firstDetected2);
    }

    /**
     * Get all the combinations of objects up to a maximum size for the combination and add it to the result List.
     * E.g. combinationsOfStrings([1,3,2], 2, []) changes the result array to [([1],[2],[3]),([1,2],[1,3],[2,3])] where () denotes a Set and [] denotes a List.
     */
    private static void combinationsOfStrings(List<String> strings, int maxCombinationSize, List<Set<List<String>>> result)
    {
        if (maxCombinationSize ==1)
        {
            Set<List<String>> resultForN = new HashSet<>();
            for (String code : strings)
            {
                List<String> tmp = new ArrayList<>();
                tmp.add(code);
                resultForN.add(tmp);
            }
            result.add(resultForN);
        }
        else if (maxCombinationSize == strings.size())
        {
            combinationsOfStrings(strings, maxCombinationSize - 1, result);
            Set<List<String>> resultForN = new HashSet<>();
            Collections.sort(strings);
            resultForN.add(strings);
            result.add(resultForN);
        }
        else
        {
            Set<List<String>> resultForN = new HashSet<>();
            combinationsOfStrings(strings, maxCombinationSize - 1, result);
            Set<List<String>> base = result.get(result.size()-1);

            for (String code : strings)
            {
                for (List<String> setMinus1 : base)
                {
                    if (!setMinus1.contains(code))
                    {
                        List<String> aResult = new ArrayList<>(setMinus1);
                        aResult.add(code);
                        Collections.sort(aResult);
                        resultForN.add(aResult);
                    }
                }
            }


            result.add(resultForN);
        }
    }

    /**
     * Search for sequential codes (or "pattern paths") in detection history. This method may
     * remove items from history that do not match the beginning of any sequential code in the
     * experience and will only return a code from the experience.
     * @param history
     * @param justAddedToHistory
     * @param experience
     * @return
     */
    private static String getSequentialMarkerFrom(List<String> history, List<? extends Object> justAddedToHistory, final Experience experience)
    {
        if (experience != null)
        {
            // Search for sequential actions in history
            // by creating history sub-lists and checking if any codes in the experience match.
            // e.g. if history=[A,B,C,D] check sub-lists [A,B,C,D], [B,C,D], [C,D].
            if (history != null && history.size() > 0)
            {
                boolean foundPrefix = false;
                int start = 0;
                while (start < history.size())
                {
                    List<String> subList = history.subList(start, history.size());
                    String joinedString = joinStr(subList, ">");
                    if (subList.size() != 1 && experience.getMarkers().get(joinedString) != null)
                    {
                        // Case 1: The history sublist is a sequential code in the experience.
                        return joinedString;
                    }
                    else if (!foundPrefix && !experience.hasCodeStartingWith(joinedString + ">"))
                    {
                        // Case 2: No sequential codes in the experience start with the history sublist (as well as previous history sublists).
                        // So remove the first part of it from history
                        // This ensures that history never grows longer than the longest code
                        if (justAddedToHistory.size() == history.size()) {
                            justAddedToHistory.remove(0);
                        }
                        history.remove(0);
                        start = 0;
                    }
                    else
                    {
                        // Case 3: Sequential codes in the experience start with the history sublist (or a previous history sublist).
                        foundPrefix = true;
                        start++;
                    }
                }
            }
        }
        return null;
    }

    private static String joinStr(List<String> strings, String joiner)
    {
        String result = new String("");
        for (int i=0; i<strings.size(); ++i)
        {
            result += (i==0?"":joiner) + strings.get(i);
        }
        return result;
    }

    /**
     * Search for the single marker with the most occurrences that is in the experience, or just the highest occurrences if none are in the experience.
     * @param detected
     * @param occurrences
     * @param experience
     * @return
     */
    private static String getStandardMarkerFrom(final List<String> detected, final Map<String, MarkerCode> occurrences, final Experience experience)
    {
        MarkerCode result = null;
        boolean resultIsInExperience = false;
        for (String code : detected)
        {
            MarkerCode marker = occurrences.get(code);
            boolean markerIsInExperience = experience==null || experience.getMarkers().get(code)!=null;
            if (result==null || (!resultIsInExperience && markerIsInExperience) || (resultIsInExperience==markerIsInExperience && marker.getOccurrences()>result.getOccurrences()))
            {
                result = marker;
                resultIsInExperience = markerIsInExperience;
            }
        }

        return result==null ? null : result.getCodeKey();
    }

    public int historySize()
    {
        return this.history.size();
    }
}
