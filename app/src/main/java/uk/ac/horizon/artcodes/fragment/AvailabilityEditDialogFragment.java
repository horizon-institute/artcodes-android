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

package uk.ac.horizon.artcodes.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.widget.DatePicker;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.Calendar;

import uk.ac.horizon.artcodes.Analytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.ExperienceActivityBase;
import uk.ac.horizon.artcodes.databinding.AvailabilityEditBinding;
import uk.ac.horizon.artcodes.model.Availability;
import uk.ac.horizon.artcodes.model.Experience;

public class AvailabilityEditDialogFragment extends DialogFragment {
	private interface DateListener {
		void dateSelected(Long timestamp);
	}

	private static final int PLACE_PICKER_REQUEST = 119;
	private AvailabilityEditBinding binding;

	static void show(FragmentManager fragmentManager, AvailabilityEditListFragment fragment, int num) {
		final AvailabilityEditDialogFragment dialog = new AvailabilityEditDialogFragment();
		final Bundle args = new Bundle();
		dialog.setTargetFragment(fragment, 1);
		args.putInt("availability", num);
		dialog.setArguments(args);
		dialog.show(fragmentManager, "Availability Edit Dialog");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		binding.availabilityLocationProgress.setVisibility(View.INVISIBLE);
		binding.availabilityLocation.setVisibility(View.VISIBLE);
		if (requestCode == PLACE_PICKER_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				final Place place = PlacePicker.getPlace(getActivity(), data);
				final int index = data.getIntExtra("availIndex", 0);
				if (index >= 0) {
					final Availability availability = getExperience().getAvailabilities().get(index);
					availability.setLocation(place.getLatLng().latitude, place.getLatLng().longitude, place.getName().toString(), place.getAddress().toString());
				}
			}
		}
	}


	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) throws NullPointerException {
		binding = AvailabilityEditBinding.inflate(getActivity().getLayoutInflater());
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(binding.getRoot());

		final Dialog dialog = builder.create();

		binding.deleteButton.setOnClickListener(v -> {
			if (getArguments().containsKey("availability")) {
				dialog.dismiss();
				final int index = getArguments().getInt("availability");
				if (getTargetFragment() instanceof AvailabilityEditListFragment) {
					((AvailabilityEditListFragment) getTargetFragment()).getAdapter().deleteAvailability(index);
				} else {
					getExperience().getAvailabilities().remove(index);
				}
			}
		});

		binding.doneButton.setOnClickListener(v -> {
			dialog.dismiss();
			if (getArguments().containsKey("availability")) {
				final int index = getArguments().getInt("availability");
				if (getTargetFragment() instanceof AvailabilityEditListFragment) {
					((AvailabilityEditListFragment) getTargetFragment()).getAdapter().availabilityUpdated(index);
				}
			}
		});

		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateAvailability();
	}

	private void selectDate(Long timestamp, final DateListener listener) {
		final Calendar calendar = Calendar.getInstance();
		if (timestamp != null) {
			calendar.setTimeInMillis(timestamp);
		} else {
			calendar.setTimeInMillis(System.currentTimeMillis());
		}
		int mYear = calendar.get(Calendar.YEAR);
		int mMonth = calendar.get(Calendar.MONTH);
		int mDay = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dialog = new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
			final Calendar calendar1 = Calendar.getInstance();
			calendar1.set(year, monthOfYear, dayOfMonth);
			listener.dateSelected(calendar1.getTimeInMillis());
		}, mYear, mMonth, mDay);
		dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.clear), (dialog1, which) -> {
			dialog1.dismiss();
			listener.dateSelected(null);
		});
		dialog.show();
	}

	private Experience getExperience() {
		if (getActivity() instanceof ExperienceActivityBase) {
			return ((ExperienceActivityBase) getActivity()).getExperience();
		}
		return null;
	}

	@NonNull
	private Availability getAvailability() throws NullPointerException {
		Experience experience = getExperience();
		if (experience != null) {
			if (getArguments().containsKey("availability")) {
				int index = getArguments().getInt("availability");
				return experience.getAvailabilities().get(index);
			}
		}
		throw new NullPointerException("Couldn't get action");
	}

	private void updateAvailability() {
		final Availability availability = getAvailability();

		binding.setAvailability(availability);
		binding.availabilityStart.setOnClickListener(v -> selectDate(availability.getStart(), timestamp -> availability.setStart(timestamp)));
		binding.availabilityEnd.setOnClickListener(v -> selectDate(availability.getEnd(), timestamp -> availability.setEnd(timestamp)));
		binding.availabilityLocation.setOnClickListener(v -> {
			try {
				binding.availabilityLocationProgress.setVisibility(View.VISIBLE);
				binding.availabilityLocation.setVisibility(View.INVISIBLE);
				PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
				Intent intent = builder.build(getActivity());
				intent.putExtra("availIndex", getExperience().getAvailabilities().indexOf(availability));
				startActivityForResult(intent, PLACE_PICKER_REQUEST);
			} catch (Exception e) {
				Analytics.trackException(e);
				binding.availabilityLocationProgress.setVisibility(View.INVISIBLE);
				binding.availabilityLocation.setVisibility(View.VISIBLE);
			}
		});
		binding.availabilityLocationClear.setOnClickListener(v -> {
			availability.clearLocation();
		});
		//binding.setActionEditor(new ActionEditor(action));
	}
}