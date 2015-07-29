package uk.ac.horizon.artcodes.storage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

public class FileStore implements Store
{
	@Override
	public boolean canLoad(String uri)
	{
		return uri.startsWith("file:");
	}

	@Override
	public void load(Loader<?> loader) throws IOException
	{
		loader.parse(new FileReader(new File(URI.create(loader.getUri()))));
	}
}
