package uk.ac.horizon.busabascan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.text.format.Time;

import uk.ac.horizon.dtouchMobile.DtouchMarker;

//Handle dTouch markers integrated over time to work with narrative and zoomed codes
//multiple markers detected at once etc.
public class IntegratedMarkers {
	
	final int PEND_SECONDS = 2;
	
	class IntegrationData {
		Time pendingUntil;
		Time validUntil;
	};

	private List<DtouchMarker> integratedMarkers = new ArrayList<DtouchMarker>();
	Map<DtouchMarker, IntegrationData> pool = new HashMap<DtouchMarker, IntegrationData>();
	final Time past = new Time();  //1970
	
	//integrate is called every frame with the list of this frames detected markers.
	//this function debounces this list using the following rules
	//a marker that is detected is definitely there.
	//a marker that is definitely there is there for 2 seconds of non-detection
	//each marker code has an on threshold of 0 seconds
	//each marker code has an off threshold of 2 seconds
	//the initial marker when going from zero to one has a pending threshold of 2 seconds.
	public void integrate(List<DtouchMarker> dtouchMarkers) {
		boolean unpend = false;
		Time now = new Time();
		now.setToNow();
		
		integratedMarkers.clear();
		
		//Pass 1 set expiry for markers in this frame
		for(DtouchMarker marker : dtouchMarkers)
		{
			Time expire = new Time(now);
			expire.second += PEND_SECONDS;
			expire.normalize(true);
			if (pool.containsKey(marker))
			{
				pool.get(marker).validUntil = expire;
			}
			else
			{
				IntegrationData data = new IntegrationData();
				data.validUntil = expire;
				data.pendingUntil = expire;
				DtouchMarker m = new DtouchMarker(marker.getCode());
				pool.put(m, data);
			}
		}
		//Pass 2 - Gather markers
		Iterator<Entry<DtouchMarker, IntegrationData>> it = pool.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<DtouchMarker, IntegrationData> pair = it.next();
			IntegrationData data = pair.getValue();
			//Remove expired
			if (now.after(data.validUntil))
			{
				//Can't do pool.remove because java is lame 
				//exception if map is modified during iteration! 
				it.remove();
			}
			else if (data.pendingUntil != null && data.pendingUntil.before(now))
			{
				unpend = true;
			}
		}
		//Pass 3 - move everything to integrated list.
		if (unpend)
		{
			it = pool.entrySet().iterator();
			while (it.hasNext())
			{
				Entry<DtouchMarker, IntegrationData> pair = it.next();
				DtouchMarker key = pair.getKey();
				integratedMarkers.add(key);
			}
		}
	}

	public boolean any() {
		return (integratedMarkers.size() > 0);
	}

	public List<DtouchMarker> get() {
		return integratedMarkers;
	}
}
