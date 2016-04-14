/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.server;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Type;

public class JsonCallback<T> implements URILoaderCallback
{
	private final Handler mainHandler;
	private final Gson gson;
	private final LoadCallback<T> callback;
	private final Type type;

	public JsonCallback(Type type, Gson gson, Context context, LoadCallback<T> callback)
	{
		this.gson = gson;
		mainHandler = new Handler(context.getMainLooper());
		this.callback = callback;
		this.type = type;
	}

	@Override
	public void onLoaded(Reader reader)
	{
		final T item = gson.fromJson(reader, type);
		//Log.i("JSON", gson.toJson(item));
		mainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				callback.loaded(item);
			}
		});
	}

	@Override
	public void onError(Exception e)
	{
		callback.error(e);
	}
}
