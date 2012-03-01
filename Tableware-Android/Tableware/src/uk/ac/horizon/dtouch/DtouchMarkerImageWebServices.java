package uk.ac.horizon.dtouch;

import java.net.URL;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.facebook.android.Utility;

public class DtouchMarkerImageWebServices{
	
	private MarkerImageDownloadRequestListener mListener;
	
	public DtouchMarkerImageWebServices(MarkerImageDownloadRequestListener listener){
		mListener = listener;
	}
	
	public void executeMarkerRequest(String code){
		String[] params = new String[1];
		params[0] = code;
		new DtouchMarkerDownloadImageTask().execute(code);
	}
		
	private class DtouchMarkerDownloadImageTask extends AsyncTask<String, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... codes) {
			String code = codes[0];
			URL url = DtouchMarkerWebServicesURL.getMarkerImageURL(code);
			return Utility.getBitmap(url.toString());
		}
		
		protected void onPostExecute(Bitmap bmp){
			mListener.onMarkerImageDownloaded(bmp);
		}
	}
	
	/**
	 * Call back Request Listener interface. 
	 * @author pszsa1
	 *
	 */
	public static interface MarkerImageDownloadRequestListener{
		public void onMarkerImageDownloaded(Bitmap bmp);
	}
	
}