package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.source.PrefList;
import uk.ac.horizon.artcodes.source.Source;
import uk.ac.horizon.artcodes.source.SourceFactory;
import uk.ac.horizon.artcodes.source.Target;
import uk.ac.horizon.artcodes.source.UriList;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AccountBase implements Account
{
	protected final Context context;
	protected final Gson gson;
	private final SortedMap<String, SourceFactory> factories = new TreeMap<>(new Comparator<String>()
	{
		@Override
		public int compare(String lhs, String rhs)
		{
			int diff = rhs.length() - lhs.length();
			if(diff == 0)
			{
				return lhs.compareTo(rhs);
			}
			return diff;
		}
	});

	@Override
	public void add(SourceFactory factory)
	{
		for (String prefix : factory.getPrefixes())
		{
			factories.put(prefix, factory);
		}
	}

	protected AccountBase(Context context, Gson gson)
	{
		this.context = context;
		this.gson = gson;
	}

	@Override
	public UriList<Experience> getStarred()
	{
		return new PrefList<Experience>(context, Experience.class.getName().toLowerCase(), "starred")
		{
			@Override
			public void loadInto(Target<Experience> target)
			{
				for (String uri : getList())
				{
					getSource(uri, Experience.class).loadInto(target);
				}
			}
		};
	}

	@Override
	public UriList<List<String>> getRecent()
	{
		return new PrefList<List<String>>(context, Experience.class.getName().toLowerCase(), "recent")
		{
			@Override
			public void loadInto(Target<List<String>> target)
			{
				target.onLoaded(getList());
			}
		};
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

	@Override
	public Source<Map<String, List<String>>> getRecommended()
	{
		String url = "http://aestheticodes.appspot.com/recommended";
		final Location location = getLocation();
		if (location != null)
		{
			url = url + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		}

		return getSource(url, new TypeToken<Map<String, List<String>>>(){});
	}

	public Context getContext()
	{
		return context;
	}

	public Gson getGson()
	{
		return gson;
	}

	@Override
	public <T> Source<T> getSource(String uri, Type type)
	{
		for (String key : factories.keySet())
		{
			if (uri.startsWith(key))
			{
				Source<T> source = factories.get(key).createSource(this, uri, type);
				if (source != null)
				{
					return source;
				}
			}
		}
		return null;
	}

	public <T> Source<T> getSource(String uri, Class<T> type)
	{
		for (String key : factories.keySet())
		{
			if (uri.startsWith(key))
			{
				Source<T> source = factories.get(key).createSource(this, uri, type);
				if (source != null)
				{
					return source;
				}
			}
		}
		return null;
	}

	public <T> Source<T> getSource(String uri, TypeToken<T> type)
	{
		return getSource(uri, type.getType());
	}

	@Override
	public Source<Experience> getExperience(String id)
	{
		return getSource(id, Experience.class);
	}
}
