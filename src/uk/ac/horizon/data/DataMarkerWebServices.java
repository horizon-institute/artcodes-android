/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
			return DtouchMarkersDataSource.getDtouchMarkerUsingKey(data[0], mContext);
		}

		protected void onPostExecute(DataMarker marker)
		{
			mListener.onMarkerDownloaded(marker);
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
