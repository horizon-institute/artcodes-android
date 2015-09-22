/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
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

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

public class LocalAccount implements Account
{
	private final ArtcodeServer server;

	public LocalAccount(ArtcodeServer server)
	{
		this.server = server;
	}

	private File getDirectory()
	{
		return server.getContext().getDir("experiences", Context.MODE_PRIVATE);
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public void loadLibrary(RequestCallback<List<String>> callback)
	{
		try
		{
			File directory = getDirectory();
			Log.i("", "Listing " + directory.getAbsolutePath());
			List<String> result = new ArrayList<>();
			SharedPreferences.Editor editor = server.getContext().getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
			for (final File file : directory.listFiles())
			{
				String uri = file.toURI().toString();
				result.add(uri);
				editor.putString(uri, getId());
			}
			editor.apply();
			callback.onResponse(result);
		} catch (Exception e)
		{
			callback.onError(e);
		}
	}

	@Override
	public void saveExperience(final Experience experience)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					File file;
					if (experience.getId() == null || willCreateCopy(experience.getId()))
					{
						if (!willCreateCopy(experience.getOriginalID()))
						{
							file = new File(URI.create(experience.getOriginalID()));
							experience.setId(file.toURI().toString());
							experience.setOriginalID(null);
						} else
						{
							file = new File(getDirectory(), UUID.randomUUID().toString());
							experience.setId(file.toURI().toString());
						}
					} else
					{
						file = new File(URI.create(experience.getId()));
					}
					experience.setEditable(true);

					FileWriter writer = new FileWriter(file);
					server.getGson().toJson(experience, writer);
					writer.flush();
					writer.close();

					SharedPreferences.Editor editor = server.getContext().getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
					Log.i("", experience.getId() + " = " + getId());
					editor.putString(experience.getId(), getId()).apply();
				} catch (Exception e)
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
		return "local";
	}

	@Override
	public String getName()
	{
		return server.getContext().getString(R.string.device);
	}

	@Override
	public boolean willCreateCopy(String uri)
	{
		final File directory = getDirectory();
		final String directoryURI = directory.toURI().toString();
		return uri != null && !uri.startsWith(directoryURI);
	}
}
