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

		// add services data.
		// String VIEDO_ID = "cKd8NXWwvKI";
		// addMarker("1:1:2:2:3", "You Tube", "vnd:youTube" + VIEDO_ID,
		// DataMarker.YOU_TUBE);
		// addMarker("1:1:1:3:3", "email", null, DataMarker.MAIL);

		//

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
		addMarker("1:1:3:3:4", "Browse website", code5, DataMarker.WEBSITE);// "http://aestheticodes.blogs.wp.horizon.ac.uk/",DataMarker.WEBSITE);
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