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

package uk.ac.horizon.aestheticodes.detect;

import android.content.Context;
import uk.ac.horizon.aestheticodes.MarkerSettings;

public class MarkerPreferences extends MarkerSettings
{
	private static final String NO_OF_TILES = "no_of_tiles";
	private static final int DEFAULT_NO_OF_TILES = 1;

	public MarkerPreferences(Context context)
	{
		super(context);
		this.setDefaultMinBranches(5);
		this.setDefaultMaxBranches(6);
		this.setDefaultEmptyBranches(0);
		this.setDefaultValidationBranches(2);
		this.setDefaultValidationBranchLeaves(1);
		this.setDefaultMaxLeaves(5);
		this.setDefaultChecksumModulo(3);
		this.setDefaultMarkerOccurrence(1);
	}

	public int getNumberOfTiles()
	{
		return getIntValue(NO_OF_TILES, DEFAULT_NO_OF_TILES);
	}
}
