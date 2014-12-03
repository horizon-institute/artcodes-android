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

package uk.ac.horizon.aestheticodes.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ExperienceManager
{
	private static final String TAG = ExperienceManager.class.getName();
	private static final Gson gson = createParser();
	private static ExperienceManager experienceManager;

	private static Gson createParser()
	{
		GsonBuilder build = new GsonBuilder();
		build.registerTypeAdapter(new TypeToken<Map<String, Marker>>()
		{}.getType(), new MarkerMapAdapter());
		return build.create();
	}

	private static HttpURLConnection createConnection(final String url, final String etag) throws IOException
	{
		Log.i(TAG, "Creating connection for " + url);
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setReadTimeout(10000 /* milliseconds */);
		connection.setConnectTimeout(15000 /* milliseconds */);
		connection.setRequestMethod("GET");
		connection.setUseCaches(false);

		if (etag != null)
		{
			connection.setRequestProperty("If-None-Match", etag);
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

	public static ExperienceManager get(Context context)
	{
		if (experienceManager == null)
		{
			experienceManager = new ExperienceManager(context);
		}
		return experienceManager;
	}

	private class LoadExperiences extends AsyncTask<Void, Experience, Void>
	{
		@SuppressWarnings("unchecked")
		private void readExperienceURLs(Map<String, String> experienceURLs, String experiencesURL) throws IOException
		{
			URL rootURL = new URL(experiencesURL);

			Map<String, String> newExperienceInfo = read(experienceURLs.getClass(), createConnection(experiencesURL, null));
			if (newExperienceInfo != null)
			{
				for (String experienceID : newExperienceInfo.keySet())
				{
					URL experienceURL = new URL(rootURL, newExperienceInfo.get(experienceID));
					experienceURLs.put(experienceID, experienceURL.toString());
					Log.i(TAG, experienceID + " = " + experienceURL.toString());
				}
			}
			else
			{
				Log.i(ExperienceManager.class.getName(), "New experiences == null");
			}
		}

		@Override
		protected void onProgressUpdate(Experience... newExperiences)
		{
			if (newExperiences != null)
			{
				for (Experience experience : newExperiences)
				{
					add(experience);
				}
			}
		}

		@Override
		protected Void doInBackground(Void... params)
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
					if (dir.exists())
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
									try
									{
										Experience experience = gson.fromJson(new FileReader(new File(context.getFilesDir(), "experiences/" + experienceName)), Experience.class);
										experienceMap.put(experience.getId(), experience);
										if (experience.getUpdateURL() != null)
										{
											experienceURLs.put(experience.getId(), experience.getUpdateURL());
										}
										publishProgress(experience);
									}
									catch (Exception e)
									{
										Log.w(TAG, "Failed to load settings", e);
									}
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
							try
							{
								Experience experience = gson.fromJson(new InputStreamReader(context.getAssets().open("experiences/" + experienceName)), Experience.class);
								experienceMap.put(experience.getId(), experience);
								if (experience.getUpdateURL() != null)
								{
									experienceURLs.put(experience.getId(), experience.getUpdateURL());
								}
								publishProgress(experience);
							}
							catch (Exception e)
							{
								Log.w(TAG, "Failed to load settings", e);
							}
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
					readExperienceURLs(experienceURLs, "http://aestheticodes.appspot.com/experiences/experiences.json");

					for (String experienceID : experienceURLs.keySet())
					{
						try
						{
							if (experienceURLs.get(experienceID) != null)
							{

								Experience experience = experienceMap.get(experienceID);
								String etag = null;
								if (experience != null)
								{
									etag = experience.getEtag();
								}
								HttpURLConnection connection = createConnection(experienceURLs.get(experienceID), etag);
								experience = read(Experience.class, connection);
								if (experience != null)
								{
									experience.setUpdateURL(experienceURLs.get(experienceID));
									experience.setEtag(connection.getHeaderField("ETag"));
									experience.setChanged(true);
									experienceMap.put(experience.getId(), experience);
									publishProgress(experience);
								}
							}
						}
						catch (IOException e)
						{
							Log.e(TAG, e.getMessage(), e);
						}
					}
				}
			}
			catch (IOException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}
	}

	private final Map<String, Experience> experiences = new HashMap<String, Experience>();
	private final Context context;
	private final Collection<ExperienceEventListener> listeners = new HashSet<ExperienceEventListener>();
	private Experience selected;

	public ExperienceManager(Context context)
	{
		this.context = context;
	}

	public void addListener(ExperienceEventListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(ExperienceEventListener listener)
	{
		listeners.remove(listener);
	}

	public Experience getSelected()
	{
		return selected;
	}

	public void setSelected(final Experience selected)
	{
		this.selected = selected;
		for (final ExperienceEventListener listener : listeners)
		{
			listener.experienceSelected(selected);
		}
	}

	public Experience get(String id)
	{
		synchronized (experiences)
		{
			if (experiences.containsKey(id))
			{
				return experiences.get(id);
			}
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

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void save(Experience experience)
	{
		// TODO Check if exists
		if (experience.hasChanged())
		{
			try
			{
				File file = new File(context.getFilesDir(), "experiences/" + experience.getId() + ".json");
				if (!file.exists())
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

	public void add(Experience experience)
	{
		if (experience == null)
		{
			return;
		}
		synchronized (experiences)
		{
			experiences.put(experience.getId(), experience);
		}
		if (selected == null)
		{
			setSelected(experience);
		}
		else if (selected.getId().equals(experience.getId()))
		{
			setSelected(experience);
		}

		save(experience);

		for (final ExperienceEventListener listener : listeners)
		{
			listener.experiencesChanged();
		}
	}

	public void delete(Experience experience)
	{
		// TODO
		if(selected != null && selected.getId().equals(experience.getId()))
		{
			// TODO
			setSelected(null);
		}

		experiences.remove(experience.getId());

		File file = new File(context.getFilesDir(), "experiences/" + experience.getId() + ".json");
		if (file.exists())
		{
			if(!file.delete())
			{
				Log.i("", "Experience not deleted?");
			}
		}

		for (final ExperienceEventListener listener : listeners)
		{
			listener.experiencesChanged();
		}
	}

	public Collection<Experience> getExperiences()
	{
		synchronized (experiences)
		{
			return experiences.values();
		}
	}

	public void load()
	{
		new LoadExperiences().execute();
	}

	private Experience load(String id)
	{
		synchronized (experiences)
		{
			if (experiences.containsKey(id))
			{
				return experiences.get(id);
			}
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

