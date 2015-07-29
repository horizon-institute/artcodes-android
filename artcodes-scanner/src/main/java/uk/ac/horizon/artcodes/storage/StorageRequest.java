package uk.ac.horizon.artcodes.storage;

import android.util.Log;
import uk.ac.horizon.artcodes.json.JsonParser;

import java.io.Reader;

public abstract class StorageRequest<T>
{
	protected final JsonParser<T> parser;
	private final Storage storage;
	protected StorageListener progressListener = new StorageListener()
	{
		@Override
		public void onError(Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
	};
	private Store store;
	private String uri;
	private StoreListener<T> listener;

	public StorageRequest(Storage storage, JsonParser<T> parser)
	{
		this.parser = parser;
		this.storage = storage;
	}

	public void async(StoreListener<T> listener)
	{
		this.listener = listener;
		async();
	}

	public abstract void async();

	protected Store getStore()
	{
		if (store == null)
		{
			return storage.getStore(uri);
		}

		return store;
	}

	protected void setStore(Store store)
	{
		this.store = store;
	}

	String getUri()
	{
		return uri;
	}

	protected void setUri(String uri)
	{
		this.uri = uri;
	}

	void parse(String json)
	{
		T item = parser.parse(json);
		if (listener != null)
		{
			listener.onItemChanged(item);
		}
	}

	void parse(Reader reader)
	{
		T item = parser.parse(reader);
		if (listener != null)
		{
			listener.onItemChanged(item);
		}
	}
}
