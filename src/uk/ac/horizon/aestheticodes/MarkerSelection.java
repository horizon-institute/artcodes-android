/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerSelection
{
	private static final float duration = 1000;
	private final Map<String, Marker> occurences = new HashMap<String, Marker>();
	private long last = 0;
	private long lastUpdate = 0;
	private long total = 0;

	public void addMarkers(List<Marker> markers)
	{
		//check if this is the first marker detected in particular duration.
		final long now = System.currentTimeMillis();
		if(markers.size() > 0)
		{
			if (occurences.size() == 0)
			{
				total = 0;
			}
			else
			{
				total += now - last;
			}
			lastUpdate = now;
		}

		last = now;

		if (isTimeUp(now))
		{
			total = 0;
		}
		else
		{
			for (Marker marker : markers)
			{
				//increase occurence if this marker is already in the list.
				Marker existing = occurences.get(marker.getCodeKey());
				if (existing != null)
				{
					existing.setOccurences(marker.getOccurences() + existing.getOccurences());
				}
				else
				{
					occurences.put(marker.getCodeKey(), marker);
				}
			}
		}
	}

	private boolean isTimeUp(long time)
	{
		return time > lastUpdate + (2*duration);
	}

	public float expiration()
	{
		final long now = System.currentTimeMillis();
		long timeout = now - lastUpdate;
		if(timeout > duration)
		{
			return (timeout - duration) / duration;
		}
		return 0;
	}

	public boolean isTimeUp()
	{
		return isTimeUp(System.currentTimeMillis());
	}

	public boolean isFinished()
	{
		return total > duration;
	}

	public Marker getLikelyMarker()
	{
		Marker likely = null;
		for (Marker marker : occurences.values())
		{
			if (likely == null || marker.getOccurences() > likely.getOccurences())
			{
				likely = marker;
			}
		}
		return likely;
	}

	public void reset()
	{
		occurences.clear();
	}

	public float getProgress()
	{
		final long now = System.currentTimeMillis();
		if (occurences.size() == 0)
		{
			return 0;
		}
		else if (isTimeUp(now))
		{
			return 0;
		}

		return total / duration;
	}

	public boolean hasStarted()
	{
		return occurences.size() != 0;
	}
}
