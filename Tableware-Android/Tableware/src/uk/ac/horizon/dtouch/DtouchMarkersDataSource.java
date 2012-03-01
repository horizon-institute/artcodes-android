package uk.ac.horizon.dtouch;

import java.util.Hashtable;

public class DtouchMarkersDataSource {
	private static Hashtable<String, DtouchMarker> dtouchMarkers;
	
	private static void initMarkers(){
		dtouchMarkers = new Hashtable<String, DtouchMarker>();
		addMarker("1:1:1:1:3:3:5:5", "http://data.horizon.ac.uk/busaba-demo/prawn-pomelo.html#", "Prawn Pomelo");
		addMarker("1:1:1:1:2:3:5:6", "http://data.horizon.ac.uk/busaba-demo/char-grilled-duck.html#", "Char-grilled Duck");
		addMarker("1:1:1:1:2:4:4:6", "http://data.horizon.ac.uk/busaba-demo/pandan-chicken.html#", "Pandan Chicken");
		addMarker("1:1:1:1:1:7:8:10", "http://data.horizon.ac.uk/busaba-demo/chilli-prawn-stir-fry.html#", "Chilli Prawn Stir-fry");
	}
	
	private static void addMarker(String code, String url, String desc){
		DtouchMarker marker = new DtouchMarker(code, url, desc);
		dtouchMarkers.put(code, marker);
	}
	
	public static DtouchMarker getDtouchMarkerUsingKey(String codeKey){
		DtouchMarker marker = null;
		if (dtouchMarkers == null || dtouchMarkers.isEmpty())
			initMarkers();
		if (dtouchMarkers.containsKey(codeKey)){
			marker = dtouchMarkers.get(codeKey);
		}else{
			marker = new DtouchMarker(codeKey, null, "Marker not in the system.");
		}
		return marker;		
	}
	
}
