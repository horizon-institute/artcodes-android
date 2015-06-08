/*
 * Aestheticodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.aestheticodes.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.ControllerActivity;
import uk.ac.horizon.aestheticodes.controllers.ExperienceLoader;
import uk.ac.horizon.aestheticodes.controllers.ExperienceParser;
import uk.ac.horizon.aestheticodes.controllers.adapters.DateAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.ListAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.MarkerChecksumAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.MarkerRegionAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.URLAdapter;
import uk.ac.horizon.aestheticodes.controllers.bindings.Action;
import uk.ac.horizon.aestheticodes.dialogs.EditMarkerChecksumDialog;
import uk.ac.horizon.aestheticodes.dialogs.EditMarkerDialog;
import uk.ac.horizon.aestheticodes.dialogs.EditMarkerRegionSizeDialog;
import uk.ac.horizon.aestheticodes.dialogs.EditMarkerRegionValueDialog;
import uk.ac.horizon.aestheticodes.model.Availability;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;

import java.util.Calendar;
import java.util.UUID;

public class ExperienceEditActivity extends ControllerActivity<Experience>
{
	private static final int PLACE_PICKER_REQUEST = 119;
	private static final int IMAGE_PICKER_REQUEST = 121;
	private static final int ICON_PICKER_REQUEST = 123;

	private ListAdapter<Experience, Marker> markerList;
	private ListAdapter<Experience, Availability> availabilityList;
	private LinearLayout markerSettings;
	private ImageView markerSettingsIcon;

	public void addAvailability(View view)
	{
		getModel().getAvailabilities().add(new Availability());
		notifyChanges("availabilities");
	}

	public void addMarker(View view)
	{
		DialogFragment newFragment = new EditMarkerDialog();
		newFragment.show(getSupportFragmentManager(), "marker.edit");
	}

	public void editChecksum(View view)
	{
		new EditMarkerChecksumDialog().show(getSupportFragmentManager(), "ChecksumModulo");
	}

//	public void editIcon(View view)
//	{
//		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//		intent.setType("image/*");
//		if (intent.resolveActivity(getPackageManager()) != null)
//		{
//			startActivityForResult(intent, ICON_PICKER_REQUEST);
//		}
//	}
//
//	public void editImage(View view)
//	{
//		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//		intent.setType("image/*");
//		if (intent.resolveActivity(getPackageManager()) != null)
//		{
//			startActivityForResult(intent, IMAGE_PICKER_REQUEST);
//		}
//	}

	public void editMaxRegionValue(View view)
	{
		new EditMarkerRegionValueDialog().show(getSupportFragmentManager(), "MaxRegionValue");
	}

	public void editRegions(View view)
	{
		new EditMarkerRegionSizeDialog().show(getSupportFragmentManager(), "Regions");
	}

	public Controller<Marker> getMarker(Marker marker)
	{
		return markerList.getController(marker);
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
				if (getModel().getOp() == null)
				{
					getModel().setOp(Experience.Operation.update);
				}
				// TODO ExperienceFileController.save(this, experiences);

				NavUtils.navigateUpTo(this, upIntent());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void toggleMarkerSettings(View view)
	{
		if (markerSettings.getVisibility() == View.VISIBLE)
		{
			markerSettings.setVisibility(View.GONE);
			markerSettingsIcon.setImageResource(R.drawable.ic_expand_more_24dp);
		}
		else
		{
			markerSettings.setVisibility(View.VISIBLE);
			markerSettingsIcon.setImageResource(R.drawable.ic_expand_less_24dp);
		}
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
					final Availability availability = getModel().getAvailabilities().get(index);
					final Controller<Availability> controller = availabilityList.getController(availability);
					availability.setName(place.getName().toString());
					availability.setAddress(place.getAddress().toString());
					availability.setLat(place.getLatLng().latitude);
					availability.setLon(place.getLatLng().longitude);
					controller.notifyChanges("name", "lat", "lon");
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
		else if(requestCode == ICON_PICKER_REQUEST)
		{
			if(resultCode == RESULT_OK)
			{

			}
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

	private void selectImage(int request_id)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivityForResult(intent, request_id);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.experience_edit);

		bindView(R.id.experienceTitle, "name");
		bindView(R.id.experienceDescription, "description");

		bindView(R.id.experienceIcon, "icon");
		bindView(R.id.experienceImage, "image");
		bindView(R.id.experienceImage, new Action<Experience>() {
			@Override
			public void execute(Context context, Controller<Experience> controller)
			{
				selectImage(IMAGE_PICKER_REQUEST);
			}
		});

		bindView(R.id.markerRegionValue, "maxRegionValue");
		bindView(R.id.markerChecksum, new MarkerChecksumAdapter(this));
		bindView(R.id.markerRegions, new MarkerRegionAdapter(this));

		markerList = new ListAdapter<>("markers", Marker.comparator, R.layout.marker_listitem);
		markerList.bindView(R.id.markerTitle, "title");
		markerList.bindView(R.id.markerCode, "code");
		markerList.bindView(R.id.markerAction, new URLAdapter<Marker>("action"));
		markerList.bindView(R.id.markerEdit, new Action<Marker>()
		{
			@Override
			public void execute(Context context, Controller<Marker> controller)
			{
				EditMarkerDialog.create(controller.getModel()).show(getSupportFragmentManager(), "marker.edit");
			}
		});
		markerList.bindView(R.id.markerDelete, new Action<Marker>()
		{
			@Override
			public void execute(Context context, Controller<Marker> controller)
			{
				getModel().getMarkers().remove(controller.getModel().getCode());
				notifyChanges("markers");
			}
		});
		bindView(R.id.markerList, markerList);

		availabilityList = new ListAdapter<>("availabilities", R.layout.availability_listitem);
		availabilityList.bindView(R.id.availabilityStart, new DateAdapter<Availability>("start"));
		availabilityList.bindView(R.id.availabilityStart, new Action<Availability>() {
			@Override
			public void execute(Context context, final Controller<Availability> controller)
			{
				showDatePickerDialog(controller.getModel().getStart(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
					{
						controller.notifyChanges("start");
					}
				});
			}
		});
		availabilityList.bindView(R.id.availabilityEnd, new DateAdapter<Availability>("end"));
		availabilityList.bindView(R.id.availabilityEnd, new Action<Availability>() {
			@Override
			public void execute(Context context, final Controller<Availability> controller)
			{
				showDatePickerDialog(controller.getModel().getEnd(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
					{
						controller.notifyChanges("end");
					}
				});
			}
		});
		availabilityList.bindView(R.id.availabilityLocation, "name");
		availabilityList.bindView(R.id.availabilityLocation, new Action<Availability>() {
			@Override
			public void execute(Context context, Controller<Availability> availability)
			{
				try
				{
					final int index = getModel().getAvailabilities().indexOf(availability.getModel());
					final PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
					final Intent intent = builder.build(ExperienceEditActivity.this);
					intent.putExtra("availIndex", index);

					startActivityForResult(intent, PLACE_PICKER_REQUEST);
				}
				catch (Exception e)
				{
					Log.w("", e.getMessage(), e);
				}
			}
		});
		availabilityList.bindView(R.id.availabilityDelete, new Action<Availability>()
		{
			@Override
			public void execute(Context context, Controller<Availability> object)
			{
				getModel().getAvailabilities().remove(object.getModel());
				notifyChanges("availabilities");
			}
		});
		bindView(R.id.availabilityList, availabilityList);

		final String experienceID = getIntent().getStringExtra("experience");
		if (savedInstanceState != null && savedInstanceState.getString("experience") != null)
		{
			setModel(ExperienceParser.createParser().fromJson(savedInstanceState.getString("experience"), Experience.class));
		}
		else if (experienceID != null)
		{
			new ExperienceLoader(this)
			{
				@Override
				protected void onProgressUpdate(Experience... values)
				{
					if (values != null && values.length != 0)
					{
						setModel(values[0]);
					}
				}
			}.execute(experienceID);
		}
		else
		{
			Experience newExperience = new Experience();
			newExperience.setId(UUID.randomUUID().toString());
			newExperience.setOp(Experience.Operation.create);

			setModel(newExperience);
		}

		markerSettings = (LinearLayout) findViewById(R.id.markerSettings);
		markerSettingsIcon = (ImageView) findViewById(R.id.markerSettingsIcon);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("experience", ExperienceParser.createParser().toJson(getModel()));
	}

	private Intent upIntent()
	{
		final Intent intent = new Intent(this, ExperienceActivity.class);
		intent.putExtra("experience", getModel().getId());

		return intent;
	}
}
