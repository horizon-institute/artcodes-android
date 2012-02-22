package uk.ac.horizon.dtouch;

import android.os.AsyncTask;

public class DtouchMarkerWebServicesTask extends AsyncTask<String, Void, DtouchMarker> {
	
	protected DtouchMarker doInBackground(String... codes){
		return DtouchMarkersDataSource.getDtouchMarkerUsingKey(codes[0]);
	}

	protected void onPostExecute(DtouchMarker marker){
		
	}
}
