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

import android.databinding.BaseObservable;

import java.util.Collection;

public class DetectionSettings extends BaseObservable
{
	public enum ThresholdDisplay
	{
		hidden, greyscale, visible;

		private static ThresholdDisplay[] vals = values();

		public ThresholdDisplay next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	public enum MarkerDisplay
	{
		hidden, outline, region_outline;

		private static MarkerDisplay[] vals = values();

		public MarkerDisplay next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	public enum CodeDisplay
	{
		hidden, visible;

		private static CodeDisplay[] vals = values();

		public CodeDisplay next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	private final MarkerDetectionHandler handler;
	private boolean detected;
	private CodeDisplay codeDisplay = CodeDisplay.hidden;
	private MarkerDisplay markerDisplay = MarkerDisplay.hidden;
	private ThresholdDisplay thresholdDisplay = ThresholdDisplay.hidden;

	public DetectionSettings(MarkerDetectionHandler handler)
	{
		this.handler = handler;
	}

	public ThresholdDisplay getThresholdDisplay()
	{
		return thresholdDisplay;
	}

	public CodeDisplay getCodeDisplay()
	{
		return codeDisplay;
	}

	public boolean hasDetected()
	{
		return detected;
	}

	public void markersDetected(Collection<String> markers)
	{
		detected = !markers.isEmpty();
		handler.onMarkersDetected(markers);
	}

	public void nextCodeDisplay()
	{
		codeDisplay = codeDisplay.next();
	}

	public void nextThresholdDisplay()
	{
		thresholdDisplay = thresholdDisplay.next();
	}

	public MarkerDisplay getMarkerDisplay()
	{
		return markerDisplay;
	}

	public void nextMarkerDisplay()
	{
		markerDisplay = markerDisplay.next();
	}
}
