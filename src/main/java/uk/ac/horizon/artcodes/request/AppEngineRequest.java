/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.request;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.account.AppEngineAccount;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

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
		if (networkResponse != null && networkResponse.statusCode == 403)// TODO HttpStatus.SC_UNAUTHORIZED)
		{
			String token = getToken();
			if (token != null)
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
		} catch (Exception e)
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
