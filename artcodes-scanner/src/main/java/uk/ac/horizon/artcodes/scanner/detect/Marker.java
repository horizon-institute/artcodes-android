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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Marker
{

	private final String codeKey;
	private final List<Integer> code;
	private final List<MarkerDetails> markerDetails = new ArrayList<>();
	public Marker(String codeKey, MarkerDetails markerDetails)
	{
		this.codeKey = codeKey;
		this.markerDetails.add(markerDetails);

		this.code = new ArrayList<>();
		for (Map<String, Object> region : markerDetails.regions)
		{
			this.code.add((Integer) region.get(MarkerDetails.REGION_VALUE));
		}
	}

	public boolean equals(Object m)
	{
		return m.getClass() == this.getClass() && isCodeEqual((Marker) m);
	}

	public List<Integer> getCode()
	{
		return this.code;
	}

	/// compatibility methods

	public String getCodeKey()
	{
		return codeKey;
	}

	public List<Integer> getComponentIndexs()
	{
		List<Integer> indexes = new ArrayList<>();
		for (MarkerDetails details : this.markerDetails)
		{
			indexes.add(details.markerIndex);
		}
		return indexes;
	}

	public List<MarkerDetails> getMarkerDetails()
	{
		return this.markerDetails;
	}

	public int hashCode()
	{
		return this.codeKey.hashCode();
	}

	boolean isCodeEqual(Marker marker)
	{
		return getCodeKey().equals(marker.getCodeKey());
	}

	public static class MarkerDetails
	{
		public static final String REGION_INDEX = "index";
		public static final String REGION_VALUE = "value";
		public int markerIndex;
		public List<Map<String, Object>> regions;
		public Integer embeddedChecksum;
		public Integer embeddedChecksumRegionIndex;

		public MarkerDetails()
		{
			regions = new ArrayList<>();
		}

		public MarkerDetails(MarkerDetails other)
		{
			this.markerIndex = other.markerIndex;
			this.regions = other.regions;
			this.embeddedChecksum = other.embeddedChecksum;
			this.embeddedChecksumRegionIndex = other.embeddedChecksumRegionIndex;
		}

		public Map<String, Object> createRegion(int index, int value)
		{
			Map<String, Object> region = new HashMap<>();
			region.put(REGION_INDEX, index);
			region.put(REGION_VALUE, value);

			this.regions.add(region);
			return region;
		}
	}
}