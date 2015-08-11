package uk.ac.horizon.artcodes.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

public class IntentBuilder
{
	private ArtcodeServer server;
	private final Context context;
	private final Intent intent;

	private IntentBuilder(Context context)
	{
		this.context = context;
		this.intent = new Intent();
	}

	public IntentBuilder set(String key, String value)
	{
		intent.putExtra(key, value);
		return this;
	}

	public IntentBuilder setServer(ArtcodeServer server)
	{
		this.server = server;
		return this;
	}

	public IntentBuilder set(String key, Object value)
	{
		intent.putExtra(key, server.getGson().toJson(value));
		return this;
	}

	public IntentBuilder setAction(String action)
	{
		intent.setAction(action);
		return this;
	}

	public IntentBuilder setURI(String uri)
	{
		intent.setData(Uri.parse(uri));
		return this;
	}

	public Intent create()
	{
		return intent;
	}

	public IntentBuilder target(Class<? extends Activity> activity)
	{
		intent.setClass(context, activity);
		return this;
	}

	public void start()
	{
		context.startActivity(intent);
	}

	public static IntentBuilder with(Context context)
	{
		return new IntentBuilder(context);
	}
}
