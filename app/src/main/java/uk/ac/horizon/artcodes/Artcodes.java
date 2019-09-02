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

package uk.ac.horizon.artcodes;

import android.app.Application;
import android.os.Build;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import uk.ac.horizon.artcodes.server.AppEngineServer;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

public final class Artcodes extends Application
{
	public static final String userAgent = "Artcodes/" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + "; Android " + Build.VERSION.RELEASE + ")";
	private static final int cacheSize = 10 * 1024 * 1024; // 10MiB
	public static OkHttpClient httpClient;
	private ArtcodeServer server;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Analytics.initialize(this);

		server = new AppEngineServer(this);
		if(httpClient == null)
		{
			HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
			logging.level(HttpLoggingInterceptor.Level.BASIC);

			httpClient = new OkHttpClient.Builder()
					.addInterceptor(logging)
					.cache(new Cache(getCacheDir(), cacheSize))
					.build();
		}
	}

	public ArtcodeServer getServer()
	{
		return server;
	}
}