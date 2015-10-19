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

package uk.ac.horizon.artcodes.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import uk.ac.horizon.artcodes.scanner.detect.Marker;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetector;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

public class MarkerSettings
{
	public final int minRegions;
	public final int maxRegions;
	public final int maxRegionValue;

	public final int checksumModulo;
	public final boolean embeddedChecksum;
	public final Collection<String> validCodes = new HashSet<>();
	public final List<ImageProcessor> pipeline = new ArrayList<>();

	public boolean detected = false;

	private final MarkerDetectionHandler handler;

	public MarkerSettings(Experience experience, MarkerDetectionHandler handler)
	{
		int maxValue = 3;
		int minRegionCount = 100;
		int maxRegionCount = 3;
		for (Action action : experience.getActions())
		{
			for (String code : action.getCodes())
			{
				String[] values = code.split(":");
				minRegionCount = Math.min(minRegionCount, values.length);
				maxRegionCount = Math.max(maxRegionCount, values.length);
				for (String value : values)
				{
					try
					{
						int codeValue = Integer.parseInt(value);
						maxValue = Math.max(maxValue, codeValue);
					} catch (Exception e)
					{
						Log.w("", e.getMessage(), e);
					}
				}

				validCodes.add(code);
			}
		}

		this.handler = handler;

		this.maxRegionValue = maxValue;
		this.minRegions = minRegionCount;
		this.maxRegions = maxRegionCount;
		this.checksumModulo = experience.getChecksumModulo();
		this.embeddedChecksum = experience.getEmbeddedChecksum();
		Log.i("", "Regions " + minRegionCount + "-" + maxRegionCount + " using max of " + maxValue);

		for(String processor: experience.getPipeline())
		{
			// TODO Construct pipeline
		}

		if (pipeline.isEmpty())
		{
			pipeline.add(new TileThresholder(this));
			pipeline.add(new MarkerDetector(this));
		}

	}

	public void markersFound(List<Marker> markers)
	{
		detected = !markers.isEmpty();
		handler.onMarkersDetected(markers);
	}

	public boolean isValidCode(List<Integer> markerCodes, Integer embeddedChecksum)
	{
		if (markerCodes == null)
		{
			return false; // No Code
		} else if (markerCodes.size() < minRegions)
		{
			return false; // Too Short
		} else if (markerCodes.size() > maxRegions)
		{
			return false; // Too long
		}

		for (Integer value : markerCodes)
		{
			//check if leaves are using in accepted range.
			if (value > maxRegionValue)
			{
				return false; // value is too Big
			}
		}

		if (embeddedChecksum == null && !hasValidChecksum(markerCodes))
		{
			return false; // Region Total not Divisable by checksumModulo
		} else if (this.embeddedChecksum && embeddedChecksum != null && !hasValidEmbeddedChecksum(markerCodes, embeddedChecksum))
		{
			return false; // Region Total not Divisable by embeddedChecksum
		} else if (!this.embeddedChecksum && embeddedChecksum != null)
		{
			// Embedded checksum is turned off yet one was provided to this function (this should never happen unless the settings are changed in the middle of detection)
			return false; // Embedded checksum markers are not valid.
		}

		return true;
	}

	/**
	 * This function divides the total number of leaves in the marker by the
	 * value given in the checksumModulo preference. Code is valid if the modulo is 0.
	 *
	 * @return true if the number of leaves are divisible by the checksumModulo value
	 * otherwise false.
	 */
	private boolean hasValidChecksum(List<Integer> markerCodes)
	{
		if (checksumModulo <= 1)
		{
			return true;
		}
		int numberOfLeaves = 0;
		for (int code : markerCodes)
		{
			numberOfLeaves += code;
		}
		return (numberOfLeaves % checksumModulo) == 0;
	}

	private boolean hasValidEmbeddedChecksum(List<Integer> code, Integer embeddedChecksum)
	{
		// Find weighted sum of code, e.g. 1:1:2:4:4 -> 1*1 + 1*2 + 2*3 + 4*4 + 4*5 = 45
		int weightedSum = 0;
		for (int i = 0; i < code.size(); ++i)
		{
			weightedSum += code.get(i) * (i + 1);
		}
		return embeddedChecksum == (weightedSum % 7 == 0 ? 7 : weightedSum % 7);
	}
}
