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

package uk.ac.horizon.aestheticodes.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import uk.ac.horizon.aestheticodes.settings.MarkerMapAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperienceManager
{
	private static final String TAG = ExperienceManager.class.getName();
	private static final Gson gson = createParser();

	private static Gson createParser()
	{
		GsonBuilder build = new GsonBuilder();
		build.registerTypeAdapter(new TypeToken<Map<String, MarkerAction>>()
		{}.getType(), new MarkerMapAdapter());
		return build.create();
	}

	private class DownloadSettings extends AsyncTask<Experience, Void, Experience>
	{
		@Override
		protected Experience doInBackground(Experience... experiences)
		{
			try
			{
				Experience experience = experiences[0];
				URL url = new URL(experience.getUpdateURL());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setUseCaches(true);
				if (experience.getLastUpdate() != null)
				{
					conn.setIfModifiedSince(experience.getLastUpdate().getTime());
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
						Experience newSettings = gson.fromJson(reader, Experience.class);
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
		protected void onPostExecute(Experience newSettings)
		{
			if (newSettings != null)
			{
				add(newSettings);
			}
		}
	}

	private final Map<String, Experience> experiences = new HashMap<String, Experience>();
	private final Context context;

	public ExperienceManager(Context context)
	{
		this.context = context;
	}

	public Experience get(String id)
	{
		if (experiences.containsKey(id))
		{
			return experiences.get(id);
		}
		return load(id);
	}

	public void add(Experience settings)
	{
		experiences.put(settings.getId(), settings);
		if (settings.hasChanged())
		{
			try
			{
				final FileWriter writer = new FileWriter(new File(context.getFilesDir(), settings.getId() + ".json"));
				gson.toJson(settings, writer);
				settings.setChanged(false);
				writer.flush();
				writer.close();
				Log.i(TAG, "Saving: " + gson.toJson(settings));
			}
			catch (Exception e)
			{
				Log.w(TAG, "Failed to save settings", e);
			}
		}
	}

	public List<Experience> list()
	{
		List<Experience> list = new ArrayList<Experience>(experiences.values());
		Collections.sort(list, new Comparator<Experience>()
		{
			@Override
			public int compare(Experience experience, Experience experience2)
			{
				return experience.getName().compareTo(experience2.getName());
			}
		});

		return list;
	}

	public void load()
	{
		try
		{
			String[] experienceNames = context.getAssets().list("experiences");
			for(String experienceName: experienceNames)
			{
				// Strip json
				String name = experienceName.substring(0, experienceName.length() - 5);
				if(!experiences.containsKey(name))
				{
					load(name);
				}
			}

			experienceNames = context.getFilesDir().list();
			for(String experienceName: experienceNames)
			{
				// Strip json
				if(experienceName.endsWith(".json"))
				{
					String name = experienceName.substring(0, experienceName.length() - 5);
					if (!experiences.containsKey(name))
					{
						load(name);
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.w(TAG, "Failed to load settings", e);
		}
	}


	private Experience load(String id)
	{
		Log.i(TAG, "Loading " + id);
		try
		{

			InputStreamReader reader = new InputStreamReader(context.getAssets().open("experiences/" + id + ".json"));
			add(gson.fromJson(reader, Experience.class));
		}
		catch (Exception e)
		{
			Log.w(TAG, "Failed to load settings", e);
		}

		try
		{
			final FileReader reader = new FileReader(new File(context.getFilesDir(), id + ".json"));
			add(gson.fromJson(reader, Experience.class));
		}
		catch(FileNotFoundException e)
		{
			// Do nothing!
		}
		catch (Exception e)
		{
			Log.w(TAG, "Failed to load settings", e);
		}

		Experience experience = experiences.get(id);
		if (experience != null)
		{
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected())
			{
				new DownloadSettings().execute(experience);
			}
			else
			{
				Log.i(TAG, "No network available");
			}
		}
		return experience;
	}
}

