package uk.ac.horizon.artcodes.request;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.GoogleAuthUtil;
import org.apache.http.HttpStatus;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.account.AppEngineAccount;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppEngineRequest<T> extends HTTPRequest<T>
{
	public AppEngineRequest(ArtcodeServer server, String uri, Type type)
	{
		super(server, uri, type);
	}

	private String getToken()
	{
		// TODO Load account from prefs for uri
		SharedPreferences preferences = server.getContext().getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE);
		String accountID = preferences.getString(uri, null);
		if (accountID != null)
		{
			Account account = server.getAccount(accountID);
			if (account instanceof AppEngineAccount)
			{
				AppEngineAccount appEngineAccount = (AppEngineAccount) account;
				String token = appEngineAccount.getToken();
				if (token != null)
				{
					return token;
				}
			}
		}

		for (Account account : server.getAccounts())
		{
			if (account instanceof AppEngineAccount)
			{
				AppEngineAccount appEngineAccount = (AppEngineAccount) account;
				String token = appEngineAccount.getToken();
				if (token != null)
				{
					return token;
				}
			}
		}

		AccountManager accountManager = AccountManager.get(server.getContext());
		for (android.accounts.Account account : accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE))
		{
			AppEngineAccount appEngineAccount = new AppEngineAccount(server, account.name);
			String token = appEngineAccount.getToken();
			if (token != null)
			{
				server.add(appEngineAccount);
				return token;
			}
		}

		return null;
	}

	@Override
	protected void onError(RequestCallback<T> target, VolleyError error)
	{
		NetworkResponse networkResponse = error.networkResponse;
		if (networkResponse != null && networkResponse.statusCode == HttpStatus.SC_UNAUTHORIZED)
		{
			String token = getToken();
			if(token != null)
			{
				GoogleAuthUtil.invalidateToken(server.getContext(), token);
			}
		}
		super.onError(target, error);
	}

	@Override
	protected Map<String, String> getRequestHeaders()
	{
		try
		{
			String token = getToken();
			if (token != null)
			{
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "Bearer " + token);
				return headers;
			}
		}
		catch (Exception e)
		{
			GoogleAnalytics.trackException(e);
		}
		return Collections.emptyMap();
	}

	public static final class Factory implements RequestFactory
	{

		@Override
		public String[] getPrefixes()
		{
			return new String[]{"http://aestheticodes.appspot.com/",
			                    "https://aestheticodes.appspot.com/",
			                    "https://www.googleapis.com/plus/v1/people/me"};
		}

		@Override
		public <T> Request<T> createRequest(ArtcodeServer server, String uri, Type type)
		{
			if (uri.startsWith("http:"))
			{
				// Always use https
				uri = uri.replace("http:", "https:");
			}
			return new AppEngineRequest<>(server, uri, type);
		}
	}
}
