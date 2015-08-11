package uk.ac.horizon.artcodes.request;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class IDList extends AbstractList<String>
{
	private final SharedPreferences preferences;
	private final String name;
	private final Gson gson;
	private boolean editing = false;
	private final List<String> ids = new ArrayList<>();

	public IDList(Context context, String packageName, String name)
	{
		this.name = name;
		preferences = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
		this.gson = new GsonBuilder().create();
		load();
	}

	public void beginEdit()
	{
		editing = true;
	}

	public void commit()
	{
		editing = false;
		save();
	}

	@Override
	public String get(int location)
	{
		return ids.get(location);
	}

	@Override
	public void add(int location, String object)
	{
		ids.add(location, object);
		if(!editing)
		{
			save();
		}
	}

	private void load()
	{
		ids.clear();
		String jsonPreferences = preferences.getString(name, "[]");
		Log.i("", name + " = " + jsonPreferences);
		ids.addAll(gson.<List<String>>fromJson(jsonPreferences, new TypeToken<List<String>>() {}.getType()));
	}

	private void save()
	{
		String json = gson.toJson(ids);
		Log.i("", "Save " + name + " = " + json);
		preferences.edit().putString(name, json).apply();
	}

	@Override
	public String remove(int location)
	{
		String result = ids.remove(location);
		if(!editing)
		{
			save();
		}
		return result;
	}

	@Override
	public String set(int location, String object)
	{
		String result = ids.set(location, object);
		if(!editing)
		{
			save();
		}
		return result;
	}

	@Override
	public int size()
	{
		return ids.size();
	}
}
