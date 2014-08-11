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

package uk.ac.horizon.aestheticodes.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.model.MarkerSettings;
import uk.ac.horizon.aestheticodes.settings.MarkerMapAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class MarkerSettingsHelper
{
	private static final String TAG = MarkerSettingsHelper.class.getName();

	private static final Gson gson = createParser();
	private static final MarkerSettings settings = MarkerSettings.getSettings();

	private static class DownloadSettings extends AsyncTask<Void, Void, MarkerSettings>
	{
		private Context context;

		public DownloadSettings(Context context)
		{
			this.context = context;
		}

		@Override
		protected MarkerSettings doInBackground(Void... voids)
		{
			try
			{
				URL url = new URL(settings.getUpdateURL());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setUseCaches(true);
				if (settings.getLastUpdate() != null)
				{
					conn.setIfModifiedSince(settings.getLastUpdate().getTime());
				}
				// Starts the query
				conn.connect();
				int response = conn.getResponseCode();
				Log.i(TAG, "The response is: " + response);
				if (response == HttpURLConnection.HTTP_OK)
				{
                    try
                    {
                        Reader reader = new InputStreamReader(conn.getInputStream());
                        MarkerSettings newSettings = gson.fromJson(reader, MarkerSettings.class);
                        newSettings.setLastUpdate(new Date(conn.getLastModified()));

                        return newSettings;
                    }
                    catch (JsonSyntaxException e)
                    {
                        Log.e(TAG, "There was a syntax problem with the downloaded JSON, maybe a proxy server is forwarding us to a login page?", e);
                        return null;
                    }
				}
			}
			catch (IOException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
			return null;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(MarkerSettings newSettings)
		{
			if(newSettings != null)
			{
				settings.setSettings(newSettings);
				settings.setChanged(true);

				saveSettings(context);
			}
		}
	}

	private static Gson createParser()
	{
		GsonBuilder build = new GsonBuilder();
		build.registerTypeAdapter(new TypeToken<Map<String, MarkerAction>>()
		{}.getType(), new MarkerMapAdapter());
		return build.create();
	}

	public static void saveSettings(Context context)
	{
		if(settings.hasChanged())
		{
			try
			{
				final FileWriter writer = new FileWriter(new File(context.getFilesDir(), "settings.json"));
				gson.toJson(settings, writer);
				settings.setChanged(false);
				writer.flush();
				writer.close();
				Log.i(TAG, "Saving: "+ gson.toJson(settings));
			}
			catch (Exception e)
			{
				Log.w(TAG, "Failed to save settings", e);
			}
		}
	}

	public static void loadSettings(Context context)
	{
		try
		{
			InputStreamReader reader = new InputStreamReader(context.getAssets().open("settings.json"));
			MarkerSettings newSettings = gson.fromJson(reader, MarkerSettings.class);
			settings.setSettings(newSettings);
		}
		catch (Exception e)
		{
			Log.w(TAG, "Failed to load settings", e);
		}

		try
		{
			final FileReader reader = new FileReader(new File(context.getFilesDir(), "settings.json"));
			final MarkerSettings newSettings = gson.fromJson(reader, MarkerSettings.class);
			Log.i(TAG, gson.toJson(newSettings));
			settings.setSettings(newSettings);
		}
		catch (Exception e)
		{
			Log.w(TAG, "Failed to load settings", e);
		}

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
		{
			new DownloadSettings(context).execute();
		}
		else
		{
			Log.i(TAG, "No network available");
		}
	}
}
