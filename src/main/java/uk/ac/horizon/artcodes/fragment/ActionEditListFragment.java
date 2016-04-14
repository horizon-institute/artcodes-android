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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import uk.ac.horizon.artcodes.BR;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.ListAdapter;
import uk.ac.horizon.artcodes.databinding.ActionItemBinding;
import uk.ac.horizon.artcodes.databinding.ListBinding;
import uk.ac.horizon.artcodes.model.Action;

public class ActionEditListFragment extends ExperienceEditFragment
{
	class ActionAdapter extends ListAdapter<ActionItemBinding>
	{
		private List<Action> actions = Collections.emptyList();

		public ActionAdapter(Context context)
		{
			super(context);
		}

		@Override
		public int getViewCount()
		{
			return actions.size();
		}

		public void setActions(List<Action> actions)
		{
			this.actions = actions;
			adapter.notifyDataSetChanged();
		}

		@Override
		public ActionItemBinding createBinding(final ViewGroup parent, final int viewType)
		{
			return ActionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		}

		@Override
		public void bind(final int position, final ActionItemBinding binding)
		{
			final Action action = actions.get(position);
			binding.setAction(action);
			binding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					final int index = getExperience().getActions().indexOf(action);
					ActionEditDialogFragment.show(getFragmentManager(), ActionEditListFragment.this, index);
				}
			});
		}

		public void deleteAction(final int index)
		{
			final Action action = actions.get(index);
			actions.remove(index);
			adapter.notifyItemRemoved(index);
			notifyPropertyChanged(BR.showError);
			Snackbar.make(getView(), R.string.action_deleted, Snackbar.LENGTH_LONG)
					.setAction(R.string.action_delete_undo, new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							getExperience().getActions().add(index, action);
							adapter.notifyItemInserted(index);
						}
					})
					.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.apptheme_accent_light))
					.show();
		}

		public void actionUpdated(final int index)
		{
			adapter.notifyItemChanged(index);
		}

		public void addAction(Action action)
		{
			getExperience().getActions().add(action);
			int index = getExperience().getActions().size() - 1;
			adapter.notifyItemInserted(index);
			notifyPropertyChanged(BR.showError);
			ActionEditDialogFragment.show(getFragmentManager(), ActionEditListFragment.this, index);
		}
	}

	private ActionAdapter adapter;

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_action;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListBinding binding = ListBinding.inflate(inflater, container, false);
		adapter = new ActionAdapter(getActivity());
		adapter.setEmptyIcon(R.drawable.ic_warning_black_144dp);
		adapter.setEmptyMessage(getString(R.string.no_actions));
		adapter.setEmptyDetail(getString(R.string.no_actions_action));

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
				adapter.deleteAction(viewHolder.getAdapterPosition());
			}
		};
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(binding.list);

		return binding.getRoot();
	}

	@Override
	public boolean displayAddFAB()
	{
		return true;
	}

	public ActionAdapter getAdapter()
	{
		return adapter;
	}

	@Override
	public void add()
	{
		adapter.addAction(new Action());
	}

	@Override
	public void onResume()
	{
		super.onResume();
		adapter.setActions(getExperience().getActions());
	}
}
