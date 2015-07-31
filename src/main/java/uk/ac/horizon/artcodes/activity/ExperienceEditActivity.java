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

package uk.ac.horizon.artcodes.activity;

import android.content.Context;
import android.content.Intent;
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
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditBinding;
import uk.ac.horizon.artcodes.fragment.ExperienceEditActionFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditAvailabilityFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditInfoFragment;
import uk.ac.horizon.artcodes.fragment.ExperienceEditColourFragment;
import uk.ac.horizon.artcodes.json.ExperienceParserFactory;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.activity.ExperienceActivityBase;
import uk.ac.horizon.artcodes.storage.ExperienceStorage;

import java.util.ArrayList;
import java.util.List;

public class ExperienceEditActivity extends ExperienceActivityBase
{
	public static void start(Context context, Experience experience)
	{
		Intent intent = new Intent(context, ExperienceEditActivity.class);
		intent.putExtra("experience", ExperienceParserFactory.toJson(experience));
		context.startActivity(intent);
	}

	public static final int IMAGE_PICKER_REQUEST = 121;
	public static final int ICON_PICKER_REQUEST = 123;

	public class ExperienceEditPagerAdapter extends FragmentPagerAdapter
	{
		private final List<ExperienceEditFragment> fragments = new ArrayList<>();

		public ExperienceEditPagerAdapter(FragmentManager fm)
		{
			super(fm);

			fragments.add(new ExperienceEditInfoFragment());
			fragments.add(new ExperienceEditAvailabilityFragment());
			fragments.add(new ExperienceEditActionFragment());
			if(Feature.get(getApplicationContext(), R.bool.feature_edit_colour).isEnabled())
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

	private ExperienceEditBinding binding;

	public void editIcon(View view)
	{
		selectImage(ICON_PICKER_REQUEST);
	}

	public void editImage(View view)
	{
		selectImage(IMAGE_PICKER_REQUEST);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.save_menu, menu);
		//updateSave();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemChanged(Experience experience)
	{
		super.onItemChanged(experience);
		if (experience != null)
		{
			GoogleAnalytics.trackEvent("Experience", "Loaded " + experience.getId());
		}
	}

	private Intent createCancelIntent()
	{
		Intent intent = (Intent) getIntent().clone();
		intent.setClass(this, ExperienceActivity.class);
		return intent;
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
				ExperienceStorage.save(getExperience()).to(ExperienceStorage.getDefaultStore()).async();
				NavUtils.navigateUpTo(this, createIntent(ExperienceActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
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
		}
		else if (requestCode == ICON_PICKER_REQUEST)
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

		binding.toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
		binding.toolbar.setTitle("");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putInt("tab", binding.viewpager.getCurrentItem());
		outState.putString("experience", ExperienceParserFactory.toJson(getExperience()));
	}
//
//	private void updateSave()
//	{
//		ExperienceListStore store = ExperienceListStore.with(this, "library");
//		if(getUri() == null)
//		{
//			saveItem.setTitle("Create");
//		}
//		else if(store.contains(getUri()))
//		{
//			saveItem.setTitle(R.string.save);
//		}
//		else
//		{
//			saveItem.setTitle("Create Copy");
//		}
//	}

	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalytics.trackScreen("Experience Edit Screen");
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
}
