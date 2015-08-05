package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.common.reflect.TypeToken;
import uk.ac.horizon.artcodes.ExperienceParser;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;
import uk.ac.horizon.artcodes.source.HTTPSource;
import uk.ac.horizon.artcodes.source.Source;
import uk.ac.horizon.artcodes.source.SourceFactory;
import uk.ac.horizon.artcodes.source.Target;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppEngineAccount extends AccountBase
{
	public static class Info implements AccountInfo
	{
		private final Context context;
		private final String name;

		public Info(Context context, String name)
		{
			this.context = context;
			this.name = name;
		}

		@Override
		public String getId()
		{
			return "google:" + name;
		}

		@Override
		public String getName()
		{
			return context.getString(R.string.artcodeServer);
		}

		@Override
		public String getUsername()
		{
			return name;
		}

		@Override
		public Drawable getIcon()
		{
			return context.getResources().getDrawable(R.drawable.ic_smartphone_black_36dp);
		}

		@Override
		public Account create()
		{
			return new AppEngineAccount(context, name);
		}
	}

	private final class Factory implements SourceFactory
	{
		@Override
		public String[] getPrefixes()
		{
			return new String[]{"http://aestheticodes.appspot.com/", "https://aestheticodes.appspot.com/", "https://www.googleapis.com/plus/v1/people/me"};
		}

		@Override
		public <T> Source<T> createSource(Account account, String uri, Type type)
		{
			return new HTTPSource<T>(account, uri, type)
			{
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
			};
		}
	}

	private final String name;
	//private static final Map<String, AppEngineUpload> requests = new HashMap<>();

	private AppEngineAccount(Context context, String name)
	{
		super(context, ExperienceParser.createGson(context));
		this.name = name;

		add(new Factory());
	}

	public AccountInfo getInfo()
	{
		return new Info(context, name);
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
	public Source<Experience> getLibrary()
	{
		return new Source<Experience>()
		{
			@Override
			public void loadInto(final Target<Experience> target)
			{
				getSource("http://aestheticodes.appspot.com/experiences", new TypeToken<List<String>>() {}).loadInto(new Target<List<String>>()
				{
					@Override
					public void onLoaded(List<String> item)
					{
						for (String uri : item)
						{
							getSource(uri, Experience.class).loadInto(target);
						}
					}
				});
			}
		};
	}

	@Override
	public void scanned(String uri, String marker, CameraAdapter adapter)
	{

	}

	@Override
	public void saveExperience(Experience experience)
	{
		new Thread(new AppEngineUpload(context, this, experience)).start();
	}
}
