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

package uk.ac.horizon.artcodes.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.ExperienceEditBinding;
import uk.ac.horizon.artcodes.fragment.ExperienceEditActionFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditAvailabilityFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditColourFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditInfoFragment;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.ui.IntentBuilder;

public class ExperienceEditActivity extends ExperienceActivityBase
{
	public static final int IMAGE_PICKER_REQUEST = 121;
	public static final int ICON_PICKER_REQUEST = 123;
	private ExperienceEditBinding binding;
	private Account account;
	private List<Account> accounts;

	public void editIcon(View view)
	{
		selectImage(ICON_PICKER_REQUEST);
	}

	public void editImage(View view)
	{
		selectImage(IMAGE_PICKER_REQUEST);
	}

	public Account getAccount()
	{
		if (account == null)
		{
			if (getIntent().hasExtra("account"))
			{
				String accountID = getIntent().getStringExtra("account");
				Account account = getServer().getAccount(accountID);
				if (account != null)
				{
					this.account = account;
					return account;
				}
			}

			SharedPreferences preferences = getSharedPreferences(Account.class.getName(), MODE_PRIVATE);
			String accountID = preferences.getString(getUri(), null);
			if (accountID != null)
			{
				Account account = getServer().getAccount(accountID);
				if (account != null)
				{
					this.account = account;
					return account;
				}
			}

			account = getServer().getAccounts().get(0);
		}

		return account;
	}

	public void setAccount(Account account)
	{
		this.account = account;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.save_menu, menu);
		//updateSave();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, createCancelIntent());
				return true;
			case R.id.save:
				getAccount().saveExperience(getExperience());
				NavUtils.navigateUpTo(this, IntentBuilder.with(this)
						.target(ExperienceActivity.class)
						.setServer(getServer())
						.set("experience", getExperience())
						.create());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResponse(Experience experience)
	{
		super.onResponse(experience);
		GoogleAnalytics.trackScreen("Edit Experience", experience.getId());

		Account account = getAccount();

		int index = accounts.indexOf(account);
		binding.accountList.setSelection(index);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("", "Activity result for " + requestCode);
		Log.i("", "Activity result is " + resultCode);
		if (requestCode == IMAGE_PICKER_REQUEST)
		{
			if (resultCode == RESULT_OK)
			{
				Uri fullPhotoUri = data.getData();
				getExperience().setImage(fullPhotoUri.toString());
			}
		} else if (requestCode == ICON_PICKER_REQUEST)
		{
			if (resultCode == RESULT_OK)
			{
				Uri fullPhotoUri = data.getData();
				getExperience().setIcon(fullPhotoUri.toString());
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.experience_edit);
		binding.viewpager.setAdapter(new ExperienceEditPagerAdapter(getSupportFragmentManager()));
		binding.tabs.setupWithViewPager(binding.viewpager);

		if (savedInstanceState != null)
		{
			binding.viewpager.setCurrentItem(savedInstanceState.getInt("tab", 0));
		}

		setSupportActionBar(binding.toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		accounts = getServer().getAccounts();
		binding.accountList.setAdapter(new ArrayAdapter<>(this, R.layout.account_item, accounts));
		binding.accountList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				Account account = (Account) binding.accountList.getAdapter().getItem(position);
				if (account != null)
				{
					setAccount(account);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		binding.toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putInt("tab", binding.viewpager.getCurrentItem());
		outState.putString("experience", getServer().getGson().toJson(getExperience()));
	}

	private Intent createCancelIntent()
	{
		Intent intent = (Intent) getIntent().clone();
		intent.setClass(this, ExperienceActivity.class);
		return intent;
	}

	private void selectImage(int request_id)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivityForResult(intent, request_id);
		}
	}

	public class ExperienceEditPagerAdapter extends FragmentPagerAdapter
	{
		private final List<ExperienceEditFragment> fragments = new ArrayList<>();

		public ExperienceEditPagerAdapter(FragmentManager fm)
		{
			super(fm);

			fragments.add(new ExperienceEditInfoFragment());
			fragments.add(new ExperienceEditAvailabilityFragment());
			fragments.add(new ExperienceEditActionFragment());
			if (Feature.get(getApplicationContext(), R.bool.feature_edit_colour).isEnabled())
			{
				fragments.add(new ExperienceEditColourFragment());
			}
		}

		@Override
		public int getCount()
		{
			return fragments.size();
		}

		@Override
		public Fragment getItem(int position)
		{
			return fragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			// Generate title based on item position
			return getString(fragments.get(position).getTitleResource());
		}
	}
}