package uk.ac.horizon.artcodes;

import android.content.Context;
import android.content.SharedPreferences;

public final class Feature
{
	public static Feature get(Context context, int feature)
	{
		return new Feature(context, feature);
	}

	private final Context context;
	private final int featureID;

	private Feature(Context context, int featureID)
	{
		this.context = context;
		this.featureID = featureID;
	}

	public int getId()
	{
		return featureID;
	}

	public String getName()
	{
		return context.getResources().getResourceEntryName(featureID);
	}

	public boolean isEnabled()
	{
		final SharedPreferences preferences = context.getSharedPreferences(Feature.class.getName(), Context.MODE_PRIVATE);
		final String featureName = getName();
		if (preferences.contains(featureName))
		{
			return preferences.getBoolean(featureName, false);
		}
		return context.getResources().getBoolean(featureID);
	}

	public void setEnabled(boolean enabled)
	{
		final SharedPreferences preferences = context.getSharedPreferences(Feature.class.getName(), Context.MODE_PRIVATE);
		final String featureName = getName();
		preferences.edit().putBoolean(featureName, enabled).apply();
	}
}
