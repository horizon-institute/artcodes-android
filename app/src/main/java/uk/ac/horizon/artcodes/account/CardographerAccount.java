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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import uk.ac.horizon.artcodes.Analytics;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.CardographerServer;
import uk.ac.horizon.artcodes.server.HTTPException;
import uk.ac.horizon.artcodes.server.JsonCallback;
import uk.ac.horizon.artcodes.server.LoadCallback;
import uk.ac.horizon.artcodes.server.URILoaderCallback;

public class CardographerAccount implements Account {
	private boolean numberOfExperiencesHasChangedHint = false;
	private final Map<String, Object> experienceUrlsThatHaveChangedHint = new ConcurrentHashMap<>();

	static final String appSavePrefix = "appSaveID:";

	private final String name;
	private final CardographerServer server;
	private final Context context;
	private final Gson gson;
	private final android.accounts.Account account;
	private final Map<String, CardographerUploadThread> uploadThreads = new HashMap<>();

	public CardographerAccount(CardographerServer server, Context context, Gson gson, String name) {
		this.server = server;
		this.context = context;
		this.gson = gson;
		this.name = name;
		this.account = new android.accounts.Account(name, "com.google");
	}

	@Override
	public boolean validates() throws UserRecoverableAuthException {
		try {
			return getToken() != null;
		} catch (UserRecoverableAuthException e) {
			throw e;
		} catch (GoogleAuthException | IOException e) {
			Analytics.trackException(e);
			return false;
		}
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public void loadLibrary(final LoadCallback<List<String>> callback) {
		load(server.getRootURL() + "experiences", new JsonCallback<>(new TypeToken<List<String>>() {
		}.getType(), gson, context, new LoadCallback<List<String>>() {
			@Override
			public void loaded(List<String> item) {
				SharedPreferences.Editor editor = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
				for (String uri : item) {
					editor.putString(uri, getId());
				}
				editor.apply();
				callback.loaded(item);
			}

			@Override
			public void error(Throwable e) {
				callback.error(e);
			}
		}));
	}

	public void setDisplayName(String displayName) {
		context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit()
				.putString(getId(), displayName)
				.apply();
	}

	@Override
	public boolean load(final String uri, final URILoaderCallback callback) {
		CardographerUploadThread uploadThread = uploadThreads.get(uri);
		if (uploadThread != null) {
			Experience experience = uploadThread.getExperience();
			callback.onLoaded(new StringReader(gson.toJson(experience)));
			return true;
		}

		if (uri.startsWith(server.getRootURL())) {
			new Thread(() -> {
				try {
					CacheControl cc;
					if (numberOfExperiencesHasChangedHint && uri.equals(server.getRootURL() + "experiences")) {
						numberOfExperiencesHasChangedHint = false;
						cc = CacheControl.FORCE_NETWORK;
					} else if (experienceUrlsThatHaveChangedHint.containsKey(uri)) {
						experienceUrlsThatHaveChangedHint.remove(uri);
						cc = CacheControl.FORCE_NETWORK;
					} else {
						cc = new CacheControl.Builder().maxAge(5, TimeUnit.MINUTES).build();
					}

					final Request request = new Request.Builder()
							.get()
							.url(uri)
							.headers(getHeaders())
							.cacheControl(cc)
							.build();

					final Response response = Artcodes.httpClient.newCall(request).execute();

					validateResponse(request, response);
					callback.onLoaded(response.body().charStream());
					response.body().close();
				} catch (Exception e) {
					callback.onError(e);
				}
			}).start();
			return true;
		}
		return false;
	}


	@Override
	public void saveExperience(final Experience experience) {
		this.saveExperience(experience, null);
	}

	@Override
	public void saveExperience(final Experience experience, final AccountProcessCallback saveCallback) {
		if (experience.getId() == null) {
			experience.setId(appSavePrefix + UUID.randomUUID().toString());
			this.numberOfExperiencesHasChangedHint = true;
		} else {
			this.experienceUrlsThatHaveChangedHint.put(experience.getId(), new Object());
		}
		CardographerUploadThread uploadThread = new CardographerUploadThread(this, context, gson, server.getRootURL(), experience, saveCallback);
		uploadThreads.put(experience.getId(), uploadThread);
		uploadThread.start();
	}

	@Override
	public void deleteExperience(final Experience experience) {
		this.deleteExperience(experience, null);
	}

	@Override
	public void deleteExperience(final Experience experience, final AccountProcessCallback accountProcessCallback) {
		if (!canEdit(experience.getId())) {
			if (accountProcessCallback != null) {
				accountProcessCallback.accountProcessCallback(false, null);
			}
			return;
		}
		this.numberOfExperiencesHasChangedHint = true;
		new Thread(() -> {
			boolean success = true;
			try {
				String url = experience.getId();
				if (!experience.getId().startsWith("http:") && !experience.getId().startsWith("https:")) {
					url = server.getRootURL() + experience.getId();
				}
				final Request request = new Request.Builder()
						.delete()
						.url(url)
						.headers(getHeaders())
						.build();

				Log.i("Delete", "Request " + request);
				final Response response = Artcodes.httpClient.newCall(request).execute();
				validateResponse(request, response);
				server.removeID(experience.getId());
			} catch (Exception e) {
				success = false;
				Analytics.trackException(e);
			}

			if (accountProcessCallback != null) {
				accountProcessCallback.accountProcessCallback(success, null);
			}
		}).start();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Account && ((Account) o).getId().equals(getId());
	}

	@Override
	public String getId() {
		return "google:" + name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).getString(getId(), name);
	}

	@Override
	public boolean canEdit(String uri) {
		final SharedPreferences preferences = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE);
		final String account = preferences.getString(uri, null);
		return account != null && account.equals(getId());
	}

	@Override
	public boolean isSaving(String uri) {
		try {
			CardographerUploadThread uploadThread = uploadThreads.get(uri);
			return uploadThread != null && !uploadThread.isFinished();
		} catch (Exception e) {
			Analytics.trackException(e);
		}
		return false;
	}

	Headers getHeaders() {
		final Headers.Builder headers = new Headers.Builder();
		headers.set("User-Agent", Artcodes.userAgent);

		try {
			String token = getToken();
			if (token != null) {
				headers.set("Authorization", "Bearer " + token);
			}
		} catch (Exception e) {
			Analytics.trackException(e);
		}

		return headers.build();
	}

	void validateResponse(Request request, Response response) throws IOException {
		if (response.code() == 401) {
			Log.w("", "Response " + response.code());
			String authHeader = request.header("Authorization");
			if (authHeader != null) {
				String token = authHeader.split(" ")[1];
				try {
					GoogleAuthUtil.clearToken(context, token);
				} catch (GoogleAuthException | IOException e) {
					Analytics.trackException(e);
				}
			}
			throw new HTTPException(response.code(), response.message());
		} else if (response.code() != 200) {
			throw new HTTPException(response.code(), response.message());
		}
	}


	private String getToken() throws IOException, GoogleAuthException {
		return GoogleAuthUtil.getToken(context, account, "oauth2:email");
	}
}
