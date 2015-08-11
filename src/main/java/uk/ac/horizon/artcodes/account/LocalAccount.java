package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
		}
		catch (Exception e)
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
					final File directory = getDirectory();
					final String directoryURI = directory.toURI().toString();
					File file;
					if (experience.getId() == null || !experience.getId().startsWith(directoryURI))
					{
						String id = UUID.randomUUID().toString();
						file = new File(directory, id);
						experience.setId(file.toURI().toString());
					}
					else
					{
						file = new File(URI.create(experience.getId()));
					}
					experience.setEditable(true);

					FileWriter writer = new FileWriter(file);
					server.getGson().toJson(experience, writer);
					writer.flush();
					writer.close();

					SharedPreferences.Editor editor = server.getContext().getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE).edit();
					editor.putString(experience.getId(), getId()).apply();
				}
				catch (Exception e)
				{
					GoogleAnalytics.trackException(e);
				}
			}
		}).start();
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
}
