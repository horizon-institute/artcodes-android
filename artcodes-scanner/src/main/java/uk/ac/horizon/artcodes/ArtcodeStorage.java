package uk.ac.horizon.artcodes;

import android.content.Context;
import com.google.gson.reflect.TypeToken;
import uk.ac.horizon.artcodes.json.ExperienceParserFactory;
import uk.ac.horizon.artcodes.storage.ContentStore;
import uk.ac.horizon.artcodes.storage.FileStore;
import uk.ac.horizon.artcodes.storage.HTTPStore;
import uk.ac.horizon.artcodes.storage.JsonStore;
import uk.ac.horizon.artcodes.storage.Loader;
import uk.ac.horizon.artcodes.storage.Storage;

public class ArtcodeStorage
{
	protected static Storage storage;

	public static void initialize(Context context)
	{
		if (storage == null)
		{
			storage = new Storage(new ExperienceParserFactory(context));
			storage.register(new ContentStore(context));
			storage.register(new JsonStore());
			storage.register(new JsonStore("x-artcode-scan"));
			storage.register(new FileStore());
			storage.register(new HTTPStore(context));
		}
	}

	public static void setStorage(Storage newStorage)
	{
		storage = newStorage;
	}

	public static <T> Loader<T> load(TypeToken<T> type)
	{
		return storage.load(type);
	}

	public static <T> Loader<T> load(Class<T> type)
	{
		return storage.load(type);
	}
}
