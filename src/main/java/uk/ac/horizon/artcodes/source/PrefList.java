package uk.ac.horizon.artcodes.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public abstract class PrefList<T> implements UriList<T>
{
	private final SharedPreferences preferences;
	private final String name;
	private final Gson gson;

	public PrefList(Context context, String packageName, String name)
	{
		this.name = name;
		preferences = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
		this.gson = new GsonBuilder().create();
	}

	@Override
	public void add(String uri)
	{
		List<String> list = getList();
		list.remove(uri);
		list.add(0, uri);
		preferences.edit().putString(name, gson.toJson(list)).apply();
	}

	@Override
	public boolean contains(String uri)
	{
		return getList().contains(uri);
	}

	protected List<String> getList()
	{
		String jsonString = preferences.getString(name, "[]");

		Log.i("", name + " = " + jsonString);

		return gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
	}

	@Override
	public void remove(String uri)
	{
		List<String> list = getList();
		list.remove(uri);
		preferences.edit().putString(name, gson.toJson(list)).apply();

	}
}
