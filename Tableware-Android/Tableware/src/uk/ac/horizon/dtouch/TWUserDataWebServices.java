package uk.ac.horizon.dtouch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class TWUserDataWebServices {
	private UserDataDownloadRequestListener mListener;
		
	public TWUserDataWebServices(UserDataDownloadRequestListener listener){
		mListener = listener;
	}
	
	public static interface UserDataDownloadRequestListener{
		public void onUserDataDownloaded(TWMember member);
		public void onUserDataDownloadError();
	}
	
	public void executeUserDataRequest(String userId){
		String[] params = new String[1];
		params[0] = userId;
		new UserDataDownloadTask().execute(params);
	}
	
	private class UserDataDownloadTask extends AsyncTask<String, Void, TWMember> {
		protected TWMember doInBackground(String... data){
			TWMember member = null;
			String userId = data[0];
			URL url = DtouchMarkerWebServicesURL.getUserURL(userId);
			HttpURLConnection urlConnection = null;
			try{
				urlConnection = (HttpURLConnection) url.openConnection();
				String jsonData = read(urlConnection.getInputStream());
				member = getMemberObjFromJsonData(jsonData);
			} catch (IOException e) {
				member = null;
			}finally{
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return member;
		}
		
		protected void onPostExecute(TWMember member){
			if (member != null)
				mListener.onUserDataDownloaded(member);
			else
				mListener.onUserDataDownloadError();
		}
		
		TWMember getMemberObjFromJsonData(String jsonData){
			JSONObject jsonObject;
			JSONArray history = null;
			TWMember member = null;
			
			try{
			jsonObject = new JSONObject(jsonData);
			member = new TWMember();
			member.id = jsonObject.getString("name");
			member.favouriteDishNames = readFavourites(jsonObject.getString("favourites"));
			member.offers = readOffers(jsonObject.getString("offers"));

			try{
				history = jsonObject.getJSONArray("history");
			}catch (JSONException e){
				history = null;
			}

			if (history != null && history.length() > 0){
				List<TWDiningHistoryItem> diningHistory = new ArrayList<TWDiningHistoryItem>();
				for (int i = 0; i < history.length(); i++){
					JSONObject jsonHistoryItem = history.getJSONObject(i);
					TWDiningHistoryItem diningHistoryItem = new TWDiningHistoryItem(jsonHistoryItem.getString("date"),
							jsonHistoryItem.getInt("rating"), jsonHistoryItem.getString("comment"));
					diningHistory.add(diningHistoryItem);
				}
				member.diningHistory = diningHistory;
			}
			}catch(JSONException e){
				member = null;
			}
			return member;
		}
		
		private List<String> readFavourites(String favourite){
			List<String> favourites = null; 
			if (favourite != null){
				String[] splits = favourite.split(",");
				for (int i = 0; i < splits.length; i++){
					splits[i] = splits[i].trim();
				}
				favourites = Arrays.asList(splits);
			}
			return favourites;
		}
		
		private List<String> readOffers(String offer){
			List<String> offers = null; 
			if (offer != null){
				String[] splits = offer.split(",");
				for (int i = 0; i < splits.length; i++){
					splits[i] = splits[i].trim();
				}
				offers = Arrays.asList(splits);
			}
			return offers;
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
	}
	
	
}
