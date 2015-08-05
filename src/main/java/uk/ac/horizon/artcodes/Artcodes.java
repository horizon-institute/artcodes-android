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

import android.accounts.AccountManager;
import android.app.Application;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.account.AccountInfo;
import uk.ac.horizon.artcodes.account.AppEngineAccount;
import uk.ac.horizon.artcodes.account.LocalAccount;
import uk.ac.horizon.artcodes.source.ContentSource;
import uk.ac.horizon.artcodes.source.FileSource;
import uk.ac.horizon.artcodes.source.HTTPSource;

import java.util.ArrayList;
import java.util.List;

public final class Artcodes extends Application
{
	private Account account;

	@Override
	public void onCreate()
	{
		super.onCreate();
		GoogleAnalytics.initialize(this);

		// TODO SharedPreferences
		final List<AccountInfo> infos = getAccounts();
		setAccount(infos.get(0).create());
	}

	public Account getAccount()
	{
		return account;
	}

	public List<AccountInfo> getAccounts()
	{
		final List<AccountInfo> result = new ArrayList<>();
		final AccountManager manager = AccountManager.get(this);
		final android.accounts.Account[] accounts = manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		for(android.accounts.Account account: accounts)
		{
			result.add(new AppEngineAccount.Info(this, account.name));
		}
		result.add(new LocalAccount.Info(this));
		return result;
	}

	public void setAccount(Account account)
	{
		this.account = account;

		account.add(new ContentSource.Factory());
		account.add(new HTTPSource.Factory());
		account.add(new FileSource.Factory());

		Log.i("", "Set account " + account.getInfo().getId());
	}

	//public List<Ac>

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