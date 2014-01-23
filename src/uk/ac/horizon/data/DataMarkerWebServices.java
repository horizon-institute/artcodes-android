package uk.ac.horizon.data;

import android.content.Context;
import android.os.AsyncTask;

public class DataMarkerWebServices
{
	private MarkerDownloadRequestListener mListener;
	private Context mContext;

	public DataMarkerWebServices(MarkerDownloadRequestListener listener)
	{
		mListener = listener;
	}

	public void executeMarkerRequestUsingCode(String code, String userId, Context context)
	{
		mContext = context;
		String[] params = new String[1];
		params[0] = code;
		new DataMarkerDownloadTask().execute(params);
	}

	DataMarker downloadMarkerData(String markerCode)
	{
		return null;
	}

	private class DataMarkerDownloadTask extends AsyncTask<String, Void, DataMarker>
	{
		protected DataMarker doInBackground(String... data)
		{
			DataMarker dataMarker = DtouchMarkersDataSource.getDtouchMarkerUsingKey(data[0], mContext);
			return dataMarker;
		}

		protected void onPostExecute(DataMarker marker)
		{
			// if (marker != null)
			mListener.onMarkerDownloaded(marker);
			// else
			// mListener.onMarkerDownloadError();
		}
	}

	/**
	 * Call back Request Listener interface.
	 * 
	 * @author pszsa1
	 * 
	 */
	public static interface MarkerDownloadRequestListener
	{
		public void onMarkerDownloaded(DataMarker marker);

		public void onMarkerDownloadError();

	}
}
