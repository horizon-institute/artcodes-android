package uk.ac.horizon.artcodes.server;

import android.content.Context;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;

public interface ArtcodeServer
{
	void add(Account account);

	Account getAccount(String id);

	List<Account> getAccounts();

	void loadStarred(RequestCallback<List<String>> callback);

	void loadRecommended(RequestCallback<Map<String, List<String>>> callback);

	void loadRecent(RequestCallback<List<String>> callback);

	void logScan(String uri, Action action, CameraAdapter adapter);

	void loadExperience(String id, RequestCallback<Experience> callback);

	<T> void load(String uri, Type type, RequestCallback<T> callback);

	Context getContext();

	Gson getGson();
}
