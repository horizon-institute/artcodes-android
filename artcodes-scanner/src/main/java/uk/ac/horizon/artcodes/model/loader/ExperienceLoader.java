package uk.ac.horizon.artcodes.model.loader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.process.HueShifter;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.Inverter;
import uk.ac.horizon.artcodes.scanner.process.ResizeThresholder;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

import java.util.ArrayList;
import java.util.List;

public class ExperienceLoader
{
	private static class ExperienceRef extends Ref<Experience>
	{
		private ExperienceRef(String uri)
		{
			super(uri);
		}

		@Override
		public void load(final Context context, final LoadListener<Experience> listener)
		{
			if (isLoaded())
			{
				if (listener != null)
				{
					listener.onLoaded(get());
				}
				return;
			}

			if (getUri().startsWith(ARTCODE_SCAN_SCHEME + ":"))
			{
				// TODO parser.parse(data.getSchemeSpecificPart());
			}
			else
			{
				final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
				final String experienceJSON = preferences.getString(getUri(), null);
				if (experienceJSON != null)
				{
					parse(experienceJSON, listener);
				}

				Ion.with(context).load(getUri()).asString().setCallback(new FutureCallback<String>()
				{
					@Override
					public void onCompleted(Exception e, String result)
					{
						preferences.edit().putString(getUri(), result).apply();
						parse(result, listener);
					}
				});
			}
		}

		private void parse(String json, LoadListener<Experience> listener)
		{
			Experience item = ExperienceLoader.parse(json);
			set(item);
			if (listener != null)
			{
				listener.onLoaded(item);
			}
		}
	}

	private static final String EXPERIENCE_STORE = "uk.ac.horizon.artcodes.experiences";
	private static final String ARTCODE_SCAN_SCHEME = "x-artcode-scan";

	public static Ref<Experience> from(Bundle bundle, Intent intent)
	{
		if (bundle != null && bundle.containsKey("experience"))
		{
			Experience experience = parse(bundle.getString("experience"));
			ExperienceRef ref = new ExperienceRef(experience.getId());
			ref.set(experience);
			return ref;
		}
		return from(intent);
	}

	public static Ref<Experience> from(Intent intent)
	{
		Log.i("", "New Intent: " + intent);
		if (intent.getStringExtra("experience") != null)
		{
			Experience experience = parse(intent.getStringExtra("experience"));
			Ref<Experience> ref = new ExperienceRef(experience.getId());
			ref.set(experience);
			return ref;
		}
		else
		{
			final Uri data = intent.getData();
			if (data != null)
			{
				if (data.getScheme() == null)
				{
					return new ExperienceRef("http://aestheticodes.appspot.com/experience/" + data.toString());
				}

				return new ExperienceRef(data.toString());
			}
		}
		return null;
	}

	public static List<Ref<Experience>> from(List<String> uris, int max)
	{
		List<Ref<Experience>> list = new ArrayList<>();
		for (String uri : uris)
		{
			if (list.size() >= max)
			{
				break;
			}

			list.add(new ExperienceRef(uri));
		}
		return list;
	}

	public static Gson createParser()
	{
		GsonBuilder builder = new GsonBuilder();
		if (true)
		{
			builder.registerTypeAdapterFactory(new ExperienceTypeAdapterFactor());
		}
		builder.registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(ImageProcessor.class)
				.registerSubtype(TileThresholder.class, "tile")
				.registerSubtype(ResizeThresholder.class, "resize")
				.registerSubtype(HueShifter.class, "hue")
				.registerSubtype(Inverter.class, "invert"));
		return builder.create();
	}

	public static Experience parse(String json)
	{
		Gson gson = createParser();
		return gson.fromJson(json, Experience.class);
	}

	public static void startActivity(Class<?> activityClass, Ref<Experience> experienceRef, Context context)
	{
		Intent intent = new Intent(context, activityClass);
		if (experienceRef.isLoaded())
		{
			Gson gson = createParser();
			Log.i("", "Experience = " + gson.toJson(experienceRef.get()));
			intent.putExtra("experience", gson.toJson(experienceRef.get()));
		}
		else
		{
			intent.setData(Uri.parse(experienceRef.getUri()));
		}
		Log.i("", "Start activity " + intent);
		context.startActivity(intent);
	}
}
