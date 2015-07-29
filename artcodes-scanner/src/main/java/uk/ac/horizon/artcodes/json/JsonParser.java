package uk.ac.horizon.artcodes.json;

import java.io.Reader;
import java.lang.reflect.Type;

public interface JsonParser<T>
{
	Type getType();

	T parse(String json);

	T parse(Reader reader);

	String toJson(T object);
}
