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

package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ActionItemBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceEditActionsBinding;
import uk.ac.horizon.artcodes.dialog.ActionEditDialog;
import uk.ac.horizon.artcodes.model.Action;

public class ExperienceEditActionFragment extends ExperienceEditFragment
{
	private class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder>
	{
		public class ViewHolder extends RecyclerView.ViewHolder
		{
			private ActionItemBinding binding;

			public ViewHolder(ActionItemBinding binding)
			{
				super(binding.getRoot());
				this.binding = binding;
			}
		}

		private List<Action> actions;

		public ActionAdapter(List<Action> actions)
		{
			this.actions = actions;
		}

		@Override
		public int getItemCount()
		{
			return actions.size();
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position)
		{
			final Action action = actions.get(position);
			holder.binding.setAction(action);
			holder.binding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int index = getExperience().getActions().indexOf(action);
					ActionEditDialog dialog = ActionEditDialog.create(index);
					dialog.show(getFragmentManager(), "");
				}
			});
		}

		@Override
		public ActionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return new ViewHolder(ActionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}
	}

	private ExperienceEditActionsBinding binding;

	@Override
	public int getTitleResource()
	{
		return R.string.fragment_action;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceEditActionsBinding.inflate(inflater, container, false);
		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
		return binding.getRoot();
	}

	@Override
	public boolean displayAddFAB()
	{
		return true;
	}

	@Override
	public void add()
	{
		Action action = new Action();
		getExperience().getActions().add(action);
		binding.list.getAdapter().notifyItemInserted(getExperience().getActions().size());
		ActionEditDialog dialog = ActionEditDialog.create(getExperience().getActions().indexOf(action));
		dialog.show(getFragmentManager(), "");
	}

	@Override
	public void update()
	{
		binding.list.getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		binding.list.setAdapter(new ActionAdapter(getExperience().getActions()));
	}
}
