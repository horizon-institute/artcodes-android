package uk.ac.horizon.artcodes.storage;

import android.util.Log;
import uk.ac.horizon.artcodes.json.JsonParser;

import java.io.IOException;

public class Loader<T> extends StorageRequest<T>
{
	Loader(Storage storage, JsonParser<T> parser)
	{
		super(storage, parser);
	}

	public void async()
	{
		try
		{
			Store store = getStore();
			Log.i("", "Loading " + getUri() + " with " + store.getClass().getName());
			store.load(this);
		}
		catch (IOException e)
		{
			progressListener.onError(e);
		}
	}

	public Loader<T> from(Store store)
	{
		setStore(store);
		return this;
	}

	public Loader<T> fromJson(String json)
	{
		setUri("json:" + json);
		return this;
	}

	public Loader<T> fromUri(String uri)
	{
		setUri(uri);
		return this;
	}
}
