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
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Okio;
import uk.ac.horizon.artcodes.Analytics;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.model.Experience;

class CardographerUploadThread extends Thread {
	private final Experience experience;
	private final Account.AccountProcessCallback saveCallback;
	private final CardographerAccount account;
	private boolean finished = false;
	private final Context context;
	private final Gson gson;
	private final String rootURL;

	CardographerUploadThread(CardographerAccount account, Context context, Gson gson, String rootURL, Experience experience, Account.AccountProcessCallback saveCallback) {
		this.account = account;
		this.experience = experience;
		this.gson = gson;
		this.rootURL = rootURL;
		this.context = context;
		this.saveCallback = saveCallback;
	}

	@Override
	public void run() {
		boolean success = true;
		Experience saved = null;
		try {
			final File tempFile = createTempFile(experience.getId());
			saveTempExperience(tempFile, experience);

			final Request.Builder builder = new Request.Builder();
			MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
			bodyBuilder.setType(MultipartBody.FORM);
			if (experience.getId() != null && experience.getId().startsWith(AppEngineAccount.appSavePrefix)) {
				experience.setId(null);
			}
			bodyBuilder.addFormDataPart("experience", gson.toJson(experience));

			RequestBody body = uploadImage(experience.getImage());
			if (body != null) {
				if (experience.getImage() != null && experience.getImage().equals(experience.getIcon())) {
					bodyBuilder.addFormDataPart("image+icon", "image", body);
					experience.setImage(null);
					experience.setIcon(null);
				} else {
					bodyBuilder.addFormDataPart("image", "image", body);
					experience.setImage(null);
				}
			}
			RequestBody body2 = uploadImage(experience.getIcon());
			if (body2 != null) {
				bodyBuilder.addFormDataPart("icon", "icon", body2);
				experience.setIcon(null);
			}

			if (experience.getId() != null) {
				builder.url(rootURL + experience.getId());
				builder.put(bodyBuilder.build());
			} else {
				builder.url(rootURL + "experiences");
				builder.post(bodyBuilder.build());
			}
			builder.headers(account.getHeaders());

			final Request request = builder.build();
			final Response response = Artcodes.httpClient.newCall(request).execute();

			account.validateResponse(request, response);
			saved = gson.fromJson(response.body().charStream(), Experience.class);
			response.body().close();

			context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE)
					.edit()
					.putString(saved.getId(), account.getId())
					.apply();

			if (!tempFile.delete()) {
				Log.w("upload", "Temp file not deleted");
			}
			Intent intent = new Intent(experience.getId());
			intent.putExtra("experience", gson.toJson(saved));
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} catch (Exception e) {
			Analytics.trackException(e);
			success = false;
		}
		finished = true;
		if (this.saveCallback != null) {
			this.saveCallback.accountProcessCallback(success, saved);
		}
	}

	public boolean isFinished() {
		return finished;
	}

	Experience getExperience() {
		return experience;
	}

	private void saveTempExperience(final File file, final Experience experience) {
		try {
			if (file != null && file.canWrite()) {
				FileWriter writer = new FileWriter(file);
				gson.toJson(experience, writer);
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			Analytics.trackException(e);
		}
	}

	private File getDirectory() {
		return context.getDir("experiences", Context.MODE_PRIVATE);
	}

	private File createTempFile(final String uri) {
		if (uri != null) {
			if (uri.startsWith(rootURL)) {
				String id = Uri.parse(uri).getLastPathSegment();
				return new File(getDirectory(), id);
			} else if (uri.startsWith(AppEngineAccount.appSavePrefix)) {
				String id = uri.substring(AppEngineAccount.appSavePrefix.length());
				return new File(getDirectory(), id);
			}
		}

		return new File(getDirectory(), UUID.randomUUID().toString());
	}

	private RequestBody uploadImage(String imageURI) {
		if (imageURI != null && (imageURI.startsWith("file:") || imageURI.startsWith("content:"))) {
			try {
				Uri uri = Uri.parse(imageURI);
				String typeName = context.getContentResolver().getType(uri);
				MediaType type = MediaType.parse(typeName);
				byte[] data = Okio.buffer(Okio.source(context.getContentResolver().openInputStream(uri))).readByteArray();

				return RequestBody.create(data, type);
			} catch (Exception e) {
				Log.w("", e.getMessage(), e);
			}
		}
		return null;
	}
}
