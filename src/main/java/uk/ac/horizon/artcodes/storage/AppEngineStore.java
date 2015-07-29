package uk.ac.horizon.artcodes.storage;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.reflect.TypeToken;
import uk.ac.horizon.artcodes.ArtcodeStorage;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppEngineStore extends HTTPStore implements ExperienceStore
{
	private final String name;
	private static final Map<String, Saver> requests = new HashMap<>();

	public static Saver getRequest(String uri)
	{
		return requests.get(uri);
	}

	static void setRequest(String uri, Saver saver)
	{
		requests.put(uri, saver);
	}

	public AppEngineStore(Context context, String name)
	{
		super(context);
		this.name = name;
	}

	@Override
	public String getId()
	{
		return "google:" + name;
	}

	@Override
	public int getAccountType()
	{
		return R.string.artcodeServer;
	}

	@Override
	public int getIcon()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getSaveText()
	{
		return R.string.save_google;
	}

	private Location getLocation()
	{
		final LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		float accuracy = Float.MAX_VALUE;
		Location location = null;
		for(String provider: locationManager.getProviders(new Criteria(), true))
		{
			Location newLocation = locationManager.getLastKnownLocation(provider);
			if(newLocation != null)
			{
				if(newLocation.getAccuracy() < accuracy)
				{
					accuracy = newLocation.getAccuracy();
					location = newLocation;
				}
			}
		}

		return location;
	}

	@Override
	public Loader<Map<String, List<String>>> recommended()
	{
		String url = "http://aestheticodes.appspot.com/recommended";
		final Location location = getLocation();
		if (location != null)
		{
			url = url + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		}

		return ArtcodeStorage.load(new TypeToken<Map<String, List<String>>>() {}).fromUri(url);
	}

	@Override
	public boolean canLoad(String uri)
	{
		return uri.startsWith("http://aestheticodes.appspot.com/") || uri.startsWith("https://aestheticodes.appspot.com/");
	}

	@Override
	public void save(Saver saver) throws IOException
	{
		new Thread(new AppEngineUpload(context, this, saver)).start();
	}

	String getToken()
	{
		try
		{
			return GoogleAuthUtil.getToken(context, name, "oauth2:email");
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
		return null;
	}

	@Override
	protected Map<String, String> getRequestHeaders()
	{
		try
		{
			String token = getToken();
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer " + token);
			return headers;
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
		return Collections.emptyMap();
	}

	@Override
	public Loader<List<String>> library()
	{
		return ArtcodeStorage.load(new TypeToken<List<String>>() {}).fromUri("http://aestheticodes.appspot.com/experiences");
	}
}
