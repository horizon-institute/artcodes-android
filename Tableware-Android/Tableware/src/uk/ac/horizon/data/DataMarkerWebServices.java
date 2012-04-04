package uk.ac.horizon.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class DataMarkerWebServices{
	private MarkerDownloadRequestListener mListener;
		
	public DataMarkerWebServices(MarkerDownloadRequestListener listener){
		mListener = listener;
	}
	
	public void executeMarkerRequestUsingCode(String code, String userId){
		String[] params = new String[2];
		params[0] = code;
		params[1] = userId;
		new DtouchMarkerDownloadDataTask().execute(params);
	}
	
	public void executeDishRequestUsingDishName(String dishName, String userId){
		String[] params = new String[2];
		params[0] = dishName;
		params[1] = userId;
		new DishDownloadDataTask().execute(params);
	}
	
	DataMarker downloadMarkerData(String markerCode, String userId) throws JSONException, IOException{
		URL url;
		DataMarker marker = null;
		if (userId != null){
			url = DataMarkerWebServicesURL.getUserMarkerURL(markerCode, userId);
		}else{
			url = DataMarkerWebServicesURL.getMarkerPrimaryURL(markerCode);
		}
		HttpURLConnection urlConnection = null;
		try{
			urlConnection = (HttpURLConnection) url.openConnection();
			String jsonData = read(urlConnection.getInputStream());
			marker = getMarkerObjFromJsonData(markerCode, jsonData);
		}catch(JSONException e){
			marker = null;
			throw new JSONException(e.getMessage());
		}catch(IOException e){
			marker = null;
			throw new IOException(e.getMessage());
		}finally{
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return marker;
	}
	
	DataMarker downloadDishData(String dishName, String userId) throws JSONException, IOException{
		URL url;
		DataMarker marker = null;
		
		if (userId != null){
			url = DataMarkerWebServicesURL.getUserDishURL(dishName, userId);
		}else{
			url = DataMarkerWebServicesURL.getDishURL(dishName);
		}
		HttpURLConnection urlConnection = null;
		try{
			urlConnection = (HttpURLConnection) url.openConnection();
			String jsonData = read(urlConnection.getInputStream());
			marker = getDishObjFromJsonData(dishName, jsonData);
		}catch(JSONException e){
			marker = null;
			throw new JSONException(e.getMessage());
		}catch(IOException e){
			marker = null;
			throw new IOException(e.getMessage());
		}finally{
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return marker;
	}
	
	DataMarker getDishObjFromJsonData(String code, String jsonData) throws JSONException{
		JSONObject jsonObject;
		JSONArray history = null;
		DataMarker marker = null;
		
		jsonObject = new JSONObject(jsonData);
		String title = jsonObject.getString("name");
		String url1 = jsonObject.getString("url1");
		String url2 = jsonObject.getString("url2");
		String url3 = jsonObject.getString("url3");
		try{
			history = jsonObject.getJSONArray("history");
		}catch (JSONException e){
			history = null;
		}
		marker = new DataMarker();
		marker.setURL1(url1);
		marker.setURL2(url2);
		marker.setURL3(url3);
		marker.setTitle(title);
		if (history != null && history.length() > 0){
			List<TWDiningHistoryItem> diningHistory = new ArrayList<TWDiningHistoryItem>();
			for (int i = 0; i < history.length(); i++){
				JSONObject jsonHistoryItem = history.getJSONObject(i);
				TWDiningHistoryItem diningHistoryItem = new TWDiningHistoryItem(jsonHistoryItem.getString("date"),
						jsonHistoryItem.getInt("rating"), jsonHistoryItem.getString("comment"));
				diningHistory.add(diningHistoryItem);
			}
			marker.setDiningHistory(diningHistory);
		}

		return marker;
	}
	
	DataMarker getMarkerObjFromJsonData(String code, String jsonData) throws JSONException{
		JSONObject jsonObject;
		JSONArray history = null;
		DataMarker marker = null;
		
		jsonObject = new JSONObject(jsonData);
		String title = jsonObject.getString("name");
		String type = jsonObject.getString("type");
		String url1 = jsonObject.getString("url1");
		String url2 = jsonObject.getString("url2");
		String url3 = jsonObject.getString("url3");
		try{
			history = jsonObject.getJSONArray("history");
		}catch (JSONException e){
			history = null;
		}
		marker = new DataMarker();
		marker.setCode(code);
		marker.setType(type);
		marker.setURL1(url1);
		marker.setURL2(url2);
		marker.setURL3(url3);
		marker.setTitle(title);
		if (history != null && history.length() > 0){
			List<TWDiningHistoryItem> diningHistory = new ArrayList<TWDiningHistoryItem>();
			for (int i = 0; i < history.length(); i++){
				JSONObject jsonHistoryItem = history.getJSONObject(i);
				TWDiningHistoryItem diningHistoryItem = new TWDiningHistoryItem(jsonHistoryItem.getString("date"),
						jsonHistoryItem.getInt("rating"), jsonHistoryItem.getString("comment"));
				diningHistory.add(diningHistoryItem);
			}
			marker.setDiningHistory(diningHistory);
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
	
	private class DtouchMarkerDownloadDataTask extends AsyncTask<String, Void, DataMarker> {
		protected DataMarker doInBackground(String... data){
			DataMarker marker = null;
			// if marker code is given
			if (data[0] != null){
				try {
					marker = downloadMarkerData(data[0], data[1]);
				} catch (JSONException e) {
					marker = null;
				} catch (IOException e) {
					marker = null;
				}
			}
			return marker;
		}
		
		protected void onPostExecute(DataMarker marker){
			if (marker != null)
				mListener.onMarkerDownloaded(marker);
			else
				mListener.onMarkerDownloadError();
		}
	}
	
	private class DishDownloadDataTask extends AsyncTask<String, Void, DataMarker> {
		protected DataMarker doInBackground(String... data){
			DataMarker marker = null;
			// if marker name is given.
			if (data[0] != null){
				try {
					marker = downloadDishData(data[0], data[1]);
				} catch (JSONException e) {
					marker = null;
				} catch (IOException e) {
					marker = null;
				}
			}
			return marker;
		}
		
		protected void onPostExecute(DataMarker marker){
			if (marker != null)
				mListener.onMarkerDownloaded(marker);
			else
				mListener.onMarkerDownloadError();
		}
	}
	
	/**
	 * Call back Request Listener interface. 
	 * @author pszsa1
	 *
	 */
	public static interface MarkerDownloadRequestListener{
		public void onMarkerDownloaded(DataMarker marker);
		public void onMarkerDownloadError();
		
	}	
}

