package uk.ac.horizon.artcodes.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ExperienceListStore
{
	private static final String EXPERIENCE_STORE = "uk.ac.horizon.aestheticodes.experiences";
	private final String id;

	public static ExperienceListStore with(Context context, String id)
	{
		return new ExperienceListStore(context, id);
	}

	private final Context context;

	private ExperienceListStore(Context context, String id)
	{
		this.context = context;
		this.id = id;
	}

	public void add(String experienceID)
	{
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		String jsonString = preferences.getString(id, "[]");

		Gson gson = new GsonBuilder().create();
		List<String> list = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());

		list.remove(experienceID);
		list.add(0, experienceID);

		preferences.edit().putString(id, gson.toJson(list)).apply();
	}

	public void remove(String experienceID)
	{
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		String jsonString = preferences.getString(id, "[]");

		Gson gson = new GsonBuilder().create();
		List<String> list = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());

		list.remove(experienceID);

		preferences.edit().putString(id, gson.toJson(list)).apply();
	}

	public boolean contains(String item)
	{
		List<String> list = get();
		return list.contains(item);
	}

	public void set(List<String> list)
	{
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		Gson gson = new GsonBuilder().create();
		preferences.edit().putString(id, gson.toJson(list)).apply();
	}

	public List<String> get()
	{
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		String jsonString = preferences.getString(id, "[]");

		Log.i("", id + " = " + jsonString);

		Gson gson = new GsonBuilder().create();
		return gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
	}
}
