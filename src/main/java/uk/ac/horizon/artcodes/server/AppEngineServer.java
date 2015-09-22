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

package uk.ac.horizon.artcodes.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import uk.ac.horizon.artcodes.ExperienceParser;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.account.AppEngineAccount;
import uk.ac.horizon.artcodes.account.LocalAccount;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.AppEngineRequest;
import uk.ac.horizon.artcodes.request.ContentRequest;
import uk.ac.horizon.artcodes.request.FileRequest;
import uk.ac.horizon.artcodes.request.HTTPRequest;
import uk.ac.horizon.artcodes.request.IDList;
import uk.ac.horizon.artcodes.request.Request;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.request.RequestCallbackBase;
import uk.ac.horizon.artcodes.request.RequestFactory;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;

public class AppEngineServer implements ArtcodeServer
{
	private static final String prefix = "google:";
	private final Context context;
	private final Gson gson;
	private final SortedMap<String, RequestFactory> factories = new TreeMap<>(new Comparator<String>()
	{
		@Override
		public int compare(String lhs, String rhs)
		{
			int diff = rhs.length() - lhs.length();
			if (diff == 0)
			{
				return lhs.compareTo(rhs);
			}
			return diff;
		}
	});

	public AppEngineServer(Context context)
	{
		this.context = context;
		this.gson = ExperienceParser.createGson(context);

		add(new AppEngineRequest.Factory());
		add(new ContentRequest.Factory());
		add(new FileRequest.Factory());
		add(new HTTPRequest.Factory());
	}

	public void add(RequestFactory factory)
	{
		for (String prefix : factory.getPrefixes())
		{
			factories.put(prefix, factory);
		}
	}

	@Override
	public void add(Account account)
	{
		IDList list = new IDList(context, Account.class.getName(), "accounts");
		if (!list.contains(account.getId()))
		{
			int localIndex = list.indexOf("local");
			list.beginEdit();
			if (localIndex == -1)
			{
				list.add(account.getId());
				list.add("local");
			} else
			{
				list.add(localIndex, account.getId());
			}
			list.commit();
		}
	}

	@Override
	public Account getAccount(String id)
	{
		if (id.startsWith(prefix))
		{
			return new AppEngineAccount(this, id.substring(prefix.length()));
		} else if (id.equals("local"))
		{
			return new LocalAccount(this);
		}
		return null;
	}

	@Override
	public List<Account> getAccounts()
	{
		final IDList ids = new IDList(context, Account.class.getName(), "accounts");
		final List<Account> accounts = new ArrayList<>();
		if (ids.isEmpty())
		{
			accounts.add(new LocalAccount(this));
		} else
		{
			for (String id : ids)
			{
				Account account = getAccount(id);
				if (account != null)
				{
					accounts.add(account);
				}
			}
		}
		return accounts;
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	public Gson getGson()
	{
		return gson;
	}

	public <T> void load(String uri, Type type, RequestCallback<T> callback)
	{
		for (String key : factories.keySet())
		{
			if (uri.startsWith(key))
			{
				Request<T> source = factories.get(key).createRequest(this, uri, type);
				if (source != null)
				{
					source.loadInto(callback);
					return;
				}
			}
		}
	}

	@Override
	public void loadExperience(final String id, final RequestCallback<Experience> callback)
	{
		load(id, Experience.class, new RequestCallbackBase<Experience>()
		{
			@Override
			public void onResponse(Experience item)
			{
				if (!item.isEditable())
				{
					SharedPreferences preferences = context.getSharedPreferences(Account.class.getName(), Context.MODE_PRIVATE);
					String account = preferences.getString(id, null);
					if (account != null)
					{
						String accounts = preferences.getString("accounts", "[]");
						if (accounts.contains(account))
						{
							item.setEditable(true);
						}
					}
				}

				callback.onResponse(item);
			}
		});
	}

	@Override
	public void loadRecent(RequestCallback<List<String>> callback)
	{
		callback.onResponse(new IDList(context, Experience.class.getName(), "recent"));
	}

	@Override
	public void loadRecommended(RequestCallback<Map<String, List<String>>> callback)
	{
		String url = "http://aestheticodes.appspot.com/recommended";
		final Location location = getLocation();
		if (location != null)
		{
			url = url + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		}

		load(url, new TypeToken<Map<String, List<String>>>()
		{
		}.getType(), callback);
	}

	@Override
	public void loadStarred(RequestCallback<List<String>> callback)
	{
		callback.onResponse(new IDList(context, Experience.class.getName(), "starred"));
	}

	@Override
	public void logScan(String uri, Action action, CameraAdapter adapter)
	{

	}

	private Location getLocation()
	{
		final LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		float accuracy = Float.MAX_VALUE;
		Location location = null;
		for (String provider : locationManager.getProviders(new Criteria(), true))
		{
			Location newLocation = locationManager.getLastKnownLocation(provider);
			if (newLocation != null)
			{
				if (newLocation.getAccuracy() < accuracy)
				{
					accuracy = newLocation.getAccuracy();
					location = newLocation;
				}
			}
		}

		return location;
	}
}
