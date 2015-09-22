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

import com.google.common.collect.Multiset;

import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;

public abstract class ActionDetectionHandler extends MarkerDetectionHandler
{
	private final Experience experience;
	private Action action;

	public ActionDetectionHandler(Experience experience)
	{
		this.experience = experience;
	}

	public abstract void onActionChanged(Action action);

	@Override
	public void onMarkersDetected(Multiset<String> markers)
	{
		int best = 0;
		Action selected = null;
		for (Action action : experience.getActions())
		{
			if (action.getMatch() == Action.Match.any)
			{
				for (String code : action.getCodes())
				{
					int count = markers.count(code);
					if (count > best)
					{
						selected = action;
						best = count;
					}
				}
			} else if (action.getMatch() == Action.Match.all)
			{
				int min = MAX;
				int total = 0;
				for (String code : action.getCodes())
				{
					int count = markers.count(code);
					min = Math.min(min, count);
					total += count;
				}

				if (min > REQUIRED && total > best)
				{
					best = total;
					selected = action;
				}
			}
		}

		if (selected == null || best < REQUIRED)
		{
			if (action != null)
			{
				action = null;
				onActionChanged(null);
			}
		} else if (selected != action)
		{
			action = selected;
			onActionChanged(action);
		}
	}
}
