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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MarkerSelection
{
	private static final int REQUIRED = 5;
	private static final int MAX = REQUIRED * 4;
	private final Map<String, MarkerCode> occurrences = new HashMap<>();
	private String current = null;

	public void reset(MarkerDetector.Listener callback)
	{
		occurrences.clear();
		if(current != null)
		{
			current = null;
			callback.markerChanged(null);
		}
	}

	public void addMarkers(List<MarkerCode> markers, MarkerDetector.Listener callback)
	{
		final Collection<String> updated = new HashSet<>();

		for (MarkerCode markerCode : markers)
		{
			//increase occurrence if this marker is already in the list.
			MarkerCode existing = occurrences.get(markerCode.getCodeKey());
			if (existing != null)
			{
				existing.setOccurrences(Math.min(MAX, markerCode.getOccurrences() + existing.getOccurrences()));
			}
			else
			{
				occurrences.put(markerCode.getCodeKey(), markerCode);
			}
			updated.add(markerCode.getCodeKey());
		}

		MarkerCode likely = null;
		for(MarkerCode marker: occurrences.values())
		{
			if(!updated.contains(marker.getCodeKey()))
			{
				marker.setOccurrences(Math.max(marker.getOccurrences() - 1, 0));
			}

			if (marker.getOccurrences() > REQUIRED && (likely == null || marker.getOccurrences() > likely.getOccurrences()))
			{
				likely = marker;
			}
		}

		if (likely == null)
		{
			if(current != null)
			{
				current = null;
				callback.markerChanged(null);
			}
		}
		else
		{
			String code = likely.getCodeKey();
			if(current == null || !current.equals(code))
			{
				current = code;
				callback.markerChanged(code);
			}
		}
	}
}
