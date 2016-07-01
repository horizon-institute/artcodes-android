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
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.Collections;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.ExperienceActivityBase;
import uk.ac.horizon.artcodes.databinding.ActionCodeBinding;
import uk.ac.horizon.artcodes.databinding.ActionEditBinding;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.ScannerActivity;
import uk.ac.horizon.artcodes.ui.ActionEditor;
import uk.ac.horizon.artcodes.ui.MarkerFormat;
import uk.ac.horizon.artcodes.ui.SimpleTextWatcher;

public class ActionEditDialogFragment extends DialogFragment
{
	private static final int ACTION_EDIT_DIALOG = 193;
	private static final int SCAN_CODE_REQUEST = 931;
	private ActionEditBinding binding;

	static void show(FragmentManager fragmentManager, ActionEditListFragment fragment, int num)
	{
		final ActionEditDialogFragment dialog = new ActionEditDialogFragment();
		if (fragment != null)
		{
			dialog.setTargetFragment(fragment, ACTION_EDIT_DIALOG);
		}
		final Bundle args = new Bundle();
		args.putInt("action", num);
		dialog.setArguments(args);
		dialog.show(fragmentManager, "Edit Action Dialog");
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) throws NullPointerException
	{
		binding = ActionEditBinding.inflate(getActivity().getLayoutInflater());
		binding.newMarkerCode.setFilters(new InputFilter[]{new MarkerFormat()});
		binding.newMarkerCode.addTextChangedListener(new SimpleTextWatcher()
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
					binding.newMarkerCode.setText("");
					Action action = getAction();
					action.getCodes().add(value);

					ActionCodeBinding codeBinding = createCodeBinding(binding, action, action.getCodes().size() - 1);
					codeBinding.editMarkerCode.requestFocus();
				}
			}
		});

		binding.scanButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getActivity(), ScannerActivity.class);
				intent.putExtra("experience", "{\"name\":\"Scan Code\"}");
				startActivityForResult(intent, SCAN_CODE_REQUEST);
			}
		});

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(binding.getRoot());
		final Dialog dialog = builder.create();

		binding.deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (getArguments().containsKey("action"))
				{
					dialog.dismiss();
					final int index = getArguments().getInt("action");
					if (getTargetFragment() instanceof ActionEditListFragment)
					{
						((ActionEditListFragment) getTargetFragment()).getAdapter().deleteAction(index);
					}
					else
					{
						getExperience().getActions().remove(index);
					}
				}
			}
		});
		binding.doneButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (getArguments().containsKey("action"))
				{
					updateAction(); // make sure code is sorted
					if (getAction().getCodes().size()==1)
					{
						// Actions with only 1 code can not be a group or sequence!
						getAction().setMatch(Action.Match.any);
					}
					final int index = getArguments().getInt("action");
					if (getTargetFragment() instanceof ActionEditListFragment)
					{
						((ActionEditListFragment) getTargetFragment()).getAdapter().actionUpdated(index);
					}
				}
				dialog.dismiss();
			}
		});

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.match_type_descriptions, R.layout.match_type_spinner_item);
		binding.matchSpinner.setAdapter(adapter);
		binding.matchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
			{
				if (getAction()!=null)
				{
					switch (i)
					{
						case 0:
							getAction().setMatch(Action.Match.any);
							break;
						case 1:
							getAction().setMatch(Action.Match.all);
							break;
						case 2:
							getAction().setMatch(Action.Match.sequence);
							break;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView)
			{

			}
		});


		if (Feature.get(getContext(), R.bool.feature_combined_markers).isEnabled())
		{
			binding.selectLayout.setVisibility(View.GONE);
			binding.matchSpinner.setVisibility(View.VISIBLE);
		}

		return dialog;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SCAN_CODE_REQUEST && resultCode == Activity.RESULT_OK)
		{
			String code = data.getStringExtra("marker");
			Action action = getAction();
			if (code != null && (action.getMatch()==Action.Match.sequence || !action.getCodes().contains(code)))
			{
				action.getCodes().add(code);
				ActionCodeBinding codeBinding = createCodeBinding(binding, action, action.getCodes().size() - 1);
				codeBinding.editMarkerCode.requestFocus();
			}
		}
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
		updateMatchType(binding, action);
	}

	private void updateCodes(final ActionEditBinding binding, final Action action)
	{
		binding.markerCodeList.removeAllViews();
		if (action!=null && action.getMatch()!=Action.Match.sequence)
		{
			// only sort codes if not a sequence!
			Collections.sort(action.getCodes());
		}
		for (int index = 0; index < action.getCodes().size(); index++)
		{
			createCodeBinding(binding, action, index);
		}
	}

	private void updateMatchType(final ActionEditBinding binding, final Action action)
	{
		if (action!=null)
		{
			binding.matchSpinner.setSelection(action.getMatch()==Action.Match.any?0:(action.getMatch()==Action.Match.all?1:2));
		}
	}

	private ActionCodeBinding createCodeBinding(final ActionEditBinding binding, final Action action, final int codeIndex)
	{
		final String code = action.getCodes().get(codeIndex);
		final ActionCodeBinding codeBinding = ActionCodeBinding.inflate(getActivity().getLayoutInflater(), binding.markerCodeList, false);
		codeBinding.editMarkerCode.setText(code);
		codeBinding.editMarkerCode.setFilters(new InputFilter[]{new MarkerFormat()});
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
					updateCodes(binding, action);
					binding.newMarkerCode.requestFocus();
				}
				else
				{
					action.getCodes().set(codeIndex, value);
				}
			}
		});
		binding.markerCodeList.addView(codeBinding.getRoot());
		return codeBinding;
	}
}