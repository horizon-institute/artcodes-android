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
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.JsonCallback;
import uk.ac.horizon.artcodes.server.LoadCallback;
import uk.ac.horizon.artcodes.server.URILoaderCallback;

public class AppEngineAccount implements Account
{
	static final String appSavePrefix = "appSaveID:";
	private static final String httpRoot = "http://aestheticodes.appspot.com/";
	private static final String httpsRoot = "https://aestheticodes.appspot.com/";

	private final Context context;
	private final Gson gson;
	private final String name;
	private final android.accounts.Account account;
	private final Map<String, AppEngineUploadThread> uploadThreads = new HashMap<>();

	public AppEngineAccount(Context context, String name, Gson gson)
	{
		this.context = context;
		this.name = name;
		this.gson = gson;
		this.account = new android.accounts.Account(name, "com.google");
	}

	@Override
	public boolean validates() throws UserRecoverableAuthException
	{
		try
		{
			return getToken() != null;
		}
		catch (UserRecoverableAuthException e)
		{
			throw e;
		}
		catch (GoogleAuthException | IOException e)
		{
			GoogleAnalytics.trackException(e);
			return false;
		}
	}

	@Override
	public boolean isLocal()
	{
		return false;
	}

	@Override
	public void loadLibrary(final LoadCallback<List<String>> callback)
	{
		load("https://aestheticodes.appspot.com/experiences", new JsonCallback<>(new TypeToken<List<String>>()
		{
		}.getType(), gson, context, new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> item)
			{
				SharedPreferences.Editor editor = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
				for (String uri : item)
				{
					editor.putString(uri, getId());
				}
				editor.apply();
				callback.loaded(item);
			}
		}));
	}

	public void setDisplayName(String displayName)
	{
		context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit()
				.putString(getId(), displayName)
				.apply();
	}

	@Override
	public boolean load(final String uri, final URILoaderCallback callback)
	{
		AppEngineUploadThread uploadThread = uploadThreads.get(uri);
		if (uploadThread != null)
		{
			Experience experience = uploadThread.getExperience();
			callback.onLoaded(new StringReader(gson.toJson(experience)));
			return true;
		}

		if (uri.startsWith(httpRoot) || uri.startsWith(httpsRoot))
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String url = uri.replace(httpRoot, httpsRoot);
						final Request request = new Request.Builder()
								.get()
								.url(url)
								.headers(getHeaders())
								.build();

						final Response response = Artcodes.httpClient.newCall(request).execute();

						if (validResponse(request, response))
						{
							callback.onLoaded(response.body().charStream());
							response.body().close();
						}
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

	@Override
	public void saveExperience(final Experience experience)
	{
		if (experience.getId() == null)
		{
			experience.setId(appSavePrefix + UUID.randomUUID().toString());
		}
		AppEngineUploadThread uploadThread = new AppEngineUploadThread(this, experience);
		uploadThreads.put(experience.getId(), uploadThread);
		uploadThread.start();
	}

	@Override
	public void deleteExperience(final Experience experience)
	{
		if (!canEdit(experience.getId()))
		{
			return;
		}
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final Request request = new Request.Builder()
							.delete()
							.url(experience.getId())
							.headers(getHeaders())
							.build();

					final Response response = Artcodes.httpClient.newCall(request).execute();
					if (validResponse(request, response))
					{
						// TODO
					}
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}
			}
		}).start();
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Account && ((Account) o).getId().equals(getId());
	}

	@Override
	public String getId()
	{
		return "google:" + name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public String getDisplayName()
	{
		return context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).getString(getId(), name);
	}

	@Override
	public boolean canEdit(String uri)
	{
		final SharedPreferences preferences = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE);
		final String account = preferences.getString(uri, null);
		return account != null && account.equals(getId());
	}

	@Override
	public boolean isSaving(String uri)
	{
		try
		{
			AppEngineUploadThread uploadThread = uploadThreads.get(uri);
			return uploadThread != null && !uploadThread.isFinished();
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}
		return false;
	}

	@Override
	public boolean logScan(final String uri)
	{
		if (uri != null && (uri.startsWith("http:") || (uri.startsWith("https:"))))
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final RequestBody body = new FormBody.Builder()
								.add("experience", uri)
								.build();

						final Request request = new Request.Builder()
								.post(body)
								.url("https://aestheticodes.appspot.com/interaction")
								.headers(getHeaders())
								.build();

						final Response response = Artcodes.httpClient.newCall(request).execute();
						if (validResponse(request, response))
						{
							// TODO
						}
					}
					catch (Exception e)
					{
						GoogleAnalytics.trackException(e);
					}
				}
			}).start();
			return true;
		}
		return false;
	}

	Headers getHeaders()
	{
		final Headers.Builder headers = new Headers.Builder();

		headers.set("User-Agent", Artcodes.userAgent);

		try
		{
			String token = getToken();
			if (token != null)
			{
				headers.set("Authorization", "Bearer " + token);
			}
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}

		return headers.build();
	}

	Gson getGson()
	{
		return gson;
	}

	Context getContext()
	{
		return context;
	}

	boolean validResponse(Request request, Response response)
	{
		if (response.code() == 401)
		{
			Log.w("", "Response " + response.code());
			String authHeader = request.header("Authorization");
			if (authHeader != null)
			{
				String token = authHeader.split(" ")[1];
				try
				{
					GoogleAuthUtil.clearToken(context, token);
				}
				catch (GoogleAuthException | IOException e)
				{
					GoogleAnalytics.trackException(e);
				}
			}
			return false;
		}
		else if (response.code() != 200)
		{
			Log.w("", "Response " + response.code() + ": " + response.message());
			return false;
		}
		return true;
	}


	private String getToken() throws IOException, GoogleAuthException
	{
		return GoogleAuthUtil.getToken(context, account, "oauth2:email");
	}
}
