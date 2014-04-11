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

import java.util.Hashtable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DtouchMarkersDataSource
{
	private static Hashtable<String, DataMarker> dataMarkers;
	private static Context mContext;
	public static boolean prefsChanged = false;

	private static void initMarkers()
	{
		dataMarkers = new Hashtable<String, DataMarker>();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String code1 = prefs.getString("code_1_1_1_1_2", "http://www.aestheticodes.com");
		String code2 = prefs.getString("code_1_1_1_4_5", "http://www.aestheticodes.com");
		String code3 = prefs.getString("code_1_1_2_3_5", "http://www.aestheticodes.com");
		String code4 = prefs.getString("code_1_1_2_4_4", "http://www.aestheticodes.com");
		String code5 = prefs.getString("code_1_1_3_3_4", "http://www.aestheticodes.com");
		String code6 = prefs.getString("code_1_1_1_1_1", "http://www.aestheticodes.com");

		// Food
		addMarker("1:1:1:1:2", "Browse website", code1, DataMarker.WEBSITE);
		// Placemat
		addMarker("1:1:1:4:5", "Browse website", code2, DataMarker.WEBSITE);
		// restaurant
		addMarker("1:1:2:3:5", "Browse website", code3, DataMarker.WEBSITE);
		addMarker("1:1:2:4:4", "Browse website", code4, DataMarker.WEBSITE);
		// post card
		addMarker("1:1:3:3:4", "Browse website", code5, DataMarker.WEBSITE);
		addMarker("1:1:1:1:1", "Browse website", code6, DataMarker.WEBSITE);

	}

	public static void addMarker(String code, String title, String uri, int serviceId)
	{
		DataMarker marker = new DataMarker(code, title, uri, serviceId);
		dataMarkers.put(code, marker);
	}

	public static DataMarker getDtouchMarkerUsingKey(String codeKey, Context context)
	{
		mContext = context;
		DataMarker marker = null;
		if (dataMarkers == null || dataMarkers.isEmpty() || prefsChanged)
		{
			initMarkers();
			if (prefsChanged)
				prefsChanged = false;
		}
		if (dataMarkers.containsKey(codeKey))
		{
			marker = dataMarkers.get(codeKey);
		}
		return marker;
	}
}