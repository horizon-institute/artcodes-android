/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
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

package uk.ac.horizon.aestheticodes.controllers.adapters;

import android.content.Context;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;

public class MarkerRegionAdapter extends ValueAdapter<Experience>
{
	private final Context context;

	public MarkerRegionAdapter(Context context)
	{
		this.context = context;
	}

	@Override
	public Object getValue(Experience experience)
	{
		if (experience == null)
		{
			return null;
		}
		if (experience.getMinRegions() == experience.getMaxRegions())
		{
			return context.getString(R.string.regions_value, experience.getMinRegions());
		}
		return context.getString(R.string.regions_value, experience.getMinRegions() + "-" + experience.getMaxRegions());
	}
}
