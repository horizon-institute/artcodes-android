/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import com.google.android.gms.auth.GoogleAuthUtil;
import uk.ac.horizon.artcodes.json.ExperienceParserFactory;
import uk.ac.horizon.artcodes.storage.AppEngineStore;
import uk.ac.horizon.artcodes.storage.ContentStore;
import uk.ac.horizon.artcodes.storage.ExperienceFileStore;
import uk.ac.horizon.artcodes.storage.HTTPStore;
import uk.ac.horizon.artcodes.storage.JsonStore;
import uk.ac.horizon.artcodes.storage.Storage;

public final class Artcodes extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		GoogleAnalytics.initialize(this);

		Storage storage = new Storage(new ExperienceParserFactory(this));

		final AccountManager manager = AccountManager.get(this);
		final Account[] accounts = manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		for (Account account : accounts)
		{
			storage.register(new AppEngineStore(this, account.name));
		}

		storage.register(new ExperienceFileStore());
		storage.register(new ContentStore(this));
		storage.register(new JsonStore());
		storage.register(new JsonStore("x-artcode-scan"));
		storage.register(new HTTPStore(this));

		ArtcodeStorage.setStorage(storage);
	}

//	public static File createImageLogFile()
//	{
//		if(LOG_MARKER_IMAGE)
//		{
//			final String title = "IMG_" + System.currentTimeMillis();
//			final File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//			final File directory = new File(picturesDir, "Artcodes");
//			if (!directory.exists())
//			{
//				boolean success = directory.mkdir();
//			}
//			return new File(directory, title + ".jpg");
//		}
//		return null;
//	}
}