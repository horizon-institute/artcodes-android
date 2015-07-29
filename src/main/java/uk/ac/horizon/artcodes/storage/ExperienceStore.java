package uk.ac.horizon.artcodes.storage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ExperienceStore extends Store
{
	int getAccountType();

	int getIcon();

	String getId();

	String getName();

	int getSaveText();

	Loader<List<String>> library();

	Loader<Map<String, List<String>>> recommended();

	void save(Saver saver) throws IOException;
}
