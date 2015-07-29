package uk.ac.horizon.artcodes.storage;

import com.google.gson.reflect.TypeToken;
import uk.ac.horizon.artcodes.json.JsonParserFactory;

import java.util.ArrayList;
import java.util.List;

public class Storage
{
	private final List<Store> stores = new ArrayList<>();
	private final JsonParserFactory parserFactory;

	public Storage(JsonParserFactory parserFactory)
	{
		this.parserFactory = parserFactory;
	}

	public JsonParserFactory getParserFactory()
	{
		return parserFactory;
	}

	public List<Store> list()
	{
		return stores;
	}

	public <T> Loader<T> load(TypeToken<T> type)
	{
		return new Loader<>(this, parserFactory.parserFor(type));
	}

	public <T> Loader<T> load(Class<T> type)
	{
		return new Loader<>(this, parserFactory.parserFor(type));
	}

	public void register(Store store)
	{
		stores.add(store);
	}

	Store getStore(String uri)
	{
		for (Store store : stores)
		{
			if (store.canLoad(uri))
			{
				return store;
			}
		}
		return null;
	}
}
