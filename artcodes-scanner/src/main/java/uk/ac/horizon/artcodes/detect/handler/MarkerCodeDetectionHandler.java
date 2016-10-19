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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import uk.ac.horizon.artcodes.detect.marker.Marker;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;

public class MarkerCodeDetectionHandler implements MarkerDetectionHandler
{
    protected static final int REQUIRED = 20;
    protected static final int MAX_MULTIPLIER = 4;
	protected static final int OCCURRENCES = 2;

    protected final Multiset<String> markerCounts = HashMultiset.create();
    private Experience experience;
    private final CodeDetectionHandler markerCodeHandler;

    public MarkerCodeDetectionHandler(Experience experience, CodeDetectionHandler markerCodeHandler)
    {
        this.experience = experience;
        this.markerCodeHandler = markerCodeHandler;
    }

    @Override
    public void onMarkersDetected(Collection<Marker> markers, ArrayList<MatOfPoint> contours, Mat hierarchy, Size sourceImageSize)
    {
        countMarkers(markers);
	    int best = 0;
	    String selected = null;
	    for (String code : markerCounts.elementSet())
	    {
		    int count = markerCounts.count(code);
		    if (count > best)
		    {
			    selected = code;
			    best = count;
		    }
	    }

	    if (selected != null || best >= this.requiredFor(selected))
	    {
		    this.markerCodeHandler.onMarkerCodeDetected(selected);
	    }
    }

    protected Multiset<String> countMarkers(Collection<Marker> markers)
    {
        final Collection<String> removals = new HashSet<>(markerCounts.elementSet());

        for (Marker marker : markers)
        {
            final String markerCode = marker.toString();
            final int count = markerCounts.count(markerCode);
            int max = maxFor(markerCode);
            if (count > max)
            {
                markerCounts.setCount(markerCode, max);
            }

            //increase occurrence if this marker is already in the list.
            markerCounts.add(markerCode, this.awardFor(markerCode));
            removals.remove(markerCode);
        }

	    Multisets.removeOccurrences(markerCounts, removals);

        return markerCounts;
    }

    private int awardFor(String code)
    {
        if (this.experience != null)
        {
            Action action = this.experience.getActionForCode(code);
            if (action != null && action.getFramesAwarded() != null)
            {
                return action.getFramesAwarded();
            }
        }
        return OCCURRENCES;
    }

    private int requiredFor(String code)
    {
        if (this.experience != null)
        {
            Action action = this.experience.getActionForCode(code);
            if (action != null && action.getFramesRequired() != null)
            {
                return action.getFramesRequired();
            }
        }
        return REQUIRED;
    }

    private int maxFor(String code)
    {
        return this.requiredFor(code) * MAX_MULTIPLIER;
    }
}
