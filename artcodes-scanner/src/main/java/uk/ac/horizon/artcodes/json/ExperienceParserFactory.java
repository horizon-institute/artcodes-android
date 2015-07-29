package uk.ac.horizon.artcodes.json;


import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.process.HueShifter;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.Inverter;
import uk.ac.horizon.artcodes.scanner.process.ResizeThresholder;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

public class ExperienceParserFactory implements JsonParserFactory
{
	private static Gson gson;

	private static Gson createGson(Context context)
	{
		if (gson == null)
		{
			GsonBuilder builder = new GsonBuilder();
			if (context == null || Feature.get(context, R.bool.feature_load_old_experiences).isEnabled())
			{
				builder.registerTypeAdapterFactory(new ExperienceTypeAdapterFactor());
			}
			builder.registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(ImageProcessor.class)
					.registerSubtype(TileThresholder.class, "tile")
					.registerSubtype(ResizeThresholder.class, "resize")
					.registerSubtype(HueShifter.class, "hue")
					.registerSubtype(Inverter.class, "invert"));
			gson = builder.create();
		}
		return gson;
	}

	public static String toJson(Object object)
	{
		return createGson(null).toJson(object);
	}

	private final Context context;

	public ExperienceParserFactory(Context context)
	{
		this.context = context;
	}

	@Override
	public <T> JsonParser<T> parserFor(Class<T> type)
	{
		return new GsonParser<>(createGson(context), type);
	}

	@Override
	public <T> JsonParser<T> parserFor(TypeToken<T> type)
	{
		return new GsonParser<>(createGson(context), type.getType());
	}
}
