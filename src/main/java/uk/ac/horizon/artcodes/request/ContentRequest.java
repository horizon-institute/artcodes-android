package uk.ac.horizon.artcodes.request;

import android.net.Uri;
import android.util.Log;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class ContentRequest<T> extends UriSource<T>
{
	public static final class Factory implements RequestFactory
	{
		@Override
		public String[] getPrefixes()
		{
			return new String[] { "content:" };
		}

		@Override
		public <T> Request<T> createRequest(ArtcodeServer server, String uri, Type type)
		{
			return new ContentRequest<>(server,uri,type);
		}
	}

	public ContentRequest(ArtcodeServer server, String uri, Type type)
	{
		super(server, uri, type);
	}

	@Override
	public void loadInto(RequestCallback<T> callback)
	{
		try
		{
			callback.onResponse(server.getGson().<T>fromJson(new InputStreamReader(server.getContext().getContentResolver().openInputStream(Uri.parse(uri))), type));
		}
		catch (Exception e)
		{
			callback.onError(e);
		}
	}
}
