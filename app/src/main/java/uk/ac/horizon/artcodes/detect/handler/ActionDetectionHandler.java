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

import java.util.List;

import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.MarkerImage;

public interface ActionDetectionHandler
{
	/**
	 * Called when markers that trigger an Action are detected (or markers that may make up a future group/sequential Action).
	 * Note: detectedAction.codes() should always be a sub-list of possibleFutureAction.codes().
	 *
	 * @param detectedAction        The detected Action (can be null if detected markers are only part of a group/sequential action).
	 * @param possibleFutureAction  A possible future action that might be triggered by markers detected so far.
	 * @param imagesForFutureAction Details of markers just detected.
	 */
	void onMarkerActionDetected(Action detectedAction, Action possibleFutureAction, List<MarkerImage> imagesForFutureAction);
}