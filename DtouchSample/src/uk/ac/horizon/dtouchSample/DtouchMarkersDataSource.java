package uk.ac.horizon.dtouchSample;

import java.util.Hashtable;

public class DtouchMarkersDataSource {
	private static Hashtable<String, DataMarker> dataMarkers;
	
	private static void initMarkers(){
		dataMarkers = new Hashtable<String, DataMarker>();
		
		//add services data.
		String VIEDO_ID = "cKd8NXWwvKI";
		addMarker("1:1:1:3", "You Tube", "vnd:youTube" + VIEDO_ID, DataMarker.YOU_TUBE);
		addMarker("2:2:2:2", "Music", null, DataMarker.MUSIC);
		addMarker("2:2:2:6", "Contacts", null, DataMarker.CONTACTS);
		addMarker("1:1:3:3", "email", null, DataMarker.MAIL);
		
		//add story data.
		addMarker("1:2:2:3:7", "Wow! what a gorgeous day", null, 0);
		addMarker("1:1:2:2", "mmm. It is partly cloudy. partly sunny", null, 0);
		addMarker("2:2:2:3:3", "Raining so heavily.", null, 0);
		
		//add images.
		addMarker("3:3:3", "Cloud Toon", "images/cloudToonWithCaption.gif", 0);
	}

	private static void addMarker(String code, String title, String uri, int serviceId){
		DataMarker marker = new DataMarker(code, title, uri, serviceId);
		dataMarkers.put(code, marker);
	}

	public static DataMarker getDtouchMarkerUsingKey(String codeKey){
		DataMarker marker = null;
		if (dataMarkers == null || dataMarkers.isEmpty())
			initMarkers();
		if (dataMarkers.containsKey(codeKey)){
			marker = dataMarkers.get(codeKey);
		}else{
			marker = new DataMarker(codeKey, "Marker not found.", null, 0);
		}
		return marker;
	}
}