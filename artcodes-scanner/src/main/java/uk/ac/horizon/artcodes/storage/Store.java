package uk.ac.horizon.artcodes.storage;


import java.io.IOException;

public interface Store
{
	boolean canLoad(String uri);

	void load(Loader<?> loader) throws IOException;
}
