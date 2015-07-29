package uk.ac.horizon.artcodes.json;

import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Type;

class GsonParser<T> implements JsonParser<T>
{
	private final Gson gson;
	private final Type type;

	GsonParser(Gson gson, Type type)
	{
		this.gson = gson;
		this.type = type;
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public T parse(String json)
	{
		return gson.fromJson(json, type);
	}

	@Override
	public T parse(Reader reader)
	{
		return gson.fromJson(reader, type);
	}

	@Override
	public String toJson(T object)
	{
		return gson.toJson(object, type);
	}
}
