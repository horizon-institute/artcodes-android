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

package uk.ac.horizon.artcodes.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.View;

import uk.ac.horizon.artcodes.activity.ExperienceActivityBase;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.databinding.ActionCodeBinding;
import uk.ac.horizon.artcodes.databinding.ActionEditBinding;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.ui.ActionEditor;
import uk.ac.horizon.artcodes.ui.MarkerFormat;
import uk.ac.horizon.artcodes.ui.SimpleTextWatcher;

public class ActionEditDialog extends DialogFragment
{
	private ActionEditBinding binding;

	public static ActionEditDialog create(int num)
	{
		final ActionEditDialog dialog = new ActionEditDialog();
		final Bundle args = new Bundle();
		args.putInt("action", num);
		dialog.setArguments(args);

		return dialog;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) throws NullPointerException
	{
		binding = ActionEditBinding.inflate(getActivity().getLayoutInflater());
		binding.editMarkerCode.setFilters(new InputFilter[]{new MarkerFormat(getExperience(), null)});
		binding.editMarkerCode.addTextChangedListener(new SimpleTextWatcher()
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
					binding.editMarkerCode.setText("");
					Action action = getAction();
					action.getCodes().add(value);

					ActionCodeBinding codeBinding = createCodeBinding(binding, action, action.getCodes().size() - 1);
					codeBinding.editMarkerCode.requestFocus();
				}
			}
		});

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(binding.getRoot());
		final Dialog dialog = builder.create();

		binding.deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (getArguments().containsKey("action") && getActivity() instanceof ExperienceEditActivity)
				{
					dialog.dismiss();
					ExperienceEditActivity activity = (ExperienceEditActivity) getActivity();
					activity.deleteAction(getArguments().getInt("action"));
				}
			}
		});

		return dialog;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateAction();
	}

	private Experience getExperience()
	{
		if (getActivity() instanceof ExperienceActivityBase)
		{
			return ((ExperienceActivityBase) getActivity()).getExperience();
		}
		return null;
	}

	@NonNull
	private Action getAction() throws NullPointerException
	{
		Experience experience = getExperience();
		if (experience != null)
		{
			if (getArguments().containsKey("action"))
			{
				int index = getArguments().getInt("action");
				return experience.getActions().get(index);
			}
		}
		throw new NullPointerException("Couldn't get action");
	}

	private void updateAction()
	{
		final Action action = getAction();

		binding.setAction(action);
		binding.setActionEditor(new ActionEditor(action));

		updateCodes(binding, action);
	}

	private void updateCodes(final ActionEditBinding binding, final Action action)
	{
		binding.markerCodes.removeAllViews();
		for (int index = 0; index < action.getCodes().size(); index++)
		{
			createCodeBinding(binding, action, index);
		}
	}

	private ActionCodeBinding createCodeBinding(final ActionEditBinding binding, final Action action, final int codeIndex)
	{
		final String code = action.getCodes().get(codeIndex);
		final ActionCodeBinding codeBinding = ActionCodeBinding.inflate(getActivity().getLayoutInflater(), binding.markerCodes, false);
		codeBinding.editMarkerCode.setText(code);
		codeBinding.editMarkerCode.setFilters(new InputFilter[]{new MarkerFormat(getExperience(), code)});
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
					action.getCodes().remove(codeIndex);
					binding.markerCodes.removeView(codeBinding.getRoot());
					binding.editMarkerCode.requestFocus();
				}
				else
				{
					action.getCodes().set(codeIndex, value);
				}
			}
		});
		binding.markerCodes.addView(codeBinding.getRoot());
		return codeBinding;
	}
}