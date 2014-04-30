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

package uk.ac.horizon.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class DtouchMarkersDataSource
{
	private static final Map<String, DataMarker> dataMarkers = new HashMap<String, DataMarker>();
	public static boolean prefsChanged = false;
	private static Context mContext;

	private static void initMarkers()
	{
		dataMarkers.clear();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		for (String key : sharedPreferences.getAll().keySet())
		{
			if (key.startsWith("code_"))
			{
				final String code = key.substring("code_".length()).replaceAll("_", ":");
				final String url = sharedPreferences.getString(key, null);
				if (url != null)
				{
					addMarker(code, url);
				}
			}
		}
	}

	public static void addMarker(String code, String uri)
	{
		dataMarkers.put(code, new DataMarker(code, "Browse website", uri, DataMarker.WEBSITE));
	}

	public static DataMarker getDtouchMarkerUsingKey(String codeKey, Context context)
	{
		mContext = context;
		DataMarker marker = null;
		if (dataMarkers.isEmpty() || prefsChanged)
		{
			initMarkers();
			if (prefsChanged)
			{
				prefsChanged = false;
			}
		}
		if (dataMarkers.containsKey(codeKey))
		{
			marker = dataMarkers.get(codeKey);
		}
		return marker;
	}
}