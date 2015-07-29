package uk.ac.horizon.artcodes.json;

import com.google.gson.reflect.TypeToken;

public interface JsonParserFactory
{
	<T> JsonParser<T> parserFor(Class<T> type);

	<T> JsonParser<T> parserFor(TypeToken<T> type);
}
