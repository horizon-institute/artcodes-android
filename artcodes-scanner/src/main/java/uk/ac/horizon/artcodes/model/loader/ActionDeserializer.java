package uk.ac.horizon.artcodes.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import uk.ac.horizon.artcodes.model.Action;

import java.lang.reflect.Type;

public class ActionDeserializer implements JsonDeserializer<Action>
{

	@Override
	public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		final Action action = context.deserialize(json, Action.class);
		if (json.isJsonObject())
		{
			JsonObject jsonObject = json.getAsJsonObject();
			if (jsonObject.has("action"))
			{
				action.setUrl(jsonObject.get("action").getAsString());
			}

			if (jsonObject.has("title"))
			{
				action.setName(jsonObject.get("title").getAsString());
			}
		}

		return action;
	}
}