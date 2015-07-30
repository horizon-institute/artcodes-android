package uk.ac.horizon.artcodes.storage;

public class JsonStore implements Store
{
	private final String prefix;

	public JsonStore()
	{
		this.prefix = "json:";
	}

	public JsonStore(String prefix)
	{
		this.prefix = prefix + ":";
	}

	@Override
	public boolean canLoad(String uri)
	{
		return uri.startsWith("json:");
	}

	@Override
	public void load(Loader<?> loader)
	{
		loader.parse(loader.getUri().substring(prefix.length()));
	}
}
