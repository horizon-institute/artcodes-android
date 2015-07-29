package uk.ac.horizon.artcodes.storage;

import uk.ac.horizon.artcodes.ArtcodeStorage;
import uk.ac.horizon.artcodes.model.Experience;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExperienceStorage extends ArtcodeStorage
{
	private static ExperienceStore defaultStore;

	public static List<ExperienceStore> list()
	{
		List<ExperienceStore> stores = new ArrayList<>();
		for (Store store : storage.list())
		{
			if (store instanceof ExperienceStore)
			{
				stores.add((ExperienceStore) store);
			}
		}
		return stores;
	}

	public static ExperienceStore getDefaultStore()
	{
		if (defaultStore == null)
		{
			for (Store store : storage.list())
			{
				if (store instanceof ExperienceStore)
				{
					defaultStore = (ExperienceStore) store;
					return defaultStore;
				}
			}
		}
		return defaultStore;
	}

	public static Loader<List<String>> loadLibrary()
	{
		return getDefaultStore().library();
	}

	public static Loader<Map<String, List<String>>> loadRecommended()
	{
		return getDefaultStore().recommended();
	}

	public static Saver save(Experience experience)
	{
		return new Saver(storage, storage.getParserFactory().parserFor(Experience.class), experience);
	}
}
