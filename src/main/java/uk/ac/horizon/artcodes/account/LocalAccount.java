/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Request;
import okhttp3.Response;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.LoadCallback;
import uk.ac.horizon.artcodes.server.URILoaderCallback;

public class LocalAccount implements Account
{
	private final Context context;
	private final Gson gson;

	public LocalAccount(Context context, Gson gson)
	{
		this.context = context;
		this.gson = gson;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public void loadLibrary(LoadCallback<List<String>> callback)
	{
		try
		{
			File directory = getDirectory();
			Log.i("local", "Listing " + directory.getAbsolutePath());
			List<String> result = new ArrayList<>();
			SharedPreferences.Editor editor = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
			for (final File file : directory.listFiles())
			{
				String uri = file.toURI().toString();
				result.add(uri);
				editor.putString(uri, getId());
			}
			editor.apply();
			callback.loaded(result);
		}
		catch (Exception e)
		{
			// TODO callback.onError(e);
		}
	}

	@Override
	public void saveExperience(final Experience experience)
	{
		try
		{
			File file;
			if (experience.getId() == null || !canEdit(experience.getId()))
			{
				if (experience.getId() != null && (experience.getId().startsWith("http://") || experience.getId().startsWith("https://")))
				{
					experience.setOriginalID(experience.getId());
				}
				file = new File(getDirectory(), UUID.randomUUID().toString());
				experience.setId(file.toURI().toString());
			}
			else
			{
				file = new File(URI.create(experience.getId()));
			}

			final FileWriter writer = new FileWriter(file);
			gson.toJson(experience, writer);
			writer.flush();
			writer.close();

			final SharedPreferences.Editor editor = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
			Log.i("local", experience.getId() + " = " + getId());
			editor.putString(experience.getId(), getId()).apply();
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Account && ((Account) o).getId().equals(getId());
	}

	@Override
	public String getId()
	{
		return "local";
	}

	@Override
	public String getName()
	{
		return context.getString(R.string.device);
	}

	@Override
	public boolean canEdit(String uri)
	{
		final File directory = getDirectory();
		final String directoryURI = directory.toURI().toString();
		return uri != null && uri.startsWith(directoryURI);
	}

	@Override
	public boolean isSaving(String uri)
	{
		return false;
	}

	@Override
	public boolean logScan(String uri)
	{
		return false;
	}

	@Override
	public boolean load(final String uri, final URILoaderCallback callback)
	{
		if (uri.startsWith("content:") || uri.startsWith("file:"))
		{
			try
			{
				callback.onLoaded(new InputStreamReader(context.getContentResolver().openInputStream(Uri.parse(uri))));
				return true;
			}
			catch (Exception e)
			{
				callback.onError(e);
			}
		}
		else if (uri.startsWith("http:") || uri.startsWith("https:"))
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final Request request = new Request.Builder()
								.get()
								.url(uri)
								.addHeader("User-Agent", Artcodes.userAgent)
								.build();
						final Response response = Artcodes.httpClient.newCall(request).execute();
						callback.onLoaded(response.body().charStream());
						response.body().close();
					}
					catch (Exception e)
					{
						callback.onError(e);
					}
				}
			}).start();
			return true;
		}
		return false;
	}

	public void deleteExperience(Experience experience)
	{
		try
		{
			File file = new File(URI.create(experience.getId()));
			file.delete();
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}
	}

	public boolean validates()
	{
		return true;
	}

	private File getDirectory()
	{
		return context.getDir("experiences", Context.MODE_PRIVATE);
	}
}
