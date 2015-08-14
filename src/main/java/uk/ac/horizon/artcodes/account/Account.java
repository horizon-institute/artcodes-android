package uk.ac.horizon.artcodes.account;

import java.util.List;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallback;

public interface Account
{
	void loadLibrary(RequestCallback<List<String>> callback);

	void saveExperience(Experience experience);

	String getId();
	String getName();

	boolean willCreateCopy(String uri);
}
