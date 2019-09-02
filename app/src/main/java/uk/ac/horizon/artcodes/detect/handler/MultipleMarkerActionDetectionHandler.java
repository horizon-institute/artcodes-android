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

package uk.ac.horizon.artcodes.detect.handler;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.horizon.artcodes.detect.handler.ActionDetectionHandler;
import uk.ac.horizon.artcodes.detect.handler.MarkerCodeDetectionHandler;
import uk.ac.horizon.artcodes.detect.marker.Marker;
import uk.ac.horizon.artcodes.detect.marker.MarkerWithEmbeddedChecksum;
import uk.ac.horizon.artcodes.drawer.MarkerDrawer;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.MarkerImage;

/**
 * Detects single marker, group and sequence actions. Provides images of detected markers and
 * indicates possible future actions based on the current state.
 *
 * Consider this a reference implementation that could be improved.
 */
public class MultipleMarkerActionDetectionHandler extends MarkerCodeDetectionHandler
{
    /**
     * A class that holds the details of a marker's detection state
     */
    private static class MarkerDetectionRecord {
        static int instanceCount = 0;
        final int instanceId;
        final String code;
        final Marker marker;
        long firstDetected;
        long lastDetected;
        int count;
        MarkerImage markerImage;

        public MarkerDetectionRecord(Marker marker)
        {
            this.marker = marker;
            this.code = marker.toString();
            this.count = 0;
            this.instanceId = instanceCount++;
        }

        public MarkerDetectionRecord clone(Marker marker)
        {
            MarkerDetectionRecord clone = new MarkerDetectionRecord(marker==null?this.marker:marker);
            clone.firstDetected = this.firstDetected;
            clone.lastDetected = this.lastDetected;
            clone.count = this.count;
            clone.markerImage = this.markerImage;
            return clone;
        }

        @Override
        public int hashCode()
        {
            return this.instanceId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof MarkerDetectionRecord)
            {
                return this.instanceId == ((MarkerDetectionRecord) o).instanceId;
            }
            return false;
        }

