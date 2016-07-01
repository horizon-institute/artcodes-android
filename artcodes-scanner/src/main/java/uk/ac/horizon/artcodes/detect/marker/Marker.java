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

import java.util.List;

public class Marker
{
	public final int markerIndex;
	public final List<MarkerRegion> regions;

	public Marker(int markerIndex, List<MarkerRegion> regions)
	{
		this.markerIndex = markerIndex;
		this.regions = regions;
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	private String cashedToString = null;
	@Override
	public String toString()
	{
		if (this.cashedToString==null)
		{
			StringBuilder sb = new StringBuilder(this.regions.size() * 2);
			for (MarkerRegion region : this.regions)
			{
				sb.append(region.value);
				sb.append(':');
			}
			sb.deleteCharAt(sb.length() - 1);
			this.cashedToString = sb.toString();
		}
		return this.cashedToString;
	}

	@Override
	public boolean equals(Object o)
	{
		// Marker and MarkerRegion contain indexes to contours in a frame
		// so use the string representation for equality
		return this.toString().equals(o.toString());
	}
}