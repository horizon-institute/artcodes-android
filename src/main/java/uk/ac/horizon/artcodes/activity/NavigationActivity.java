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

package uk.ac.horizon.artcodes.activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.NavigationBinding;
import uk.ac.horizon.artcodes.fragment.ExperienceLibraryFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceRecentFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceRecommendFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceStarFragment;
import uk.ac.horizon.artcodes.fragment.FeatureListFragment;
import uk.ac.horizon.artcodes.server.LoadCallback;

import java.util.List;

public class NavigationActivity extends ArtcodeActivityBase implements
		NavigationView.OnNavigationItemSelectedListener
{
	private static final int REQUEST_CODE_PICK_ACCOUNT = 67;
	private static final long DRAWER_CLOSE_DELAY_MS = 250;
	private static final String NAV_ITEM_ID = "navItemId";

	private final Handler drawerActionHandler = new Handler();
	private NavigationBinding binding;
	private ActionBarDrawerToggle drawerToggle;
	private MenuItem selected;

	private String accountName;

	@Override
	public void onBackPressed()
	{
		if (binding.drawer.isDrawerOpen(GravityCompat.START))
		{
			binding.drawer.closeDrawer(GravityCompat.START);
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onNavigationItemSelected(final MenuItem menuItem)
	{
		if (menuItem != null)
		{
			if (menuItem.isCheckable())
			{
				if (selected != null)
				{
					selected.setChecked(false);
				}

				selected = menuItem;
				menuItem.setChecked(true);
			}
			drawerActionHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					navigate(menuItem, true);
				}
			}, DRAWER_CLOSE_DELAY_MS);
		}

		binding.drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == android.support.v7.appcompat.R.id.home)
		{
			return drawerToggle.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateAccounts()
	{
		final Menu menu = binding.navigation.getMenu();
		final MenuItem libraries = menu.findItem(R.id.nav_libraries);
		final SubMenu subMenu = libraries.getSubMenu();

		while (subMenu.size() > 0)
		{
			subMenu.removeItem(subMenu.getItem(0).getItemId());
		}

		List<Account> accounts = getServer().getAccounts();
		for (int index = 0; index < accounts.size(); index++)
		{
			Account account = accounts.get(index);
			MenuItem menuItem = subMenu.add(R.id.navigation, index, Menu.NONE, account.getName());
			menuItem.setIcon(R.drawable.ic_folder_24dp);
			menuItem.setCheckable(true);
		}

		for (int i = 0, count = binding.navigation.getChildCount(); i < count; i++)
		{
			final View child = binding.navigation.getChildAt(i);
			if (child != null && child instanceof ListView)
			{
				final ListView menuView = (ListView) child;
				final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
				final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
				wrapped.notifyDataSetChanged();
			}
		}

		getServer().loadRecent(new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> item)
			{
				MenuItem recent = menu.findItem(R.id.nav_recent);
				if (recent != null)
				{
					recent.setVisible(!item.isEmpty());
				}
			}
		});
		getServer().loadStarred(new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> item)
			{
				MenuItem starred = menu.findItem(R.id.nav_starred);
				if (starred != null)
				{
					starred.setVisible(!item.isEmpty());
				}
			}
		});
	}

	public void navigate(Fragment fragment, boolean addToBackStack)
	{
		if(addToBackStack)
		{
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content, fragment)
					.addToBackStack(null)
					.commit();
		}
		else
		{
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content, fragment)
					.commit();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			if (data != null)
			{
				if (data.hasExtra(AccountManager.KEY_ACCOUNT_NAME))
				{
					accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				}
			}

			Log.i("account", "Account = " + accountName);
			if (accountName != null)
			{
				tryGoogleAccount(accountName);
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(Feature.get(this, R.bool.feature_show_welcome).isEnabled())
		{
			startActivity(new Intent(this, AboutArtcodesActivity.class));
			return;
		}

		binding = DataBindingUtil.setContentView(this, R.layout.navigation);

		setSupportActionBar(binding.toolbar);

		binding.navigation.setNavigationItemSelectedListener(this);

		final View headerView = binding.navigation.inflateHeaderView(R.layout.navigation_header);

		final MenuItem featureItem = binding.navigation.getMenu().findItem(R.id.nav_features);
		if (Feature.get(this, R.bool.feature_edit_features).isEnabled())
		{
			featureItem.setVisible(true);
		}
		else
		{
			headerView.setLongClickable(true);
			headerView.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					featureItem.setVisible(true);
					Feature.get(getBaseContext(), R.bool.feature_edit_features).setEnabled(true);
					return false;
				}
			});
		}

		updateAccounts();

		drawerToggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.open, R.string.close)
		{
			@Override
			public void onDrawerOpened(View drawerView)
			{
				super.onDrawerOpened(drawerView);
				updateAccounts();
			}
		};
		binding.drawer.addDrawerListener(drawerToggle);

		drawerToggle.syncState();

		int navigationIndex = R.id.nav_home;
		if (savedInstanceState != null)
		{
			navigationIndex = savedInstanceState.getInt(NAV_ITEM_ID, R.id.nav_home);
		}
		MenuItem item = binding.navigation.getMenu().findItem(navigationIndex);
		if (item == null)
		{
			item = binding.navigation.getMenu().findItem(R.id.nav_home);
		}

		navigate(item, false);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (selected != null)
		{
			outState.putInt(NAV_ITEM_ID, selected.getItemId());
		}
	}

	private void tryGoogleAccount(final String name)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final Account account = getServer().createAccount("google:" + name);
					if (account.validates())
					{
						getServer().add(account);
						new Handler(Looper.getMainLooper()).post(new Runnable()
						{
							@Override
							public void run()
							{
								updateAccounts();
								final Bundle bundle = new Bundle();
								bundle.putString("account", account.getId());
								final Fragment fragment = new ExperienceLibraryFragment();
								fragment.setArguments(bundle);
								navigate(fragment, true);
							}
						});
					}
				}
				catch (UserRecoverableAuthException userRecoverableException)
				{
					Log.i("intent", "Intent = " + userRecoverableException.getIntent());
					startActivityForResult(userRecoverableException.getIntent(), REQUEST_CODE_PICK_ACCOUNT);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void navigate(MenuItem item, boolean addToBackStack)
	{
		switch (item.getItemId())
		{
			case R.id.nav_home:
				navigate(new ExperienceRecommendFragment(), addToBackStack);
				break;

			case R.id.nav_starred:
				navigate(new ExperienceStarFragment(), addToBackStack);
				break;

			case R.id.nav_features:
				navigate(new FeatureListFragment(), addToBackStack);
				break;

			case R.id.nav_recent:
				navigate(new ExperienceRecentFragment(), addToBackStack);
				break;

			case R.id.nav_about_artcodes:
				startActivity(new Intent(this, AboutArtcodesActivity.class));
				break;

			case R.id.nav_addaccount:
				startActivityForResult(AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQUEST_CODE_PICK_ACCOUNT);
				break;

			default:
				final List<Account> accounts = getServer().getAccounts();
				if (item.getItemId() < accounts.size())
				{
					final Account account = accounts.get(item.getItemId());
					Bundle bundle = new Bundle();
					bundle.putString("account", account.getId());
					final Fragment fragment = new ExperienceLibraryFragment();
					fragment.setArguments(bundle);
					navigate(fragment, addToBackStack);
				}
				break;
		}
	}
}
