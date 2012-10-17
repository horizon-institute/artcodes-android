package uk.ac.horizon.data;

import java.util.Hashtable;

public class DtouchMarkersDataSource {
	private static Hashtable<String, DataMarker> dataMarkers;
	
	private static void initMarkers(){
		dataMarkers = new Hashtable<String, DataMarker>();
		
		//add services data.
		//String VIEDO_ID = "cKd8NXWwvKI";
		//addMarker("1:1:2:2:3", "You Tube", "vnd:youTube" + VIEDO_ID, DataMarker.YOU_TUBE);
		//addMarker("1:1:1:3:3", "email", null, DataMarker.MAIL);
		
		//post card
		addMarker("1:1:3:3:4", "Browse website", "http://aestheticodes.blogs.wp.horizon.ac.uk/",DataMarker.WEBSITE);

		//Food
		addMarker("1:1:1:1:2","Browse website", "http://aestheticodes.blogs.wp.horizon.ac.uk/food/",DataMarker.WEBSITE);
		addMarker("1:1:2:4:4","Browse website", "http://aestheticodes.blogs.wp.horizon.ac.uk/food/",DataMarker.WEBSITE);
		
		//Placemat
		addMarker("1:1:1:4:5","Browse website", "http://aestheticodes.blogs.wp.horizon.ac.uk/placemat/",DataMarker.WEBSITE);
		
		//restaurant
		addMarker("1:1:2:3:5","Browse website", "http://aestheticodes.blogs.wp.horizon.ac.uk/restaurant/",DataMarker.WEBSITE);
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
		}
		return marker;
	}
}