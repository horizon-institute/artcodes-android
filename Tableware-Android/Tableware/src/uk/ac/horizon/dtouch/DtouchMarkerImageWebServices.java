package uk.ac.horizon.dtouch;

import java.io.IOException;
import java.net.URL;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.facebook.android.Utility;

public class DtouchMarkerImageWebServices{
	
	private MarkerImageDownloadRequestListener mListener;
	
	public DtouchMarkerImageWebServices(MarkerImageDownloadRequestListener listener){
		mListener = listener;
	}
	
	public void executeMarkerImageRequest(String code){
		URL[] params = new URL[1];
		params[0] = DtouchMarkerWebServicesURL.getMarkerImageURL(code);
		new DtouchMarkerDownloadImageTask().execute(params);
	}
	
	public void executeDishImageRequest(String title){
		URL[] params = new URL[1];
		params[0] = DtouchMarkerWebServicesURL.getDishImageURL(title);
		new DtouchMarkerDownloadImageTask().execute(params);
	}
		
	private class DtouchMarkerDownloadImageTask extends AsyncTask<URL, Void, Bitmap>{
		@Override
		protected Bitmap doInBackground(URL... params) {
			Bitmap bmp = null;
			URL url = params[0];
			if (url != null){
				try {
					bmp = Utility.getBitmap(url.toString());
				} catch (IOException e) {
					bmp = null;
				}
			}
			return bmp;
		}
		
		protected void onPostExecute(Bitmap bmp){
			if (bmp != null)
				mListener.onMarkerImageDownloaded(bmp);
			else
				mListener.onMarkerImageDownloadError();
		}
	}
	
	/**
	 * Call back Request Listener interface. 
	 * @author pszsa1
	 *
	 */
	public static interface MarkerImageDownloadRequestListener{
		public void onMarkerImageDownloaded(Bitmap bmp);
		public void onMarkerImageDownloadError();
	}
	
}