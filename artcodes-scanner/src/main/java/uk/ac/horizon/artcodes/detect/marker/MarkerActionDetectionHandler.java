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

package uk.ac.horizon.artcodes.detect.marker;

import com.google.common.collect.Multiset;

import java.util.Collection;

import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;

public class MarkerActionDetectionHandler extends MarkerCodeDetectionHandler
{
    public interface ActionDetectionHandler
    {
        /**
         * Called when markers that trigger an action are detected (or markers that may make up a future group/sequential action).
         * Note: detectedAction.codes() should always be a sub-list of asPartOfFutureAction.codes().
         * @param detectedAction The detected Action (can be null if detected markers are only part of a group/sequential action).
         * @param asPartOfFutureAction A possible future action that might be triggered by markers detected so far.
         * @param detectedMarkers Details of markers just detected.
         */
        void onMarkerActionDetected(Action detectedAction, Collection<Marker> detectedMarkers, Action asPartOfFutureAction);
    }

    private ActionDetectionHandler markerActionHandler;
    private Experience experience;
    private Action currentAction;

    public MarkerActionDetectionHandler(ActionDetectionHandler markerActionHandler, Experience experience)
    {
        super(null);
        this.markerActionHandler = markerActionHandler;
        this.experience = experience;
    }

    @Override
    protected void actOnMarkers(Multiset<String> markers)
    {
        int best = 0;
        Action selected = null;
        for (Action action : this.experience.getActions())
        {
            if (action.getMatch() == Action.Match.any)
            {
                for (String code : action.getCodes())
                {
                    int count = markers.count(code);
                    if (count > best)
                    {
                        selected = action;
                        best = count;
                    }
                }
            }
            else if (action.getMatch() == Action.Match.all)
            {
                int min = MAX;
                int total = 0;
                for (String code : action.getCodes())
                {
                    int count = markers.count(code);
                    min = Math.min(min, count);
                    total += (count * 2);
                }

                if (min > REQUIRED && total > best)
                {
                    best = total;
                    selected = action;
                }
            }
        }

        if (selected == null || best < REQUIRED)
        {
            if (currentAction != null)
            {
                currentAction = null;
                this.markerActionHandler.onMarkerActionDetected(null, null, null);
            }
        }
        else if (selected != currentAction)
        {
            currentAction = selected;
            this.markerActionHandler.onMarkerActionDetected(currentAction, null, null);
        }
    }
}
