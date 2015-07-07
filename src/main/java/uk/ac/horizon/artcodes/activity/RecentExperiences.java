package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class RecentExperiences
{
	private static final String EXPERIENCE_STORE = "uk.ac.horizon.aestheticodes.experiences";
	private static final String RECENT_LIST = "recent";

	public static RecentExperiences with(Context context)
	{
		return new RecentExperiences(context);
	}

	private final Context context;

	private RecentExperiences(Context context)
	{
		this.context = context;
	}

	public void add(String experienceID)
	{
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		String jsonString = preferences.getString(RECENT_LIST, "[]");

		Gson gson = new GsonBuilder().create();
		List<String> list = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());

		list.remove(experienceID);
		list.add(0, experienceID);

		preferences.edit().putString(RECENT_LIST, gson.toJson(list)).apply();
	}

	public List<String> get()
	{
		final SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EXPERIENCE_STORE, Context.MODE_PRIVATE);
		String jsonString = preferences.getString(RECENT_LIST, "[]");

		Gson gson = new GsonBuilder().create();
		return gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
	}
}
