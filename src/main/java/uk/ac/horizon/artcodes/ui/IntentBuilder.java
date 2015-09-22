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

package uk.ac.horizon.artcodes.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import uk.ac.horizon.artcodes.server.ArtcodeServer;

public class IntentBuilder
{
	private final Context context;
	private final Intent intent;
	private ArtcodeServer server;

	private IntentBuilder(Context context)
	{
		this.context = context;
		this.intent = new Intent();
	}

	public static IntentBuilder with(Context context)
	{
		return new IntentBuilder(context);
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
}
