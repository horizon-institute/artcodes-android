package uk.ac.horizon.data;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.facebook.android.Utility;

public class FavouriteDishThumbnailsWebService {

	private FavouriteDishThumbnailsDownloadRequestListener mListener;
	
	public FavouriteDishThumbnailsWebService(FavouriteDishThumbnailsDownloadRequestListener listener){
		mListener = listener;
	}
	
	public void executeFavouriteDishRequest(String[] dishNames){
		new FavouriteDishDownloadImageTask().execute(dishNames);
	}
		
	private class FavouriteDishDownloadImageTask extends AsyncTask<String, Void, HashMap<String,Bitmap>>{
		@Override
		protected HashMap<String,Bitmap> doInBackground(String... dishNames) {
			HashMap<String,Bitmap> thumbnailsMap = new HashMap<String, Bitmap>();
			for (String dishName:dishNames){
				URL url = DataMarkerWebServicesURL.getDishThumbnailURL(dishName);
				Bitmap bmp = null;
				try {
					bmp = Utility.getBitmap(url.toString());
				} catch (IOException e) {
					bmp = null;
				}finally{
					if (bmp != null)
						thumbnailsMap.put(dishName, bmp);
				}
			}
			return thumbnailsMap;
		}
		
		protected void onPostExecute(HashMap<String,Bitmap> thumbnailsMap){
			mListener.onFavouriteDishThumbnailsDownloaded(thumbnailsMap);
		}
	}
	
	/**
	 * Call back Request Listener interface. 
	 * @author pszsa1
	 *
	 */
	public static interface FavouriteDishThumbnailsDownloadRequestListener{
		public void onFavouriteDishThumbnailsDownloaded(HashMap<String,Bitmap> thumbnailsMap);
	}
}
