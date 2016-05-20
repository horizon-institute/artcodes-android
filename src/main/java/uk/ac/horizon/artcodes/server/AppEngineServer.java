/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.ExperienceParser;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.account.AppEngineAccount;
import uk.ac.horizon.artcodes.account.LocalAccount;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.ScanEvent;

public class AppEngineServer implements ArtcodeServer
{
	private static final String starred_tag = "starred";
	private static final String recent_tag = "recent";
	private static final String accounts_tag = "accounts";
	private static final String prefix = "google:";
	private final Context context;
	private final Gson gson;
	private final List<Account> accounts = new ArrayList<>();

	public AppEngineServer(Context context)
	{
		this.context = context;
		this.gson = ExperienceParser.createGson(context);
		final List<String> accountIDs = loadIDs(Account.class, accounts_tag);
		if (!accountIDs.contains("local"))
		{
			accountIDs.add("local");
			saveIDs(Account.class, accounts_tag, accountIDs);
		}

		for (String accountID : accountIDs)
		{
			accounts.add(createAccount(accountID));
		}
	}

	public Account createAccount(String id)
	{
		if (id.startsWith(prefix))
		{
			String name = id.substring(prefix.length());
			return new AppEngineAccount(context, name, gson);
		}
		else if (id.equals("local"))
		{
			return new LocalAccount(context, gson);
		}
		return null;
	}

	@Override
	public void add(Account account)
	{
		if (getAccount(account.getId()) != null)
		{
			return;
		}

		accounts.add(accounts.size() - 1, account);
		List<String> accountIDs = new ArrayList<>();
		for (Account accountItem : accounts)
		{
			accountIDs.add(accountItem.getId());
		}
		saveIDs(Account.class, accounts_tag, accountIDs);
	}

	public void saveRecent(List<String> ids)
	{
		saveIDs(Experience.class, recent_tag, ids);
	}

	public void saveStarred(List<String> ids)
	{
		saveIDs(Experience.class, starred_tag, ids);
	}

	@Override
	public Account getAccount(String id)
	{
		for (Account account : accounts)
		{
			if (account.getId().equals(id))
			{
				return account;
			}
		}
		return null;
	}

	@Override
	public List<Account> getAccounts()
	{
		return accounts;
	}

	@Override
	public void loadExperience(final String id, final LoadCallback<Experience> callback)
	{
		load(id, new JsonCallback<>(Experience.class, gson, context, callback));
	}

	@Override
	public void loadRecent(LoadCallback<List<String>> callback)
	{
		callback.loaded(loadIDs(Experience.class, recent_tag));
	}

	@Override
	public void loadRecommended(final LoadCallback<Map<String, List<String>>> callback, Location location)
	{
		String url = "http://aestheticodes.appspot.com/recommended";
		if (location != null)
		{
			url = url + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		}

		load(url, new JsonCallback<>(new TypeToken<Map<String, List<String>>>()
		{
		}.getType(), gson, context, callback));
	}

	@Override
	public void loadStarred(LoadCallback<List<String>> callback)
	{
		callback.loaded(loadIDs(Experience.class, starred_tag));
	}

	@Override
	public void logScan(final String uri, final Action action)
	{
		final SharedPreferences preferences = context.getSharedPreferences("History", Context.MODE_PRIVATE);
		final String historyJSON = preferences.getString(uri, "[]");
		final List<ScanEvent> history = gson.fromJson(historyJSON, new TypeToken<List<ScanEvent>>()
		{
		}.getType());

		// Only log scan event if an identical scan hasn't happened recently
		if(!history.isEmpty())
		{
			final ScanEvent lastEvent = history.get(history.size() - 1);
			if(System.currentTimeMillis() - lastEvent.getTimestamp() < 1000)
			{
				String lastAction = gson.toJson(lastEvent.getAction());
				String currentAction = gson.toJson(action);
				if(currentAction.equals(lastAction))
				{
					return;
				}
			}
		}

		history.add(new ScanEvent(action));

		preferences.edit().putString(uri, gson.toJson(history)).apply();

		for (Account account : accounts)
		{
			if (account.logScan(uri))
			{
				break;
			}
		}
	}

	@Override
	public List<ScanEvent> getScanHistory(String id)
	{
		final SharedPreferences preferences = context.getSharedPreferences("History", Context.MODE_PRIVATE);
		final String historyJSON = preferences.getString(id, "[]");
		return gson.fromJson(historyJSON, new TypeToken<List<ScanEvent>>()
		{
		}.getType());
	}

	@Override
	public void search(String query, LoadCallback<List<String>> callback)
	{
		String url = Uri.parse("https://aestheticodes.appspot.com/search")
				.buildUpon()
				.appendQueryParameter("q", query)
				.build().toString();
		load(url, new JsonCallback<>(new TypeToken<List<String>>()
		{
		}.getType(), gson, context, callback));
	}

	private void load(String uri, URILoaderCallback callback)
	{
		SharedPreferences preferences = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE);
		String accountID = preferences.getString(uri, null);
		if (accountID != null)
		{
			Account account = getAccount(accountID);
			if (account != null)
			{
				if (account.load(uri, callback))
				{
					return;
				}
			}
		}

		for (Account account : getAccounts())
		{
			if (account.load(uri, callback))
			{
				return;
			}
		}
		Log.w("", "Failed to find loader for " + uri);
	}

	private List<String> loadIDs(Class<?> clazz, String name)
	{
		SharedPreferences preferences = context.getSharedPreferences(clazz.getName(), Context.MODE_PRIVATE);
		String jsonPreferences = preferences.getString(name, "[]");
		Log.i("ids", name + " = " + jsonPreferences);
		return gson.fromJson(jsonPreferences, new TypeToken<List<String>>()
		{
		}.getType());
	}


	private void saveIDs(Class<?> clazz, String name, List<String> ids)
	{
		SharedPreferences preferences = context.getSharedPreferences(clazz.getName(), Context.MODE_PRIVATE);
		String jsonPreferences = gson.toJson(ids);
		preferences.edit().putString(name, jsonPreferences).apply();
	}
}
