package uk.ac.horizon.artcodes.source;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import uk.ac.horizon.artcodes.account.Account;

import java.lang.reflect.Type;

public class IntentSource<T> implements Source<T>
{
	private final Account account;
	private final Intent intent;
	private final Bundle bundle;
	private final Type type;

	public IntentSource(Account account, Intent intent, Bundle bundle, Type type)
	{
		this.account = account;
		this.intent = intent;
		this.bundle = bundle;
		this.type = type;
	}

	@Override
	public void loadInto(Target<T> target)
	{
		if (bundle != null && bundle.containsKey("experience"))
		{
			target.onLoaded(account.getGson().<T>fromJson(bundle.getString("experience"), type));
		}
		else
		{
			if (intent.hasExtra("experience"))
			{
				target.onLoaded(account.getGson().<T>fromJson(intent.getStringExtra("experience"), type));
			}
			else
			{
				final Uri data = intent.getData();
				if (data != null)
				{
					account.<T>getSource(data.toString(), type).loadInto(target);
				}
			}
		}
	}
}
