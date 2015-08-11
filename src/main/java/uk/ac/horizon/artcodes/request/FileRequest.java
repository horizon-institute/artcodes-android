package uk.ac.horizon.artcodes.request;

import android.util.Log;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URI;

public class FileRequest<T> extends UriSource<T>
{
	public static final class Factory implements RequestFactory
	{
		@Override
		public String[] getPrefixes()
		{
			return new String[]{"file:"};
		}

		@Override
		public <T> Request<T> createRequest(ArtcodeServer server, String uri, Type type)
		{
			return new FileRequest<>(server, uri, type);
		}
	}

	public FileRequest(ArtcodeServer server, String uri, Type type)
	{
		super(server, uri, type);
	}

	@Override
	public void loadInto(RequestCallback<T> callback)
	{
		try
		{
			callback.onResponse(server.getGson().<T>fromJson(new FileReader(new File(URI.create(uri))), type));
		}
		catch (Exception e)
		{
			callback.onError(e);
		}
	}
}
