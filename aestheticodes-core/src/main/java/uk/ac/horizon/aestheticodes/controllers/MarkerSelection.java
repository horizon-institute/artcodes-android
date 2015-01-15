/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
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

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerSelection
{
	private static final int TIMEOUT = 2000;
	private static final int REQUIRED = 5;
	private final Map<String, MarkerCode> occurrences = new HashMap<>();
	private long lastUpdate = 0;

	public void addMarkers(List<MarkerCode> markers)
	{
		//check if this is the first marker detected in particular duration.
		final long now = System.currentTimeMillis();
		if (markers.size() > 0)
		{
			lastUpdate = now;
		}

		if (hasTimedOut(lastUpdate, now))
		{
			reset();
		}
		else
		{
			for (MarkerCode markerCode : markers)
			{
				//increase occurrence if this marker is already in the list.
				MarkerCode existing = occurrences.get(markerCode.getCodeKey());
				if (existing != null)
				{
					existing.setOccurrences(markerCode.getOccurrences() + existing.getOccurrences());
				}
				else
				{
					occurrences.put(markerCode.getCodeKey(), markerCode);
				}
			}
		}
	}

	private boolean hasTimedOut(long lastUpdate, long now)
	{
		return now - lastUpdate > TIMEOUT;
	}

	public String getFoundMarker()
	{
		MarkerCode likely = null;
		for (MarkerCode marker : occurrences.values())
		{
			if (likely == null || marker.getOccurrences() > likely.getOccurrences())
			{
				likely = marker;
			}
		}
		if (likely != null && likely.getOccurrences() > REQUIRED)
		{
			Log.i(MarkerSelection.class.getName(), "Detected " + likely.getOccurrences());
			return likely.getCodeKey();
		}
		return null;
	}

	public void reset()
	{
		occurrences.clear();
	}
}
