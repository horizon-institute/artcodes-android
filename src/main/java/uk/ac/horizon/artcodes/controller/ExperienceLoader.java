/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.horizon.artcodes.model.Experience;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExperienceLoader extends AsyncTask<String, Experience, Collection<Experience>>
{
	private static final String TAG = ExperienceLoader.class.getName();
	private static final String EXPERIENCE_STORE = "uk.ac.horizon.aestheticodes.experiences";

	private static final long UPDATE_TIMEOUT = 900000;

	private final Context context;

	public ExperienceLoader(Context context)
	{
		this.context = context;
	}

	@Override
	protected Collection<Experience> doInBackground(String... experienceURIs)
	{
		final Gson gson = new GsonBuilder().create();
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		final Map<String, Experience> experiences = new HashMap<>();

		for (final String experienceURI : experienceURIs)
		{
			try
			{
				// Try loading from preferences
				final String experienceJSON = preferences.getString(experienceURI, null);
				if (experienceJSON != null)
				{
					Experience experience = gson.fromJson(experienceJSON, Experience.class);
					if (experience != null)
					{
						experiences.put(experienceURI, experience);
						publishProgress(experience);
					}
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}

		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
		{
			for (final String experienceURI : experienceURIs)
			{
				try
				{
					Experience existing = experiences.get(experienceURI);
					if(existing != null)
					{
						Long lastUpdate = existing.getUpdated();

						if(lastUpdate != null && System.currentTimeMillis() - lastUpdate < UPDATE_TIMEOUT)
						{
							continue;
						}
					}
					// TODO Check uri to see if local
					// TODO if_not_modified kinda stuff
					String url = experienceURI;
					if (!url.contains("://"))
					{
						url = "http://aestheticodes.appspot.com/experience/" + experienceURI;
					}

					final HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
					connection.connect();
					if (connection.getResponseCode() == 200)
					{
						final Experience experience = gson.fromJson(new InputStreamReader(connection.getInputStream()), Experience.class);
						if (experience != null)
						{
							experience.setUpdated(System.currentTimeMillis());
							experiences.put(experienceURI, experience);
							preferences.edit().putString(experienceURI, gson.toJson(experience)).apply();
							publishProgress(experience);
						}
					}
				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}


		return experiences.values();
	}
}
