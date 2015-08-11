package uk.ac.horizon.artcodes.request;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

import java.lang.reflect.Type;

public class IntentSource<T> implements Request<T>
{
	private final ArtcodeServer server;
	private final Intent intent;
	private final Bundle bundle;
	private final Type type;

	public IntentSource(ArtcodeServer server, Intent intent, Bundle bundle, Type type)
	{
		this.server = server;
		this.intent = intent;
		this.bundle = bundle;
		this.type = type;
	}

	@Override
	public void loadInto(RequestCallback<T> target)
	{
		if (bundle != null && bundle.containsKey("experience"))
		{
			target.onResponse(server.getGson().<T>fromJson(bundle.getString("experience"), type));
		}
		else
		{
			if (intent.hasExtra("experience"))
			{
				target.onResponse(server.getGson().<T>fromJson(intent.getStringExtra("experience"), type));
			}
			else
			{
				final Uri data = intent.getData();
				if (data != null)
				{
					server.load(data.toString(), type, target);
				}
			}
		}
	}
}
