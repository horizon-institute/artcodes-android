package uk.ac.horizon.artcodes.source;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import uk.ac.horizon.artcodes.account.Account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URI;

public class FileSource<T> extends UriSource<T>
{
	public static class Factory implements SourceFactory
	{
		@Override
		public String[] getPrefixes()
		{
			return new String[]{"file:"};
		}

		@Override
		public <T> Source<T> createSource(Account account, String uri, Type type)
		{
			return new FileSource<>(account, uri, type);
		}
	}

	public FileSource(Account account, String uri, Type type)
	{
		super(account, uri, type);
	}

	@Override
	public void loadInto(Target<T> target)
	{
		try
		{
			target.onLoaded(account.getGson().<T>fromJson(new FileReader(new File(URI.create(uri))), type));
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
	}
}
