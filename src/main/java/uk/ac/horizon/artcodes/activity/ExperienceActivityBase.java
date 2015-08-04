package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.ExperienceParser;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.source.IntentSource;
import uk.ac.horizon.artcodes.source.Target;

public abstract class ExperienceActivityBase extends ArtcodeActivityBase implements Target<Experience>
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

	public static Intent createIntent(Context context, Class<?> activity, Experience experience)
	{
		Intent intent = new Intent(context, activity);
		intent.putExtra("experience", ExperienceParser.createGson(context).toJson(experience));
		return intent;
	}

    protected Intent createIntent(Class<?> activity)
    {
        Intent intent = new Intent(this, activity);
        if(isLoaded())
        {
            intent.putExtra("experience", getAccount().getGson().toJson(experience));
        }
        else
        {
            intent.setData(Uri.parse(getUri()));
        }
        return intent;
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		new IntentSource<Experience>(getAccount(), getIntent(), savedInstanceState, Experience.class).loadInto(this);
	}

    @Override
    public void onLoaded(Experience experience)
    {
        this.experience = experience;
        if(experience != null)
        {
            uri = experience.getId();

	        GoogleAnalytics.trackEvent("Experience", "Loaded " + experience.getId());
        }
    }

    protected void startActivity(Class<?> activity)
    {
        this.startActivity(createIntent(activity));
    }
}
