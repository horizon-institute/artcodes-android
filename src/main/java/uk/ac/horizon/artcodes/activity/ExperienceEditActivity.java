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

import android.app.DatePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ExperienceEditActionBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceEditActionCodeBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceEditAvailabilityBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceEditBinding;
import uk.ac.horizon.artcodes.dialog.EditChecksumDialog;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Availability;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.model.loader.ExperienceLoader;
import uk.ac.horizon.artcodes.model.loader.LoadListener;
import uk.ac.horizon.artcodes.model.loader.Ref;

import java.util.Calendar;

public class ExperienceEditActivity extends AppCompatActivity
{
	private static final int PLACE_PICKER_REQUEST = 119;
	private static final int IMAGE_PICKER_REQUEST = 121;
	private static final int ICON_PICKER_REQUEST = 123;

	private ExperienceEditBinding binding;
	private Ref<Experience> experience;

	public void addAction(View view)
	{
		experience.get().getActions().add(new Action());
		updateActions();
	}

	public void addAvailability(View view)
	{
		experience.get().getAvailabilities().add(new Availability());
		updateAvailabilities();
	}

	public void editChecksum(View view)
	{
		new EditChecksumDialog().show(getSupportFragmentManager(), "ChecksumModulo");
	}

	public void editIcon(View view)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivityForResult(intent, ICON_PICKER_REQUEST);
		}
	}

	public void editImage(View view)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivityForResult(intent, IMAGE_PICKER_REQUEST);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.save_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, upIntent());
				return true;
			case R.id.save:
				// TODO binding.getExperience().save(Experience.Operation.update);
				NavUtils.navigateUpTo(this, upIntent());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PLACE_PICKER_REQUEST)
		{
			if (resultCode == RESULT_OK)
			{
				final Place place = PlacePicker.getPlace(data, this);
				final int index = data.getIntExtra("availIndex", 0);
				if (index >= 0)
				{
					final Availability availability = binding.getExperience().getAvailabilities().get(index);
					availability.setName(place.getName().toString());
					availability.setAddress(place.getAddress().toString());
					availability.setLat(place.getLatLng().latitude);
					availability.setLon(place.getLatLng().longitude);
				}
			}
		}
		else if (requestCode == IMAGE_PICKER_REQUEST)
		{
			if (resultCode == RESULT_OK)
			{
				Bitmap thumbnail = data.getParcelableExtra("data");
				Uri fullPhotoUri = data.getData();

			}
		}
		else if (requestCode == ICON_PICKER_REQUEST)
		{
			if (resultCode == RESULT_OK)
			{

			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.experience_edit);
		experience = ExperienceLoader.from(savedInstanceState, getIntent());
		experience.load(this, new LoadListener<Experience>()
		{
			@Override
			public void onLoaded(Experience item)
			{
				if (item != null)
				{
					GoogleAnalytics.trackEvent("Experience", "Loaded " + item.getId());
				}
				binding.setExperience(item);
			}
		});

		binding.addOnRebindCallback(new OnRebindCallback()
		{
			@Override
			public void onBound(ViewDataBinding binding)
			{
				updateActions();
				updateAvailabilities();
			}
		});

		setSupportActionBar(binding.toolbar);

		binding.toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// TODO outState.putString("experience", binding.getExperience().toJson());
	}

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

	private void showDatePickerDialog(long timestamp, DatePickerDialog.OnDateSetListener listener)
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		int mYear = calendar.get(Calendar.YEAR);
		int mMonth = calendar.get(Calendar.MONTH);
		int mDay = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dialog = new DatePickerDialog(this, listener, mYear, mMonth, mDay);
		dialog.show();
	}

	private Intent upIntent()
	{
		final Intent intent = new Intent(this, ExperienceActivity.class);
		intent.putExtra("experience", experience.getUri());

		return intent;
	}

	private void updateActions()
	{
		binding.actionList.removeAllViews();
		if (binding.getExperience() != null)
		{
			for (Action action : binding.getExperience().getActions())
			{
				ExperienceEditActionBinding markerBinding = ExperienceEditActionBinding.inflate(getLayoutInflater(), binding.actionList, false);
				markerBinding.setAction(action);
				for (String code : action.getCodes())
				{
					ExperienceEditActionCodeBinding codeBinding = ExperienceEditActionCodeBinding.inflate(getLayoutInflater(), markerBinding.markerCodes, false);
					codeBinding.setAction(action);
					markerBinding.markerCodes.addView(codeBinding.getRoot());
				}

				binding.actionList.addView(markerBinding.getRoot());
			}
		}
	}

	private void updateAvailabilities()
	{
		binding.availabilityList.removeAllViews();
		if (binding.getExperience() != null)
		{
			for (Availability availability : binding.getExperience().getAvailabilities())
			{
				ExperienceEditAvailabilityBinding availabilityBinding = ExperienceEditAvailabilityBinding.inflate(getLayoutInflater(), binding.availabilityList, false);
				availabilityBinding.setMarker(availability);
				binding.availabilityList.addView(availabilityBinding.getRoot());
			}
		}
	}
}
