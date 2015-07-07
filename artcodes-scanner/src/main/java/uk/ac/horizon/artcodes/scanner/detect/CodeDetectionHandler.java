/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.scanner.detect;

import com.google.common.collect.Multiset;

public abstract class CodeDetectionHandler extends MarkerDetectionHandler
{
	public abstract void onCodeDetected(String code);

	public void onMarkersDetected(Multiset<String> markers)
	{
		int best = 0;
		String selected = null;
		for (String code : markers.elementSet())
		{
			int count = markers.count(code);
			if (count > best)
			{
				selected = code;
				best = count;
			}
		}

		if (selected != null || best >= REQUIRED)
		{
			onCodeDetected(selected);
		}
	}
}
