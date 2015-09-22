/*
 * Artcodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.artcodes.scanner.detect;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class MarkerDetectionHandler
{
	protected static final int REQUIRED = 5;
	protected static final int MAX = REQUIRED * 4;
	private final Multiset<String> markerCounts = HashMultiset.create();

	public void onMarkersDetected(List<Marker> markers)
	{
		final Collection<String> removals = new HashSet<>(markerCounts.elementSet());

		for (Marker markerCode : markers)
		{
			final String marker = markerCode.getCodeKey();
			final int count = markerCounts.count(marker);
			if (count > MAX)
			{
				markerCounts.setCount(marker, MAX);
			}

			//increase occurrence if this marker is already in the list.
			markerCounts.add(marker);
			removals.remove(marker);
		}

		markerCounts.removeAll(removals);

		onMarkersDetected(markerCounts);
	}

	public abstract void onMarkersDetected(Multiset<String> markers);

	public void reset()
	{
		markerCounts.clear();
	}
}
