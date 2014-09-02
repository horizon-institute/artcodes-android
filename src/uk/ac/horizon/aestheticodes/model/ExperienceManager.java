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
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionListener;
import uk.ac.horizon.aestheticodes.detect.MarkerDetectionThread;
import uk.ac.horizon.aestheticodes.settings.MarkerMapAdapter;

import java.io.File;
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

	private static HttpURLConnection createConnection(final String url, final Date lastModified) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setReadTimeout(10000 /* milliseconds */);
		connection.setConnectTimeout(15000 /* milliseconds */);
		connection.setRequestMethod("GET");
		connection.setUseCaches(true);
		if (lastModified != null)
		{
			connection.setIfModifiedSince(lastModified.getTime());
		}

		return connection;
	}

	private static <T> T read(Class<T> clazz, HttpURLConnection connection) throws IOException
	{
		connection.connect();
		int response = connection.getResponseCode();
		Log.i(TAG, "The response is: " + response);
		if (response == HttpURLConnection.HTTP_OK)
		{
			try
			{
				Reader reader = new InputStreamReader(connection.getInputStream());
				return gson.fromJson(reader, clazz);
			}
			catch (JsonSyntaxException e)
			{
				Log.e(TAG, "There was a syntax problem with the downloaded JSON, maybe a proxy server is forwarding us to a login page?", e);
				return null;
			}
		}
		return null;
	}

	private class DownloadSettings extends AsyncTask<Experience, Void, Iterable<Experience>>
	{

		@Override
		protected Iterable<Experience> doInBackground(Experience... ignored)
		{
			try
			{
				final Map<String, Experience> experienceMap = new HashMap<String, Experience>();
				final Map<String, String> experienceURLs = new HashMap<String, String>();

				for (Experience experience : experiences.values())
				{
					experienceMap.put(experience.getId(), experience);
					experienceURLs.put(experience.getId(), experience.getUpdateURL());
				}

				try
				{
					File dir = new File(context.getFilesDir(), "experiences");
					if(dir.exists())
					{
						String[] experienceNames = dir.list();
						for (String experienceName : experienceNames)
						{
							// Strip json
							if (experienceName.endsWith(".json"))
							{
								String name = experienceName.substring(0, experienceName.length() - 5);
								if (!experienceMap.containsKey(name))
								{
									Reader reader = new FileReader(new File(context.getFilesDir(), "experiences/" + experienceName));
									Experience experience = gson.fromJson(reader, Experience.class);
									experienceMap.put(experience.getId(), experience);
									experienceURLs.put(experience.getId(), experience.getUpdateURL());
								}
							}
						}
					}

					String[] experienceNames = context.getAssets().list("experiences");
					for (String experienceName : experienceNames)
					{
						// Strip json
						String name = experienceName.substring(0, experienceName.length() - 5);
						if (!experienceMap.containsKey(name))
						{
							Reader reader = new InputStreamReader(context.getAssets().open("experiences/" + experienceName));
							Experience experience = gson.fromJson(reader, Experience.class);
							experienceMap.put(experience.getId(), experience);
							experienceURLs.put(experience.getId(), experience.getUpdateURL());
						}
					}

				}
				catch (Exception e)
				{
					Log.w(TAG, "Failed to load settings", e);
				}

				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				if (netInfo != null && netInfo.isConnectedOrConnecting())
				{

					HttpURLConnection connection = createConnection("http://www.wornchaos.org/experiences/experiences.json", null);
					Map newExperienceInfo = read(experienceURLs.getClass(), connection);
					if (newExperienceInfo != null)
					{
						experienceURLs.putAll(newExperienceInfo);
					}

					for (String experienceID : experienceURLs.keySet())
					{
						try
						{
							Experience experience = experienceMap.get(experienceID);
							Date lastModified = null;
							if (experience != null)
							{
								lastModified = experience.getLastUpdate();
							}
							connection = createConnection(experienceURLs.get(experienceID), lastModified);
							experience = read(Experience.class, connection);
							if (experience != null)
							{
								experience.setLastUpdate(new Date(connection.getLastModified()));
								experience.setChanged(true);
								experienceMap.put(experience.getId(), experience);
							}
						}
						catch (IOException e)
						{
							Log.e(TAG, e.getMessage(), e);
						}
					}
				}

				return experienceMap.values();
			}
			catch (IOException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
			return null;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Iterable<Experience> newExperiences)
		{
			for (Experience experience : newExperiences)
			{
				add(experience);
			}

			if(listener != null)
			{
				listener.experiencesChanged();
			}
		}
	}

	private final Map<String, Experience> experiences = new HashMap<String, Experience>();
	private final Context context;
	private final MarkerDetectionListener listener;

	public ExperienceManager(Context context, MarkerDetectionListener listener)
	{
		this.context = context;
		this.listener = listener;
	}

	public Experience get(String id)
	{
		if (experiences.containsKey(id))
		{
			return experiences.get(id);
		}
		return load(id);
	}

	private Reader loadFile(String path) throws IOException
	{
		File file = new File(context.getFilesDir(), path);
		if (file.exists())
		{
			return new FileReader(file);
		}

		return new InputStreamReader(context.getAssets().open(path));
	}

	public void add(Experience experience)
	{
		experiences.put(experience.getId(), experience);
		if (experience.hasChanged())
		{
			try
			{
				File file = new File(context.getFilesDir(), "experiences/" + experience.getId() + ".json");
				if(!file.exists())
				{
					File dir = new File(context.getFilesDir(), "experiences");
					dir.mkdirs();
					file.createNewFile();
				}
				final FileWriter writer = new FileWriter(file);
				gson.toJson(experience, writer);
				experience.setChanged(false);
				writer.flush();
				writer.close();
				Log.i(TAG, "Saved: " + gson.toJson(experience));
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
		new DownloadSettings().execute();
	}

	private Experience load(String id)
	{
		if (experiences.containsKey(id))
		{
			return experiences.get(id);
		}

		try
		{
			Reader reader = loadFile("experiences/" + id + ".json");
			if (reader != null)
			{
				Experience experience = gson.fromJson(reader, Experience.class);
				if (experience != null)
				{
					add(experience);
					return experience;
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}
}

