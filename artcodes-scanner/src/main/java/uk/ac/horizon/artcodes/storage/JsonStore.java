package uk.ac.horizon.artcodes.storage;

import android.net.Uri;

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
		Uri uri = Uri.parse(loader.getUri());
		loader.parse(uri.getSchemeSpecificPart());
	}
}
