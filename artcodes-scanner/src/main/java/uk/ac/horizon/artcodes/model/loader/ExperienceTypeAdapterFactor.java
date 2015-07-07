package uk.ac.horizon.artcodes.model.loader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import uk.ac.horizon.artcodes.model.Experience;

import java.io.IOException;

public class ExperienceTypeAdapterFactor implements TypeAdapterFactory
{
	@SuppressWarnings("unchecked") // we use a runtime check to guarantee that 'C' and 'T' are equal
	public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
	{
		return type.getRawType() == Experience.class ? (TypeAdapter<T>) customizeMyClassAdapter(gson, (TypeToken<Experience>) type) : null;
	}

	private TypeAdapter<Experience> customizeMyClassAdapter(Gson gson, TypeToken<Experience> type)
	{
		final TypeAdapter<Experience> delegate = gson.getDelegateAdapter(this, type);
		final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

		return new TypeAdapter<Experience>()
		{
			@Override
			public Experience read(JsonReader in) throws IOException
			{
				JsonElement tree = elementAdapter.read(in);
				if (tree.isJsonObject())
				{
					JsonObject jsonObject = tree.getAsJsonObject();
					if (jsonObject.has("threshold") && jsonObject.get("threshold").isJsonPrimitive())
					{
						jsonObject.remove("threshold");
					}
					if (jsonObject.has("id"))
					{
						String id = jsonObject.get("id").getAsString();
						if (!id.contains(":"))
						{
							jsonObject.addProperty("id", "http://aestheticodes.appspot.com/experience/" + id);
						}
					}

					if (jsonObject.has("markers"))
					{
						JsonElement markers = jsonObject.get("markers");
						jsonObject.add("actions", markers);

						if (markers.isJsonArray())
						{
							JsonArray markerArray = markers.getAsJsonArray();
							for (JsonElement element : markerArray)
							{
								if (element.isJsonObject())
								{
									JsonObject actionObject = element.getAsJsonObject();
									if (actionObject.has("title"))
									{
										actionObject.add("name", actionObject.get("title"));
									}

									if (actionObject.has("action"))
									{
										actionObject.add("url", actionObject.get("action"));
									}

									if (actionObject.has("code"))
									{
										JsonArray codeArray = new JsonArray();
										codeArray.add(actionObject.get("code"));
										actionObject.add("codes", codeArray);
									}
								}
							}
						}
					}
				}

				return delegate.fromJsonTree(tree);
			}

			@Override
			public void write(JsonWriter out, Experience value) throws IOException
			{
				delegate.write(out, value);
			}
		};
	}
}