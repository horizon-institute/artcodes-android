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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.BR;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.ListAdapter;
import uk.ac.horizon.artcodes.databinding.AvailabilityBinding;
import uk.ac.horizon.artcodes.databinding.ListBinding;
import uk.ac.horizon.artcodes.model.Availability;
import uk.ac.horizon.artcodes.ui.Bindings;

public class AvailabilityEditListFragment extends ExperienceEditFragment
{
	class AvailabilityAdapter extends ListAdapter<AvailabilityBinding>
	{
		private List<Availability> availabilities = new ArrayList<>();

		public AvailabilityAdapter(Context context)
		{
			super(context);
		}

		public void setAvailabilities(List<Availability> availabilities)
		{
			this.availabilities = availabilities;
			adapter.notifyDataSetChanged();
		}

		@Override
		public void bind(final int position, final AvailabilityBinding binding)
		{
			final Availability availability = availabilities.get(position);
			binding.setAvailability(availability);
			if (availability.getEnd() == null)
			{
				if (availability.getStart() == null)
				{
					if (availability.getName() != null)
					{
						binding.availabilityDesc.setText(getString(R.string.available_near, availability.getName()));
						binding.availabilityIcon.setImageResource(R.drawable.ic_place_32dp);
					}
					else
					{
						binding.availabilityDesc.setText(R.string.available_public);
						binding.availabilityIcon.setImageResource(R.drawable.ic_public_32dp);
					}
				}
				else
				{
					if (availability.getName() != null)
					{
						binding.availabilityDesc.setText(getString(R.string.available_from_near, Bindings.getDate(availability.getStart()), availability.getName()));
						binding.availabilityIcon.setImageResource(R.drawable.ic_place_32dp);
					}
					else
					{
						binding.availabilityDesc.setText(getString(R.string.available_from, Bindings.getDate(availability.getStart())));
						binding.availabilityIcon.setImageResource(R.drawable.ic_schedule_32dp);
					}
				}
			}
			else if (availability.getStart() == null)
			{
				if (availability.getName() != null)
				{
					binding.availabilityDesc.setText(getString(R.string.available_to_near, Bindings.getDate(availability.getEnd()), availability.getName()));
					binding.availabilityIcon.setImageResource(R.drawable.ic_place_32dp);
				}
				else
				{
					binding.availabilityDesc.setText(getString(R.string.available_to, Bindings.getDate(availability.getEnd())));
					binding.availabilityIcon.setImageResource(R.drawable.ic_schedule_32dp);
				}
			}
			else
			{
				if (availability.getName() != null)
				{
					binding.availabilityDesc.setText(getString(R.string.available_between_near, Bindings.getDate(availability.getStart(), availability.getEnd()), availability.getName()));
					binding.availabilityIcon.setImageResource(R.drawable.ic_place_32dp);
				}
				else
				{
					binding.availabilityDesc.setText(getString(R.string.available_between, Bindings.getDate(availability.getStart(), availability.getEnd())));
					binding.availabilityIcon.setImageResource(R.drawable.ic_schedule_32dp);
				}
			}

			binding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int index = getExperience().getAvailabilities().indexOf(availability);
					AvailabilityEditDialogFragment.show(getFragmentManager(), AvailabilityEditListFragment.this, index);
				}
			});
		}

		@Override
		public AvailabilityBinding createBinding(final ViewGroup parent, final int viewType)
		{
			return AvailabilityBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		}

		@Override
		public int getViewCount()
		{
			return availabilities.size();
		}

		private void addAvailability(final Availability availability)
		{
			getExperience().getAvailabilities().add(availability);
			int index = getExperience().getAvailabilities().size() - 1;
			Log.i("Added", "Added Availability at " + index);
			adapter.notifyItemInserted(index);
			notifyPropertyChanged(BR.empty);
			AvailabilityEditDialogFragment.show(getFragmentManager(), AvailabilityEditListFragment.this, index);
		}

		public void availabilityUpdated(final int index)
		{
			adapter.notifyItemChanged(index);
		}

		public void deleteAvailability(final int index)
		{
			final Availability availability = getExperience().getAvailabilities().get(index);
			getExperience().getAvailabilities().remove(index);
			adapter.notifyItemRemoved(index);
			notifyPropertyChanged(BR.empty);
			Snackbar.make(getView(), R.string.action_deleted, Snackbar.LENGTH_LONG)
					.setAction(R.string.action_delete_undo, new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							getExperience().getAvailabilities().add(index, availability);
							adapter.notifyItemInserted(index);
						}
					})
					.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.apptheme_accent_light))
					.show();
		}
	}

	private AvailabilityAdapter adapter;

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_availability;
	}

	@Override
	public boolean displayAddFAB()
	{
		return true;
	}

	@Override
	public void add()
	{
		adapter.addAvailability(new Availability());
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListBinding binding = ListBinding.inflate(inflater, container, false);
		binding.emptyIcon.setImageResource(R.drawable.ic_lock_black_144dp);
		binding.emptyText.setText(R.string.availability_private);

		adapter = new AvailabilityAdapter(getActivity());
		binding.setAdapter(adapter);

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
				adapter.deleteAvailability(viewHolder.getAdapterPosition());
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
		adapter.setAvailabilities(getExperience().getAvailabilities());
	}

	public AvailabilityAdapter getAdapter()
	{
		return adapter;
	}
}
