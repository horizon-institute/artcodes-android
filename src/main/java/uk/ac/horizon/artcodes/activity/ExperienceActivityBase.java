package uk.ac.horizon.artcodes.activity;

import android.os.Bundle;
import android.util.Log;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.IntentSource;
import uk.ac.horizon.artcodes.request.RequestCallback;

public abstract class ExperienceActivityBase extends ArtcodeActivityBase implements RequestCallback<Experience>
{
	private String uri;
	private Experience experience;

	public Experience getExperience()
	{
		return experience;
	}

	public String getUri()
	{
		return uri;
	}

	protected boolean isLoaded()
	{
		return experience != null;
	}

	@Override
	public void onResponse(Experience item)
	{
		experience = item;
		if (experience != null)
		{
			uri = experience.getId();
		}
	}

	@Override
	public void onError(Exception e)
	{
		GoogleAnalytics.trackException(e);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		new IntentSource<Experience>(getServer(), getIntent(), savedInstanceState, Experience.class).loadInto(this);
	}
}
