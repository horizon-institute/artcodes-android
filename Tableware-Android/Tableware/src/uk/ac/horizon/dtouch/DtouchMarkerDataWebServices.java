package uk.ac.horizon.dtouch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

public class DtouchMarkerDataWebServices{
	private MarkerDownloadRequestListener mListener;
	DtouchMarker dtouchMarker;
	
	public DtouchMarkerDataWebServices(MarkerDownloadRequestListener listener){
		mListener = listener;
	}
	
	public void executeMarkerRequest(String code){
		String[] params = new String[1];
		params[0] = code;
		new DtouchMarkerDownloadDataTask().execute(code);
	}
	
	void downloadMarkerPrimaryData(String markerCode){
		URL url = DtouchMarkerWebServicesURL.getMarkerPrimaryURL(markerCode);
		HttpURLConnection urlConnection = null;
		try{
			urlConnection = (HttpURLConnection) url.openConnection();
			String jsonData = read(urlConnection.getInputStream());
			dtouchMarker = getMarkerObjFromJsonData(markerCode, jsonData);
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		
	}
	
	void downloadMarkerURLs(String markerCode){
		URL url = DtouchMarkerWebServicesURL.getMarkerURL1(markerCode);
		dtouchMarker.setURL1(getURLContent(url));
		url = DtouchMarkerWebServicesURL.getMarkerURL2(markerCode);
		dtouchMarker.setURL2(getURLContent(url));
		url = DtouchMarkerWebServicesURL.getMarkerURL3(markerCode);
		dtouchMarker.setURL3(getURLContent(url));
	}
	
	private String getURLContent(URL url){
		String data = null;
		HttpURLConnection urlConnection = null;
		try{
			urlConnection = (HttpURLConnection) url.openConnection();
			data = read(urlConnection.getInputStream());
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return data;
	}
	
	DtouchMarker getMarkerObjFromJsonData(String code, String jsonData){
		JSONObject jsonObject;
		DtouchMarker marker = null;
		try {
			jsonObject = new JSONObject(jsonData);
			String name = jsonObject.getString("name");
			String type = jsonObject.getString("type");
			marker = new DtouchMarker();
			marker.setCode(code);
			marker.setType(type);
			marker.setDescription(name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return marker;
	}
	
	private String read(InputStream inputStream) throws IOException{
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()){
			sb.append(line);
		}
		inputStream.close();
		return sb.toString();
	}
	
	private class DtouchMarkerDownloadDataTask extends AsyncTask<String, Void, DtouchMarker> {

		protected DtouchMarker doInBackground(String... codes){
			downloadMarkerPrimaryData(codes[0]);
			downloadMarkerURLs(codes[0]);
			return dtouchMarker;
		}
		
		protected void onPostExecute(DtouchMarker marker){
			mListener.onMarkerDownloaded(marker);
		}
	}
	
	/**
	 * Call back Request Listener interface. 
	 * @author pszsa1
	 *
	 */
	public static interface MarkerDownloadRequestListener{
		public void onMarkerDownloaded(DtouchMarker marker);
		
	}
	
}

