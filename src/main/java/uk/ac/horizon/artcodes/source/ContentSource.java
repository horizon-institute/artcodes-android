package uk.ac.horizon.artcodes.source;

import android.net.Uri;
import android.util.Log;
import uk.ac.horizon.artcodes.account.Account;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class ContentSource<T> extends UriSource<T>
{
	public static class Factory implements SourceFactory
	{

		@Override
		public String[] getPrefixes()
		{
			return new String[] { "content:" };
		}

		@Override
		public <T> Source<T> createSource(Account account, String uri, Type type)
		{
			return new ContentSource<>(account,uri,type);
		}
	}

	public ContentSource(Account account, String uri, Type type)
	{
		super(account, uri, type);
	}

	@Override
	public void loadInto(Target<T> target)
	{
		try
		{
			target.onLoaded(account.getGson().<T>fromJson(new InputStreamReader(account.getContext().getContentResolver().openInputStream(Uri.parse(uri))), type));
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
	}
}
