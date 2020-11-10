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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.databinding.ExperienceEditBinding;
import uk.ac.horizon.artcodes.fragment.ActionEditListFragment;
import uk.ac.horizon.artcodes.fragment.AvailabilityEditListFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditColourFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditInfoFragment;
import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceEditActivity extends ExperienceActivityBase
{
	private class ExperienceEditPagerAdapter extends FragmentPagerAdapter
	{
		private final List<ExperienceEditFragment> fragments = new ArrayList<>();

		private ExperienceEditPagerAdapter(FragmentManager fm)
		{
			super(fm);

			fragments.add(new ExperienceEditInfoFragment());
			fragments.add(new ActionEditListFragment());
			fragments.add(new AvailabilityEditListFragment());
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

	private static final int IMAGE_PICKER_REQUEST = 121;
	private static final int ICON_PICKER_REQUEST = 123;
	private ExperienceEditBinding binding;
	private Account account;
	private ExperienceEditPagerAdapter adapter;

	public static void start(Context context, Experience experience, Account account)
	{
		Intent intent = new Intent(context, ExperienceEditActivity.class);
		intent.putExtra("experience", new Gson().toJson(experience));
		intent.putExtra("account", account.getId());
		context.startActivity(intent);
	}

	public void nextPage(View view)
	{
		binding.viewpager.setCurrentItem(binding.viewpager.getCurrentItem() + 1);
	}

	public void prevPage(View view)
	{
		binding.viewpager.setCurrentItem(Math.max(0, binding.viewpager.getCurrentItem() - 1));
	}

	public void saveExperience(View view)
	{
		final Experience experience = getExperience();
		final boolean isNew = experience.getId() == null;
		final Activity activity = this;
		final ProgressDialog dialog = ProgressDialog.show(activity, getResources().getString(R.string.saving_progress_dialog_title), getResources().getString(R.string.saving_progress_dialog_message), true);
		getAccount().saveExperience(experience, (success, savedExperience) -> {
			dialog.dismiss();
			if (success)
			{
				if (isNew)
				{
					Intent intent = ExperienceActivity.intent(activity, savedExperience);
					startActivity(intent);
					activity.finish();
				}
				else
				{
					NavUtils.navigateUpTo(activity, ExperienceActivity.intent(activity, experience));
				}
			}
			else
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						new AlertDialog.Builder(activity)
								.setTitle(R.string.saving_error_title)
								.setMessage(R.string.saving_error_message)
								.setPositiveButton(R.string.saving_error_button, (dialog1, which) -> dialog1.dismiss())
								.show();
					}
				});
			}
		});
	}

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
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, createCancelIntent());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void loaded(Experience experience)
	{
		super.loaded(experience);

		if (experience.getId() == null)
		{
			binding.deleteButton.setVisibility(View.GONE);
			binding.tabs.setVisibility(View.GONE);
			binding.saveButton.setVisibility(View.GONE);
			binding.prevButton.setVisibility(View.GONE);
			binding.nextButton.setVisibility(View.VISIBLE);
		}
		else
		{
			binding.deleteButton.setVisibility(View.VISIBLE);
			binding.tabs.setVisibility(View.VISIBLE);
			binding.saveButton.setVisibility(View.VISIBLE);
			binding.prevButton.setVisibility(View.GONE);
			binding.nextButton.setVisibility(View.GONE);
		}
	}

	public View getRoot()
	{
		return binding.getRoot();
	}

	public void deleteExperience(View view)
	{
		final Activity activity = this;
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.experienceDeleteConfirm, getExperience().getName()))
				.setPositiveButton(R.string.delete, (dialog, whichButton) -> {
					final ProgressDialog progressDialog = ProgressDialog.show(activity, getResources().getString(R.string.delete_progress_dialog_title), getResources().getString(R.string.delete_progress_dialog_message), true);

					getAccount().deleteExperience(getExperience(), (success, experience) -> {
						progressDialog.dismiss();
						if (success)
						{
							NavUtils.navigateUpTo(ExperienceEditActivity.this, new Intent(ExperienceEditActivity.this, NavigationActivity.class));
						}
						else
						{
							runOnUiThread(() -> new AlertDialog.Builder(activity)
									.setTitle(R.string.delete_error_title)
									.setMessage(R.string.delete_error_message)
									.setPositiveButton(R.string.delete_error_button, (dialog1, which) -> dialog1.dismiss())
									.show());
						}
					});
				})
				.setNegativeButton(android.R.string.cancel, null).show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			if (requestCode == IMAGE_PICKER_REQUEST)
			{
				Uri fullPhotoUri = data.getData();
				getExperience().setImage(fullPhotoUri.toString());
			}
			else if (requestCode == ICON_PICKER_REQUEST)
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
		adapter = new ExperienceEditPagerAdapter(getSupportFragmentManager());
		binding.viewpager.setAdapter(adapter);
		binding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				Fragment fragment = adapter.getItem(position);
				if (fragment instanceof ExperienceEditFragment)
				{
					ExperienceEditFragment experienceEditFragment = (ExperienceEditFragment) fragment;
					if (experienceEditFragment.displayAddFAB())
					{
						binding.add.show();
					}
					else
					{
						binding.add.hide();
					}
				}
				else
				{
					binding.add.hide();
				}

				Experience experience = getExperience();
				if (experience == null || experience.getId() == null)
				{

					if (position == 0)
					{
						binding.saveButton.setVisibility(View.GONE);
						binding.prevButton.setVisibility(View.GONE);
						binding.nextButton.setVisibility(View.VISIBLE);
					}
					else if (position == adapter.getCount() - 1)
					{
						binding.saveButton.setVisibility(View.VISIBLE);
						binding.prevButton.setVisibility(View.VISIBLE);
						binding.nextButton.setVisibility(View.GONE);
					}
					else
					{
						binding.saveButton.setVisibility(View.GONE);
						binding.prevButton.setVisibility(View.VISIBLE);
						binding.nextButton.setVisibility(View.VISIBLE);
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
			}
		});
		binding.add.setOnClickListener(v -> {
			int position = binding.viewpager.getCurrentItem();
			Fragment fragment = adapter.getItem(position);
			if (fragment instanceof ExperienceEditFragment)
			{
				ExperienceEditFragment experienceEditFragment = (ExperienceEditFragment) fragment;
				if (experienceEditFragment.displayAddFAB())
				{
					experienceEditFragment.add();
				}
			}
		});
		binding.tabs.setupWithViewPager(binding.viewpager);

		if (savedInstanceState != null)
		{
			binding.viewpager.setCurrentItem(savedInstanceState.getInt("tab", 0));
		}

		setSupportActionBar(binding.toolbar);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}

		binding.toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putInt("tab", binding.viewpager.getCurrentItem());
		outState.putString("experience", new Gson().toJson(getExperience()));
	}

	private Intent createCancelIntent()
	{
		Intent intent = (Intent) getIntent().clone();
		intent.setClass(this, ExperienceActivity.class);
		return intent;
	}

//	private void updateFragment()
//	{
//		int position = binding.viewpager.getCurrentItem();
//		Fragment fragment = adapter.getItem(position);
//		if (fragment instanceof ExperienceEditFragment)
//		{
//			ExperienceEditFragment experienceEditFragment = (ExperienceEditFragment) fragment;
//			experienceEditFragment.update();
//		}
//	}

	private void selectImage(int request_id)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivityForResult(intent, request_id);
		}
	}
}
