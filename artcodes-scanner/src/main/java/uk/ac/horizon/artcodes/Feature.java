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

package uk.ac.horizon.artcodes;

import android.content.Context;
import android.content.SharedPreferences;

public final class Feature
{
	private final Context context;
	private final int featureID;

	private Feature(Context context, int featureID)
	{
		this.context = context;
		this.featureID = featureID;
	}

	public static Feature get(Context context, int feature)
	{
		return new Feature(context, feature);
	}

	public int getId()
	{
		return featureID;
	}

	public String getName()
	{
		return context.getResources().getResourceEntryName(featureID);
	}

	public boolean isEnabled()
	{
		final SharedPreferences preferences = context.getSharedPreferences(Feature.class.getName(), Context.MODE_PRIVATE);
		final String featureName = getName();
		if (preferences.contains(featureName))
		{
			return preferences.getBoolean(featureName, false);
		}
		return context.getResources().getBoolean(featureID);
	}

	public void setEnabled(boolean enabled)
	{
		final SharedPreferences preferences = context.getSharedPreferences(Feature.class.getName(), Context.MODE_PRIVATE);
		final String featureName = getName();
		preferences.edit().putBoolean(featureName, enabled).apply();
	}
}