        @Override
        public String toString()
        {
            return "<#"+instanceId+" "+code+" x"+count+">";
        }
    }

    protected final ActionDetectionHandler markerActionHandler;
    protected final Experience experience;
    protected final MarkerDrawer markerDrawer;


    protected static final int REQUIRED = 5;
    protected static final int MAX = REQUIRED*4;

    protected long lastAddedToHistory = 0;
    protected boolean shouldClearHistoryOnReset = true;

    protected List<MarkerDetectionRecord> mDetectionHistory = new ArrayList<>();
    protected List<String> mCodesDetected = new ArrayList<>();
    protected Map<String, MarkerDetectionRecord> mActiveMarkerRecoreds = new HashMap<>();

    public MultipleMarkerActionDetectionHandler(ActionDetectionHandler markerActionHandler, Experience experience, MarkerDrawer markerDrawer)
    {
        super(experience, null);
        this.markerActionHandler = markerActionHandler;
        this.experience = experience;
        this.markerDrawer = markerDrawer;
    }

    @Override
    public void onMarkersDetected(Collection<Marker> markers, ArrayList<MatOfPoint> contours, Mat hierarchy, Size sourceImageSize)
    {
        addMarkers(markers, contours, hierarchy, sourceImageSize);
        actOnMarkers();
    }

    private MarkerImage createImageForMarker(Marker marker, ArrayList<MatOfPoint> contours, Mat hierarchy, Size sourceImageSize)
    {
        if (marker != null)
        {
            final Rect boundingRect = Imgproc.boundingRect(contours.get(marker.markerIndex));
            final Mat thumbnailMat = this.markerDrawer.drawMarker(marker, contours, hierarchy, boundingRect, null);
            final Bitmap thumbnail = Bitmap.createBitmap(thumbnailMat.width(), thumbnailMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(thumbnailMat, thumbnail);
            return new MarkerImage(marker.toString(), thumbnail, (float) (boundingRect.tl().x / sourceImageSize.width), (float) (boundingRect.tl().y / sourceImageSize.height), (float) (boundingRect.width / sourceImageSize.width), (float) (boundingRect.height / sourceImageSize.height));
        }
        return null;
    }

    public void reset()
    {
        mActiveMarkerRecoreds.clear();
        mCodesDetected.clear();
        if (shouldClearHistoryOnReset)
        {
            mDetectionHistory.clear();
        }
        existingAction = null;
        existingThumbnails = null;
        existingFutureAction = null;
        this.markerActionHandler.onMarkerActionDetected(null, null, null);
    }

    public void addMarkers(Collection<Marker> markers, ArrayList<MatOfPoint> contours, Mat hierarchy, Size sourceImageSize)
    {
        long time = System.currentTimeMillis();

        // Process markers detected on this frame
        for (Marker marker : markers)
        {
            String code = marker.toString();

            MarkerDetectionRecord markerDetectionRecord = mActiveMarkerRecoreds.get(code);
            if (markerDetectionRecord == null)
            {
                // New marker: add it to data structure
                markerDetectionRecord = new MarkerDetectionRecord(marker);
                mActiveMarkerRecoreds.put(code, markerDetectionRecord);
            }

            int countIncrease = marker instanceof MarkerWithEmbeddedChecksum ? REQUIRED-1 : 1;
            // add to history (if it has passed the required count on this frame)
            if (markerDetectionRecord.count < REQUIRED && markerDetectionRecord.count + countIncrease >= REQUIRED)
            {
                // don't add duplicates to history unless enough time has passed
                if (this.mDetectionHistory.isEmpty() || System.currentTimeMillis() - this.lastAddedToHistory >= 1000 || !code.equals(this.mDetectionHistory.get(this.mDetectionHistory.size() - 1).code))
                {
                    if (markerDetectionRecord.markerImage!=null)
                    {
                        // if second time this marker is detected
                        // create new entry and leave old one in history
                        markerDetectionRecord.markerImage.newDetection = false;
                        markerDetectionRecord.markerImage.detectionActive = false;
                        markerDetectionRecord = markerDetectionRecord.clone(marker);
                        mActiveMarkerRecoreds.put(code, markerDetectionRecord);
                    }
                    markerDetectionRecord.firstDetected = time;
                    mDetectionHistory.add(markerDetectionRecord);
                    this.lastAddedToHistory = time;
                    mCodesDetected.add(markerDetectionRecord.code);
                }
                markerDetectionRecord.markerImage = createImageForMarker(marker, contours, hierarchy, sourceImageSize);
                markerDetectionRecord.markerImage.newDetection = true;
            }
            else if (markerDetectionRecord.markerImage != null)
            {
                markerDetectionRecord.markerImage.newDetection = false;
            }

            // increase its count
            markerDetectionRecord.count = Math.min(markerDetectionRecord.count + countIncrease, MAX);
            markerDetectionRecord.lastDetected = time;
        }

        // Workout which markers have timed out:
        List<String> toRemove = new ArrayList<>();
        for(MarkerDetectionRecord markerRecord: mActiveMarkerRecoreds.values())
        {
            if(!markers.contains(markerRecord.marker))
            {
                if (markerRecord.count == REQUIRED)
                {
                    mCodesDetected.remove(markerRecord.code);
                    if (markerRecord.markerImage != null)
                    {
                        markerRecord.markerImage.detectionActive = false;
                        markerRecord.markerImage.newDetection = false;
                    }
                }
                else if (markerRecord.count <= 1)
                {
                    toRemove.add(markerRecord.code);
                    continue;
                }
                markerRecord.count = markerRecord.count - 1;
            }
        }
        for (String markerToRemove : toRemove)
        {
            mActiveMarkerRecoreds.remove(markerToRemove);
        }
        Collections.sort(mCodesDetected);
    }

    private void actOnMarkers()
    {
        final String standardCode =  getStandardCode();
        final Action action = getActionFor(standardCode);

        final Action sequentialAction = getActionFor(getSequentialCode());
        final Action futureSequentialAction = getPossibleFutureSequentialActionFor(sequentialAction==null?action:sequentialAction, standardCode);
        if (sequentialAction!=null)
        {
            sendIfResultChanged(sequentialAction, futureSequentialAction, getImagesForAction(futureSequentialAction));
            return;
        }

        final Action groupAction = getActionFor(getGroupCode());
        final Action futureGroupAction = getPossibleFutureGroupActionFor(groupAction==null?action:groupAction);
        if (groupAction!=null)
        {
            sendIfResultChanged(groupAction, futureGroupAction, getImagesForAction(futureGroupAction));
            return;
        }

        final Action futureAction = futureSequentialAction!=action?futureSequentialAction:futureGroupAction;
        sendIfResultChanged(action, futureAction, getImagesForAction(futureAction));
    }

    private Action existingAction = null, existingFutureAction = null;
    private List<MarkerImage> existingThumbnails = null;
    private void sendIfResultChanged(Action action, Action futureAction, List<MarkerImage> thumbnails)
    {
        if (((existingAction!=null && !existingAction.equals(action)) || (action!=null && !action.equals(existingAction))) ||
                ((existingThumbnails!=null && !existingThumbnails.equals(thumbnails)) || (thumbnails!=null && !thumbnails.equals(existingThumbnails))) ||
                ((existingFutureAction!=null && !existingFutureAction.equals(futureAction)) || ((futureAction!=null && !futureAction.equals(existingFutureAction)))))
        {
            this.existingAction = action;
            this.existingThumbnails = thumbnails;
            this.existingFutureAction = futureAction;
            this.markerActionHandler.onMarkerActionDetected(action, futureAction, thumbnails);
        }

    }

    private List<MarkerImage> getImagesForAction(Action action)
    {
        if (action!=null)
        {
            List<MarkerImage> result = new ArrayList<>(action.getCodes().size());
            if (action.getMatch() == Action.Match.any)
            {
                for (String code : action.getCodes())
                {
                    MarkerDetectionRecord record = mActiveMarkerRecoreds.get(code);
                    if (record != null && record.markerImage != null && record.markerImage.detectionActive)
                    {
                        result.add(record.markerImage);
                        return result;
                    }
                }
            }
            else if (action.getMatch() == Action.Match.all)
            {
                for (String code : action.getCodes())
                {
                    MarkerDetectionRecord record = mActiveMarkerRecoreds.get(code);
                    if (record != null && record.markerImage != null && record.markerImage.detectionActive)
                    {
                        result.add(record.markerImage);
                    }
                    else
                    {
                        result.add(null);
                    }
                }
                return result;
            }
            else if (action.getMatch() == Action.Match.sequence)
            {
                List<String> historyAsStrings = new ArrayList<>();
                for (MarkerDetectionRecord record : mDetectionHistory)
                {
                    historyAsStrings.add(record.code);
                }

                for (int numberOfCodesInHistory = Math.min(action.getCodes().size(), historyAsStrings.size()); numberOfCodesInHistory>0; --numberOfCodesInHistory)
                {
                    if (firstN(action.getCodes(), numberOfCodesInHistory).equals(lastN(historyAsStrings, numberOfCodesInHistory)))
                    {
                        int start = mDetectionHistory.size() - numberOfCodesInHistory;
                        for (MarkerDetectionRecord record : mDetectionHistory.subList(start<0?0:start, mDetectionHistory.size()))
                        {
                            result.add(record.markerImage);
                        }
                        for (int i=numberOfCodesInHistory; i<action.getCodes().size(); ++i)
                        {
                            result.add(null);
                        }
                        break;
                    }
                }
                return result;
            }
            return result;
        }
        return null;
    }

    private List<String> firstN(List<String> list, int n)
    {
        int start = list.size() - n;
        return list.subList(0, n>list.size()?list.size():n);
    }
    private List<String> lastN(List<String> list, int n)
    {
        int start = list.size() - n;
        return list.subList(start<0?0:start, list.size());
    }

    /**
     * Search for group codes (or "pattern groups") in the detected codes. This will only return
     * group codes set in the experience.
     * @return
     */
    private String getGroupCode() {
        if (experience != null) {
            // Search for pattern groups
            // By getting every combination of the currently detected markers and checking if they exist in the experience (biggest groups first, groups must include at least 2 markers)
            if (mCodesDetected != null && mCodesDetected.size() > 1) {
                List<Set<List<String>>> combinations = new ArrayList<>();
                combinationsOfStrings(mCodesDetected, mCodesDetected.size(), combinations);
                for (int i = combinations.size() - 1; i >= 1; --i) {
                    List<String> mostRecentGroup = null;
                    String mostRecentGroupStr = null;
                    for (List<String> code : combinations.get(i)) {
                        String codeStr = joinStr(code, "+");
                        if (isValidCode(codeStr) && doMarkerDetectionTimesOverlap(code) && getMostRecentDetectionTime(code, mostRecentGroup)>getMostRecentDetectionTime(mostRecentGroup, code)) {
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

    private long getMostRecentDetectionTime(List<String> codes, List<String> excluding)
    {
        long mostRecentTime = 0;
        if (codes!=null)
        {
            for (String codeStr : codes)
            {
                if (excluding == null || !excluding.contains(codeStr))
                {
                    MarkerDetectionRecord code = mActiveMarkerRecoreds.get(codeStr);
                    if (code != null && code.lastDetected > mostRecentTime)
                    {
                        mostRecentTime = code.lastDetected;
                    }
                }
            }
        }
        return mostRecentTime;
    }

    private boolean doMarkerDetectionTimesOverlap(List<String> codes)
    {
        for (int i=0; i<codes.size()-1; ++i)
        {
            MarkerDetectionRecord code1 = mActiveMarkerRecoreds.get(codes.get(i));
            boolean overlapFound = false;
            for (int j=i+1; j<codes.size(); ++j)
            {
                MarkerDetectionRecord code2 = mActiveMarkerRecoreds.get(codes.get(j));
                overlapFound = doTimesOverlap(code1.firstDetected, code1.lastDetected, code2.firstDetected, code2.lastDetected);
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
        return (firstDetected1 <= lastDetected2)  &&  (lastDetected1 >= firstDetected2);
    }

    /**
     * Get all the combinations of objects up to a maximum size for the combination and add it to the result List.
     * E.g. combinationsOfStrings([1,3,2], 2, []) changes the result array to [([1],[2],[3]),([1,2],[1,3],[2,3])] where () denotes a Set and [] denotes a List.
     */
    private static void combinationsOfStrings(List<String> strings, int maxCombinationSize, List<Set<List<String>>> result)
    {

        if (maxCombinationSize > 0)
        {
            if (maxCombinationSize == 1)
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
                Set<List<String>> base = result.get(result.size() - 1);

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
    }

    /**
     * Search for sequential codes (or "pattern paths") in detection history. This method may
     * remove items from history that do not match the beginning of any sequential code in the
     * experience and will only return a code from the experience.
     * @return
     */
    private String getSequentialCode()
    {
        if (experience != null)
        {
            // Search for sequential actions in history
            // by creating history sub-lists and checking if any codes in the experience match.
            // e.g. if history=[A,B,C,D] check sub-lists [A,B,C,D], [B,C,D], [C,D].
            if (mDetectionHistory != null && mDetectionHistory.size() > 0)
            {
                boolean foundPrefix = false;
                int start = 0;

                List<String> detectionHistoryAsStrings = new ArrayList<>();
                for (MarkerDetectionRecord record : mDetectionHistory)
                {
                    detectionHistoryAsStrings.add(record.code);
                }
                while (start < mDetectionHistory.size())
                {
                    List<String> subList = detectionHistoryAsStrings.subList(start, detectionHistoryAsStrings.size());
                    String joinedString = joinStr(subList, ">");
                    if (subList.size() != 1 && isValidCode(joinedString))
                    {
                        // Case 1: The history sublist is a sequential code in the experience.
                        return joinedString;
                    }
                    else if (!foundPrefix && !hasSequentialPrefix(joinedString))
                    {
                        // Case 2: No sequential codes in the experience start with the history sublist (as well as previous history sublists).
                        // So remove the first part of it from history
                        // This ensures that history never grows longer than the longest code
                        detectionHistoryAsStrings.remove(0);
                        mDetectionHistory.remove(0);
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

    private static String joinStr(Collection<String> strings, String joiner)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String string : strings)
        {
            if (!first)
            {
                sb.append(joiner);
            }
            sb.append(string);
            first=false;
        }
        return sb.toString();
    }

    /**
     * Search for the single marker with the highest count that is in the experience, or just the highest count if none are in the experience.
     * @return
     */
    private String getStandardCode()
    {
        MarkerDetectionRecord result = null;
        boolean resultIsInExperience = false;
        for (String code : mCodesDetected)
        {
            MarkerDetectionRecord marker = mActiveMarkerRecoreds.get(code);
            boolean markerIsInExperience = isValidCode(code);
            if (result==null || (!resultIsInExperience && markerIsInExperience) || (resultIsInExperience==markerIsInExperience && ((marker.lastDetected>result.lastDetected)||(marker.lastDetected==result.lastDetected && marker.firstDetected>result.firstDetected)||(marker.lastDetected==result.lastDetected && marker.firstDetected==result.firstDetected && marker.count>result.count))))
            {
                result = marker;
                resultIsInExperience = markerIsInExperience;
            }
        }

        return result==null ? null : result.code;
    }

    private HashMap<String, Action> validCodes = null;
    private HashMap<String, Set<Action>> subGroupCodes = null;
    private HashMap<String, Set<Action>> subSequenceCodes = null;

    private void logDataCache()
    {
        Log.i("DATACACHE", "Valid codes = " + joinStr(validCodes.keySet(), ", "));

        List<String> subGroupCodesStrs = new ArrayList<>();
        for (Map.Entry<String, Set<Action>> entry : subGroupCodes.entrySet())
        {
            List<String> actionStrs = new ArrayList<>();
            for (Action action : entry.getValue())
            {
                actionStrs.add(joinStr(action.getCodes(), ","));
            }
            subGroupCodesStrs.add(entry.getKey() + ": " + joinStr(actionStrs, " or "));
        }
        Log.i("DATACACHE", "Sub-group codes = " + joinStr(subGroupCodesStrs, ", "));

        subGroupCodesStrs = new ArrayList<>();
        for (Map.Entry<String, Set<Action>> entry : subSequenceCodes.entrySet())
        {
            List<String> actionStrs = new ArrayList<>();
            for (Action action : entry.getValue())
            {
                actionStrs.add(joinStr(action.getCodes(), ","));
            }
            subGroupCodesStrs.add(entry.getKey() + ": " + joinStr(actionStrs, " or "));
        }
        Log.i("DATACACHE", "Sub-sequence codes = " + joinStr(subGroupCodesStrs, ", "));
    }

    private boolean isValidCode(String code)
    {
        if (validCodes==null)
        {
            createDataCache();
        }
        return validCodes.containsKey(code);
    }

    private boolean hasSequentialPrefix(String prefix)
    {
        if (subSequenceCodes==null)
        {
            createDataCache();
        }
        return subSequenceCodes.containsKey(prefix);
    }

    private Action getActionFor(String code)
    {
        if (validCodes==null)
        {
            createDataCache();
        }
        return validCodes.get(code);
    }

    private Action getPossibleFutureSequentialActionFor(Action found, String foundUsing)
    {
        if (subSequenceCodes == null)
        {
            createDataCache();
        }

        int minimumSize = 1;
        if (found != null && found.getMatch() != Action.Match.any)
        {
            minimumSize = found.getCodes().size() + 1;
        }

        if (mDetectionHistory.size()==0)
        {
            return found;
        }

        // if a single marker triggered found Action and it's not the last one in history then do
        // not provide a possible future sequential action as this will look confusing in the interface
        if (found!=null && found.getMatch()==Action.Match.any && foundUsing!=null)
        {
            MarkerDetectionRecord last = mDetectionHistory.get(mDetectionHistory.size()-1);
            if (!foundUsing.equals(last.code))
            {
                return found;
            }
        }

        if (found == null || found.getMatch() != Action.Match.all)
        {
            List<String> detectionHistoryAsStrings = new ArrayList<>();
            for (MarkerDetectionRecord record : mDetectionHistory)
            {
                detectionHistoryAsStrings.add(record.code);
            }
            for (int i = 0; i < detectionHistoryAsStrings.size(); ++i)
            {
                List<String> subHistory = detectionHistoryAsStrings.subList(i, detectionHistoryAsStrings.size());
                Set<Action> actions = subSequenceCodes.get(joinStr(subHistory, ">"));
                if (actions != null && !actions.isEmpty())
                {
                    Action longestSequentialAction = null;
                    for (Action action : actions)
                    {
                        if (action.getCodes().size() >= minimumSize && (longestSequentialAction == null || longestSequentialAction.getCodes().size() < action.getCodes().size()))
                        {
                            longestSequentialAction = action;
                        }
                    }
                    if (longestSequentialAction != null)
                    {
                        return longestSequentialAction;
                    }
                }
            }
        }

        return found;
    }

    private Action getPossibleFutureGroupActionFor(Action found)
    {
        if (subGroupCodes == null)
        {
            createDataCache();
        }

        if (found == null || found.getMatch()!=Action.Match.sequence)
        {

            List<String> detectedInFound = null;
            if (found!=null)
            {
                detectedInFound = intersection(found.getCodes(), mCodesDetected);
            }

            Set<Action> groupFutureActions = subGroupCodes.get(joinStr(mCodesDetected, "+"));
            if (groupFutureActions != null && !groupFutureActions.isEmpty())
            {
                Action largestGroupAction = null;
                for (Action action : groupFutureActions)
                {
                    if ((found==null || action.getCodes().containsAll(detectedInFound)) && (largestGroupAction == null || largestGroupAction.getCodes().size() < action.getCodes().size()))
                    {
                        largestGroupAction = action;
                    }
                }
                if (largestGroupAction!=null)
                {
                    return largestGroupAction;
                }
            }
        }

        return found;
    }

    private static List<String> intersection(List<String> list1, List<String> list2)
    {
        List<String> intersection = new ArrayList<>();
        if (list1!=null && list2!=null)
        {
            intersection.addAll(list1);
            intersection.retainAll(list2);
        }
        return intersection;
    }

    private void createDataCache()
    {
        if (validCodes==null)
        {
            validCodes = new HashMap<>();
            subGroupCodes = new HashMap<>();
            subSequenceCodes = new HashMap<>();
            for (Action action : experience.getActions())
            {
                if (action.getMatch()== Action.Match.any || action.getCodes().size()==1) // single
                {
                    for (String code : action.getCodes())
                    {
                        validCodes.put(code, action);
                    }
                }
                else if (action.getMatch()== Action.Match.all) // group
                {
                    String code = joinStr(action.getCodes(), "+");
                    validCodes.put(code, action);

                    List<Set<List<String>>> subGroupsByLength = new ArrayList<>();
                    combinationsOfStrings(action.getCodes(), action.getCodes().size()-1, subGroupsByLength);
                    for (Set<List<String>> setOfGroups : subGroupsByLength)
                    {
                        for (List<String> group : setOfGroups)
                        {
                            code = joinStr(group, "+");
                            Set<Action> actions = subGroupCodes.get(code);
                            if (actions != null)
                            {
                                actions.add(action);
                            }
                            else
                            {
                                HashSet<Action> actionsForSubGroup = new HashSet<>();
                                actionsForSubGroup.add(action);
                                subGroupCodes.put(code, actionsForSubGroup);
                            }
                        }
                    }
                }
                else if (action.getMatch()== Action.Match.sequence)
                {
                    String code = joinStr(action.getCodes(), ">");
                    validCodes.put(code, action);
                    for (int subCodeSize=1; subCodeSize<action.getCodes().size(); ++subCodeSize)
                    {
                        code = joinStr(action.getCodes().subList(0, subCodeSize), ">");
                        Set<Action> actions = subSequenceCodes.get(code);
                        if (actions != null)
                        {
                            actions.add(action);
                        }
                        else
                        {
                            HashSet<Action> actionsForSubSequence = new HashSet<>();
                            actionsForSubSequence.add(action);
                            subSequenceCodes.put(code, actionsForSubSequence);
                        }
                    }
                }
            }
        }
    }
}
