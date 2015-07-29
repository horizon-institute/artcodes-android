package uk.ac.horizon.artcodes.storage;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStreamReader;

public class ContentStore implements Store
{
	private static final String prefix = "content:";
	private final Context context;

	public ContentStore(Context context)
	{
		this.context = context;
	}

	@Override
	public boolean canLoad(String uri)
	{
		return uri.startsWith(prefix);
	}

	@Override
	public void load(Loader<?> loader) throws IOException
	{
		loader.parse(new InputStreamReader(context.getContentResolver().openInputStream(Uri.parse(loader.getUri()))));
	}
}
