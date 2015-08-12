package uk.ac.horizon.artcodes.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.AvailabilityEditBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceEditAvailabilitiesBinding;
import uk.ac.horizon.artcodes.model.Availability;
import uk.ac.horizon.artcodes.ui.Bindings;

import java.util.Calendar;
import java.util.List;

public class ExperienceEditAvailabilityFragment extends ExperienceEditFragment
{
	private static final int PLACE_PICKER_REQUEST = 119;

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_availability;
	}

	private class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.ViewHolder>
	{
		public class ViewHolder extends RecyclerView.ViewHolder
		{
			private AvailabilityEditBinding binding;

			public ViewHolder(AvailabilityEditBinding binding)
			{
				super(binding.getRoot());
				this.binding = binding;
			}
		}

		private List<Availability> availabilities;

		public AvailabilityAdapter(List<Availability> availabilities)
		{
			this.availabilities = availabilities;
		}

		@Override
		public int getItemCount()
		{
			return availabilities.size();
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position)
		{
			final Availability availability = availabilities.get(position);
			holder.binding.setAvailability(availability);
			if (availability.getEnd() == null)
			{
				if (availability.getStart() == null)
				{
					holder.binding.availabilityDesc.setText("Always Available");
				}
				else
				{
					holder.binding.availabilityDesc.setText("Available from " + Bindings.getDate(availability.getStart()));
				}
			}
			else
			{
				if (availability.getStart() == null)
				{
					holder.binding.availabilityDesc.setText("Available until " + Bindings.getDate(availability.getEnd()));
				}
				else
				{

					holder.binding.availabilityDesc.setText("Available " + Bindings.getDate(availability.getStart(), availability.getEnd()));
				}
			}

			holder.binding.availabilityStart.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					selectDate(availability.getStart(), new DatePickerDialog.OnDateSetListener()
					{
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
						{
							final Calendar calendar = Calendar.getInstance();
							calendar.set(year, monthOfYear, dayOfMonth);
							availability.setStart(calendar.getTimeInMillis());
						}
					});
				}
			});
			holder.binding.availabilityEnd.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					selectDate(availability.getEnd(), new DatePickerDialog.OnDateSetListener()
					{
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
						{
							final Calendar calendar = Calendar.getInstance();
							calendar.set(year, monthOfYear, dayOfMonth);
							availability.setEnd(calendar.getTimeInMillis());
						}
					});
				}
			});
			holder.binding.toggleDates.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					holder.binding.toggleDates.setVisibility(View.GONE);
					holder.binding.dateExpand.setVisibility(View.VISIBLE);
				}
			});
			holder.binding.availabilityLocation.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
						Intent intent = builder.build(getActivity());
						intent.putExtra("availIndex", getExperience().getAvailabilities().indexOf(availability));
						startActivityForResult(intent, PLACE_PICKER_REQUEST);
					}
					catch (Exception e)
					{
						GoogleAnalytics.trackException(e);
					}
				}
			});
			holder.binding.delete.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					delete(availability);
				}
			});
		}

		@Override
		public AvailabilityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return new ViewHolder(AvailabilityEditBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}
	}

	private ExperienceEditAvailabilitiesBinding binding;

	private void delete(final Availability availability)
	{
		final int index = getExperience().getAvailabilities().indexOf(availability);
		getExperience().getAvailabilities().remove(availability);
		binding.list.getAdapter().notifyItemRemoved(index);
		updateAvailabilities();
		Snackbar.make(binding.getRoot(), R.string.action_deleted, Snackbar.LENGTH_LONG)
				.setAction(R.string.action_delete_undo, new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						getExperience().getAvailabilities().add(index, availability);
						binding.list.getAdapter().notifyItemInserted(index);
						updateAvailabilities();
					}
				}).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PLACE_PICKER_REQUEST)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				final Place place = PlacePicker.getPlace(data, getActivity());
				Log.i("", place.getName().toString());
				Log.i("", place.getAddress().toString());
				final int index = data.getIntExtra("availIndex", 0);
				if (index >= 0)
				{
					final Availability availability = getExperience().getAvailabilities().get(index);
					availability.setName(place.getName().toString());
					availability.setAddress(place.getAddress().toString());
					availability.setLat(place.getLatLng().latitude);
					availability.setLon(place.getLatLng().longitude);
				}
			}
		}
	}

	private void updateAvailabilities()
	{
		if (getExperience().getAvailabilities().isEmpty())
		{
			binding.list.setVisibility(View.GONE);
			binding.emptyView.setVisibility(View.VISIBLE);
		}
		else
		{
			binding.list.setVisibility(View.VISIBLE);
			binding.emptyView.setVisibility(View.GONE);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceEditAvailabilitiesBinding.inflate(inflater, container, false);
		binding.add.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.i("", "Adding new availability");
				if(getExperience().getAvailabilities().add(new Availability()))
				{
					updateAvailabilities();
					binding.list.getAdapter().notifyItemInserted(getExperience().getAvailabilities().size()-1);
				}
			}
		});

		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));

		ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
		{
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1)
			{
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
			{
				int position =viewHolder.getAdapterPosition();
				delete(getExperience().getAvailabilities().get(position));
			}
		};
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(binding.list);

		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		binding.list.setAdapter(new AvailabilityAdapter(getExperience().getAvailabilities()));
		updateAvailabilities();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isInLayout())
		{
			if (isVisibleToUser)
			{
				binding.add.show();
			}
			else
			{
				binding.add.hide();
			}
		}
	}

	private void selectDate(Long timestamp, DatePickerDialog.OnDateSetListener listener)
	{
		final Calendar calendar = Calendar.getInstance();
		if (timestamp != null)
		{
			calendar.setTimeInMillis(timestamp);
		}
		else
		{
			calendar.setTimeInMillis(System.currentTimeMillis());
		}
		int mYear = calendar.get(Calendar.YEAR);
		int mMonth = calendar.get(Calendar.MONTH);
		int mDay = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dialog = new DatePickerDialog(getActivity(), listener, mYear, mMonth, mDay);
		dialog.show();
	}
}
