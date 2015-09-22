/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.request;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.lang.reflect.Type;

import uk.ac.horizon.artcodes.server.ArtcodeServer;

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
		} else
		{
			if (intent.hasExtra("experience"))
			{
				target.onResponse(server.getGson().<T>fromJson(intent.getStringExtra("experience"), type));
			} else
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
