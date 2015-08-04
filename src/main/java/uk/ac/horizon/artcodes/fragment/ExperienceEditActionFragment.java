package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.databinding.ActionEditBinding;
import uk.ac.horizon.artcodes.databinding.ActionEditCodeBinding;
import uk.ac.horizon.artcodes.databinding.ExperienceEditActionsBinding;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.ui.ActionEditor;
import uk.ac.horizon.artcodes.ui.MarkerFormat;
import uk.ac.horizon.artcodes.ui.SimpleTextWatcher;

import java.util.List;

public class ExperienceEditActionFragment extends ExperienceEditFragment
{
	private class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder>
	{
		public class ViewHolder extends RecyclerView.ViewHolder
		{
			private ActionEditBinding binding;

			public ViewHolder(ActionEditBinding binding)
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
			holder.binding.setActionEditor(new ActionEditor(action));
			holder.binding.editToggle.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (selected != null)
					{
						selected.overview.setVisibility(View.VISIBLE);
						selected.editview.setVisibility(View.GONE);
						selected.expandImage.setImageResource(R.drawable.ic_expand_more_24dp);
					}

					if (selected != holder.binding)
					{
						holder.binding.overview.setVisibility(View.GONE);
						holder.binding.editview.setVisibility(View.VISIBLE);
						holder.binding.expandImage.setImageResource(R.drawable.ic_expand_less_24dp);
						holder.binding.actionName.requestFocus();
						selected = holder.binding;
					}
					else
					{
						selected = null;
					}
				}
			});
			holder.binding.newMarkerCode.addTextChangedListener(new SimpleTextWatcher()
			{
				@Override
				public String getText()
				{
					return null;
				}

				@Override
				public void onTextChanged(String value)
				{
					if (!value.isEmpty())
					{
						holder.binding.newMarkerCode.setText("");
						action.getCodes().add(value);

						ActionEditCodeBinding codeBinding = createCodeEditor(holder.binding, action.getCodes().size() - 1);
						codeBinding.editMarkerCode.requestFocus();
					}
				}
			});
			holder.binding.actionDelete.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					final int index = getExperience().getActions().indexOf(action);
					getExperience().getActions().remove(action);
					notifyDataSetChanged();
					Snackbar.make(binding.getRoot(), R.string.action_deleted, Snackbar.LENGTH_LONG)
							.setAction(R.string.action_delete_undo, new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									getExperience().getActions().add(index, action);
									notifyDataSetChanged();
								}
							}).show();
				}
			});

			updateCodes(holder.binding);
		}

		@Override
		public ActionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return new ViewHolder(ActionEditBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}
	}

	private ExperienceEditActionsBinding binding;
	private ActionEditBinding selected;

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
		binding.add.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Action action = new Action();
				action.setName("New Action");
				getExperience().getActions().add(action);
				binding.list.getAdapter().notifyDataSetChanged();
			}
		});

		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		binding.list.setAdapter(new ActionAdapter(getExperience().getActions()));
	}

	private ActionEditCodeBinding createCodeEditor(final ActionEditBinding actionBinding, final int index)
	{
		String code = actionBinding.getAction().getCodes().get(index);
		final ActionEditCodeBinding codeBinding = ActionEditCodeBinding.inflate(getActivity().getLayoutInflater(), actionBinding.markerCodes, false);
		codeBinding.editMarkerCode.setText(code);
		codeBinding.editMarkerCode.setFilters(new InputFilter[]{new MarkerFormat(binding.getExperience(), code)});
		codeBinding.editMarkerCode.addTextChangedListener(new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return null;
			}

			@Override
			public void onTextChanged(String value)
			{
				if (value.isEmpty())
				{
					actionBinding.newMarkerCode.requestFocus();
					actionBinding.getAction().getCodes().remove(index);
					updateCodes(actionBinding);
				}
				else
				{
					actionBinding.getAction().getCodes().set(index, value);
				}
			}
		});
		actionBinding.markerCodes.addView(codeBinding.getRoot());

		return codeBinding;
	}

	private void updateCodes(ActionEditBinding actionBinding)
	{
		actionBinding.markerCodes.removeAllViews();
		for (int index = 0; index < actionBinding.getAction().getCodes().size(); index++)
		{
			createCodeEditor(actionBinding, index);
		}
	}
}
