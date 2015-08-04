package uk.ac.horizon.artcodes.account;

import android.content.Context;
import com.google.gson.Gson;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;
import uk.ac.horizon.artcodes.source.Source;
import uk.ac.horizon.artcodes.source.SourceFactory;
import uk.ac.horizon.artcodes.source.UriList;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface Account
{
	void add(SourceFactory factory);

	AccountInfo getInfo();

	Source<Experience> getLibrary();

	UriList<Experience> getStarred();

	Source<Map<String, List<String>>> getRecommended();

	UriList<List<String>> getRecent();

	Context getContext();

	Gson getGson();

	void scanned(String uri, String marker, CameraAdapter adapter);

	Source<Experience> getExperience(String id);

	void saveExperience(Experience experience);

	<T> Source<T> getSource(String uri, Type type);
}
