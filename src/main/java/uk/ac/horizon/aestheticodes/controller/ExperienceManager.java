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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.detect.ExperienceEventListener;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExperienceManager
{
	private static final class ExperienceResults
	{
		private final Map<String, Experience> experiences = new HashMap<>();
	}

	private static final class ExperienceUpdates
	{
		private final List<Object> experiences = new ArrayList<>();
	}

	private static final class ExperienceOp
	{
		private final String id;
		private final Experience.Operation op;
		private final Integer version;

		public ExperienceOp(String id, Experience.Operation op)
		{
			this.id = id;
			this.op = op;
			this.version = null;
		}

		public ExperienceOp(String id, int version)
		{
			this.id = id;
			this.op = null;
			this.version = version;
		}
	}

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

	public static ExperienceManager get(Context context)
	{
		if (experienceManager == null)
		{
			experienceManager = new ExperienceManager(context);
		}
		return experienceManager;
	}

	private final class UpdateExperiences extends AsyncTask<Void, Experience, Collection<String>>
	{
		@Override
		protected Collection<String> doInBackground(Void... params)
		{
			Set<String> removals = new HashSet<>();
			try
			{
				Collection<Experience> experienceList = null;
				if (!loaded)
				{
					Log.i("", "Loading...");
					try
					{
						Experience[] experiencesLoaded = null;
						File experienceFile = new File(context.getFilesDir(), "experiences.json");
						if (experienceFile.exists())
						{
							try
							{
								Log.i("", "Loading " + experienceFile.getAbsolutePath());
								experiencesLoaded = gson.fromJson(new FileReader(experienceFile), Experience[].class);
							}
							catch (Exception e)
							{
								Log.w("", e.getMessage(), e);
							}
						}

						if (experiencesLoaded == null)
						{
							Log.i("", "Loading default.json");
							experiencesLoaded = gson.fromJson(new InputStreamReader(context.getAssets().open("default.json")), Experience[].class);
						}

						if (experiencesLoaded != null)
						{
							Log.i("", "Loaded");
							publishProgress(experiencesLoaded);
							loaded = true;
							experienceList = Arrays.asList(experiencesLoaded);
						}
					}
					catch (Exception e)
					{
						Log.w(TAG, "Failed to load settings", e);
					}
				}

				if (experienceList == null)
				{
					experienceList = experiences.values();
				}

				Log.i(TAG, "Updating...");
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				if (netInfo != null && netInfo.isConnectedOrConnecting())
				{
					boolean changes = false;
					final ExperienceUpdates updates = new ExperienceUpdates();
					for (Experience experience : experienceList)
					{
						Log.i("", experience.getId());
						if (experience.getOp() == null || experience.getOp() == Experience.Operation.retrieve)
						{
							updates.experiences.add(new ExperienceOp(experience.getId(), experience.getVersion()));
						}
						else if (experience.getOp() == Experience.Operation.remove)
						{
							updates.experiences.add(new ExperienceOp(experience.getId(), Experience.Operation.remove));
							changes = true;
						}
						else if(experience.getOp() != Experience.Operation.temp)
						{
							updates.experiences.add(experience);
							changes = true;
						}

					}

					if (changes || !init)
					{
						HttpResponse response = put("https://aestheticodes.appspot.com/_ah/api/experiences/v1/experiences", updates);
						if (response.getStatusLine().getStatusCode() == 200)
						{
							init = true;
							ExperienceResults results = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), ExperienceResults.class);
							for (String experienceID : results.experiences.keySet())
							{
								Experience experience = results.experiences.get(experienceID);
								if (experience.getId() == null)
								{
									experience.setId(experienceID);
								}

								if (!experienceID.equals(experience.getId()))
								{
									publishProgress(experience);
									removals.add(experienceID);
								}
								else if (experience.getOp() == Experience.Operation.remove)
								{
									removals.add(experience.getId());
								}
								else
								{
									publishProgress(experience);
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}

			return removals;
		}

		@Override
		protected void onPostExecute(Collection<String> removals)
		{
			for (String experienceID : removals)
			{
				Log.i("", "Removing " + experienceID);
				experiences.remove(experienceID);

				if (selected != null && selected.getId().equals(experienceID))
				{
					if (experiences.size() >= 1)
					{
						setSelected(experiences.values().iterator().next());
					}
					else
					{
						Experience experience = new Experience();
						experience.setName("Aestheticodes");
						setSelected(experience);
					}
				}
			}
			save();

			for (final ExperienceEventListener listener : listeners)
			{
				listener.experiencesChanged();
			}
		}

		@Override
		protected void onProgressUpdate(Experience... newExperiences)
		{
			if (newExperiences == null)
			{
				return;
			}

			for (Experience experience : newExperiences)
			{
				synchronized (experiences)
				{
					experiences.put(experience.getId(), experience);
				}
				if (selected != null && selected.getId().equals(experience.getId()))
				{
					setSelected(experience);
				}
			}

			if (selected == null)
			{
				SharedPreferences preferences = context.getSharedPreferences(ExperienceManager.class.getSimpleName(), Context.MODE_PRIVATE);
				Log.i(TAG, "Loading experience " + preferences.getString("experience", "uk.ac.horizon.aestheticodes.default"));
				Experience experience = experienceManager.get(preferences.getString("experience", "uk.ac.horizon.aestheticodes.default"));
				if (experience != null)
				{
					experienceManager.setSelected(experience);
				}
			}

			for (final ExperienceEventListener listener : listeners)
			{
				listener.experiencesChanged();
			}
		}
	}

	private final Map<String, Experience> experiences = new HashMap<>();
	private final Context context;
	private final Collection<ExperienceEventListener> listeners = new HashSet<>();
	private Experience selected;
	private boolean loaded = false;
	private boolean init = false;

	public ExperienceManager(Context context)
	{
		this.context = context;
	}


	public void add(Experience experience)
	{
		if (experience == null || experience.getId() == null)
		{
			return;
		}
		Log.i("", "Adding " + experience.getId());
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

		for (final ExperienceEventListener listener : listeners)
		{
			listener.experiencesChanged();
		}
	}

	public void addListener(ExperienceEventListener listener)
	{
		listeners.add(listener);
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
		return null;
	}

	public Collection<Experience> getExperiences()
	{
		synchronized (experiences)
		{
			return experiences.values();
		}
	}

	public Experience getSelected()
	{
		return selected;
	}

	public void setSelected(final Experience selected)
	{
		this.selected = selected;

		context.getSharedPreferences(ExperienceManager.class.getSimpleName(), Context.MODE_PRIVATE).edit().putString("experience", selected.getId()).commit();

		for (final ExperienceEventListener listener : listeners)
		{
			listener.experienceSelected(selected);
		}
	}

	public void handleIntent(Intent intent)
	{
		if (intent == null)
		{
			return;
		}

		String experienceID = intent.getStringExtra("experienceID");
		if (experienceID != null)
		{
			Experience experience = experienceManager.get(experienceID);
			if (experience != null)
			{
				experienceManager.setSelected(experience);
			}
		}
		String experienceJSON = intent.getStringExtra("experience");
		if (experienceJSON != null)
		{
			try
			{
				Experience experience = gson.fromJson(experienceJSON, Experience.class);
				if (experience != null)
				{
					add(experience);
					setSelected(experience);
				}
			}
			catch(Exception e)
			{
				Log.w("", e.getMessage(), e);
			}
		}

	}

	public void load()
	{
		new UpdateExperiences().execute();
	}

	public void removeListener(ExperienceEventListener listener)
	{
		listeners.remove(listener);
	}

	public void save()
	{
		try
		{
			final File experienceFile = new File(context.getFilesDir(), "experiences.json");
			final FileWriter writer = new FileWriter(experienceFile);
			final Collection<Experience> saveExperiences = new ArrayList<>();
			synchronized (experiences)
			{
				for (Experience experience : experiences.values())
				{
					if (experience.getOp() != Experience.Operation.temp)
					{
						saveExperiences.add(experience);
					}
				}
			}
			Log.i(TAG, "Saving: " +  gson.toJson(saveExperiences));
			gson.toJson(saveExperiences, writer);

			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
	}

	private HttpResponse put(String url, Object data) throws Exception
	{
		HttpClient client = new DefaultHttpClient();
		HttpPut put = new HttpPut(new URL(url).toURI());
		put.addHeader("content-type", "application/json");
		put.setEntity(new StringEntity(gson.toJson(data)));

		String token = null;
		try
		{
			AccountManager accountManager = AccountManager.get(context);
			Account[] accounts = accountManager.getAccountsByType("com.google");
			if (accounts.length >= 1)
			{
				Log.i("", "Getting token for " + accounts[0].name);
				token = GoogleAuthUtil.getToken(context, accounts[0].name, context.getString(R.string.app_scope));
				put.addHeader("Authorization", "Bearer " + token);
				Log.i("", token);
			}
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}

		Log.i("", "PUT " + url);
		Log.i("", gson.toJson(data));
		HttpResponse response = client.execute(put);
		if (response.getStatusLine().getStatusCode() == 401)
		{
			Log.w("", "Response " + response.getStatusLine().getStatusCode());
			if (token != null)
			{
				GoogleAuthUtil.invalidateToken(context, token);
			}
		}
		else if (response.getStatusLine().getStatusCode() != 200)
		{
			Log.w("", "Response " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
		}

		return response;
	}
}

