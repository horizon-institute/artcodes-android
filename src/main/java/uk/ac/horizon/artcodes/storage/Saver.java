package uk.ac.horizon.artcodes.storage;

import uk.ac.horizon.artcodes.json.JsonParser;
import uk.ac.horizon.artcodes.model.Experience;

import java.io.IOException;

public class Saver extends StorageRequest<Experience>
{
	private final Experience item;

	Saver(Storage storage, JsonParser<Experience> parser, Experience item)
	{
		super(storage, parser);
		this.item = item;
	}

	public void async()
	{
		try
		{
			Store store = getStore();
			if (store instanceof ExperienceStore)
			{
				((ExperienceStore) store).save(this);
			}
		}
		catch (IOException e)
		{
			progressListener.onError(e);
		}
	}

	public Experience getItem()
	{
		return item;
	}

	public Saver to(Store store)
	{
		setStore(store);
		return this;
	}

	public String toJson()
	{
		return parser.toJson(item);
	}

	public Saver toUri(String uri)
	{
		setUri(uri);
		return this;
	}
}
