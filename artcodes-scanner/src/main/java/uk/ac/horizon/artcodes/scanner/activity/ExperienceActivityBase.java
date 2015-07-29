package uk.ac.horizon.artcodes.scanner.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import uk.ac.horizon.artcodes.ArtcodeStorage;
import uk.ac.horizon.artcodes.json.ExperienceParserFactory;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.storage.StoreListener;

public abstract class ExperienceActivityBase extends AppCompatActivity implements StoreListener<Experience>
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

    protected Intent createIntent(Class<?> activity)
    {
        Intent intent = new Intent(this, activity);
        if(isLoaded())
        {
            intent.putExtra("experience", ExperienceParserFactory.toJson(experience));
        }
        else
        {
            intent.setData(Uri.parse(getUri()));
        }
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

		if (intent.hasExtra("experience"))
		{
            ArtcodeStorage.load(Experience.class).fromJson(intent.getStringExtra("experience")).async(this);
		}
		else
		{
			final Uri data = intent.getData();
			if (data != null)
			{
                uri = data.toString();
                ArtcodeStorage.load(Experience.class).fromUri(uri).async(this);
			}
		}
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

       	if (savedInstanceState != null && savedInstanceState.containsKey("experience"))
		{
            ArtcodeStorage.load(Experience.class).fromJson(savedInstanceState.getString("experience")).async(this);
		}
        else
        {
            onNewIntent(getIntent());
        }
    }

    @Override
    public void onItemChanged(Experience experience)
    {
        this.experience = experience;
        if(experience != null)
        {
            uri = experience.getId();
        }
    }

    protected void startActivity(Class<?> activity)
    {
        this.startActivity(createIntent(activity));
    }
}
