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
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.model.Experience;

class AppEngineUploadThread extends Thread
{
	private static final String rootHTTP = "http://aestheticodes.appspot.com/experience";
	private static final String rootHTTPS = "https://aestheticodes.appspot.com/experience";
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

	private final Experience experience;
	private final Account.AccountProcessCallback saveCallback;
	private final AppEngineAccount account;
	private boolean finished = false;

	AppEngineUploadThread(AppEngineAccount account, Experience experience, Account.AccountProcessCallback saveCallback)
	{
		this.account = account;
		this.experience = experience;
		this.saveCallback = saveCallback;
	}

	@Override
	public void run()
	{
		boolean success = true;
		Experience saved = null;
		try
		{
			final File tempFile = createTempFile(experience.getId());
			saveTempExperience(tempFile, experience);

			uploadImage(experience.getImage());
			uploadImage(experience.getIcon());

			saveTempExperience(tempFile, experience);

			final Request.Builder builder = new Request.Builder();
			if (experience.getId() != null && experience.getId().startsWith(rootHTTP))
			{
				builder.url(experience.getId().replace("http://", "https://"));
				builder.put(RequestBody.create(MEDIA_TYPE_JSON, account.getGson().toJson(experience)));
			}
			else if (experience.getId() != null && experience.getId().startsWith(rootHTTPS))
			{
				builder.url(experience.getId());
				builder.put(RequestBody.create(MEDIA_TYPE_JSON, account.getGson().toJson(experience)));
			}
			else if (experience.getId() != null && experience.getId().startsWith(AppEngineAccount.appSavePrefix))
			{
				// Remove temp id for upload
				final String id = experience.getId();
				experience.setId(null);
				builder.url(rootHTTPS);
				builder.post(RequestBody.create(MEDIA_TYPE_JSON, account.getGson().toJson(experience)));
				experience.setId(id);
			}
			else
			{
				builder.url(rootHTTPS);
				builder.post(RequestBody.create(MEDIA_TYPE_JSON, account.getGson().toJson(experience)));
			}
			builder.headers(account.getHeaders());

			final Request request = builder.build();
			final Response response = Artcodes.httpClient.newCall(request).execute();

			account.validateResponse(request, response);
			saved = account.getGson().fromJson(response.body().charStream(), Experience.class);
			response.body().close();

			account.getContext()
					.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE)
					.edit()
					.putString(saved.getId(), account.getId())
					.apply();

			if (!tempFile.delete())
			{
				Log.w("upload", "Temp file not deleted");
			}
			Intent intent = new Intent(experience.getId());
			intent.putExtra("experience", account.getGson().toJson(saved));
			LocalBroadcastManager.getInstance(account.getContext()).sendBroadcast(intent);
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
			success = false;
		}
		finished = true;
		if (this.saveCallback != null)
		{
			this.saveCallback.accountProcessCallback(success, saved);
		}
	}

	public boolean isFinished()
	{
		return finished;
	}

	Experience getExperience()
	{
		return experience;
	}

	private void saveTempExperience(final File file, final Experience experience)
	{
		try
		{
			if (file != null && file.canWrite())
			{
				FileWriter writer = new FileWriter(file);
				account.getGson().toJson(experience, writer);
				writer.flush();
				writer.close();
			}
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}
	}

	private File getDirectory()
	{
		return account.getContext().getDir("experiences", Context.MODE_PRIVATE);
	}

	private File createTempFile(final String uri)
	{
		if (uri != null)
		{
			if (uri.startsWith(rootHTTP) || uri.startsWith(rootHTTPS))
			{
				String id = Uri.parse(uri).getLastPathSegment();
				return new File(getDirectory(), id);
			}
			else if (uri.startsWith(AppEngineAccount.appSavePrefix))
			{
				String id = uri.substring(AppEngineAccount.appSavePrefix.length());
				return new File(getDirectory(), id);
			}
		}

		return new File(getDirectory(), UUID.randomUUID().toString());
	}

	private boolean exists(String url) throws Exception
	{
		final Request request = new Request.Builder()
				.url(url)
				.head()
				.build();
		final Response response = Artcodes.httpClient.newCall(request).execute();
		return response.code() == 200;
	}

	private void uploadImage(String imageURI)
	{
		if (imageURI != null && (imageURI.startsWith("file:") || imageURI.startsWith("content:")))
		{
			try
			{
				final RequestBodyUtil.ImageRequestBody imageBody = RequestBodyUtil.createImageBody(account.getContext(), imageURI);
				final String hash = imageBody.getHash();
				final String url = "https://aestheticodes.appspot.com/image/" + hash;

				if (!exists(url))
				{
					Request request = new Request.Builder()
							.url(url)
							.put(imageBody)
							.headers(account.getHeaders())
							.build();

					Response response = Artcodes.httpClient.newCall(request).execute();
					account.validateResponse(request, response);
				}

				if (imageURI.equals(experience.getImage()))
				{
					experience.setImage(url);
				}

				if (imageURI.equals(experience.getIcon()))
				{
					experience.setIcon(url);
				}
				Log.i("upload", imageURI + " is now " + url);
			}
			catch (Exception e)
			{
				Log.w("", e.getMessage(), e);
			}
		}
	}
}
