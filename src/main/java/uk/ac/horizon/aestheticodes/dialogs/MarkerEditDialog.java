/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.activities.ExperienceEditActivity;
import uk.ac.horizon.aestheticodes.bindings.ViewBindings;
import uk.ac.horizon.aestheticodes.model.MarkerAction;

public class MarkerEditDialog extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// TODO builder.setMessage(getDescription());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		final String code = getArguments().getString("code");

		ExperienceEditActivity activity = (ExperienceEditActivity)getActivity();
		final MarkerAction marker = activity.getExperience().getMarkers().get(code);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setTitle(getResources().getString(R.string.dialog_marker_title_prefix) + " " + marker.getCode());

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.marker_edit, null);

		final ViewBindings viewBindings = new ViewBindings(getActivity(), marker)
		{
			@Override
			protected void setValid(boolean valid)
			{
				//dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(valid);
			}
		};

		View markerCode = viewBindings.bind(R.id.markerCode, "code");
		if (marker.getCode() != null)
		{
			markerCode.setVisibility(View.GONE);
		}

		viewBindings.bind(R.id.markerAction, "action");

		builder.setView(view);
		builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				if (viewBindings.isValid())
				{
					// TODO
				}
//					if (Patterns.WEB_URL.matcher("http://" + actionView.getText().toString()).matches())
//					{
//						marker.setAction("http://" + actionView.getText().toString());
//					}
//					else
//					{
//						marker.setAction(actionView.getText().toString());
//					}
//					// TODO experience.setChanged(true);
//					// TODO settingsActivity.saveChanges();
			}
		});

		builder.setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				// User cancelled the dialog
			}
		});

		builder.setNeutralButton(R.string.dialog_action_delete, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
				confirmBuilder.setTitle(getResources().getString(R.string.confirmDeleteDialogTitlePrefix) + " " + marker.getCode() + "?");
				confirmBuilder.setMessage(R.string.confirmDeleteDialogMessage);
				confirmBuilder.setPositiveButton(R.string.confirmDeleteDialogPositive, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						// delete the marker:
						//experience.deleteMarker(code);
						// TODO settingsActivity.saveChanges();
					}
				});
				confirmBuilder.setNegativeButton(R.string.confirmDeleteDialogNegative, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						// nothing
					}
				});

				confirmBuilder.create().show();
			}
		});

		// Create the AlertDialog object and return it
		final AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

//			actionView.addTextChangedListener(new TextWatcher()
//			{
//				final Runnable userStoppedTyping = new Runnable()
//				{
//					@Override
//					public void run()
//					{
//						if (Patterns.WEB_URL.matcher(actionView.getText().toString()).matches())
//						{
//							dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
//							actionView.setError(null);
//						}
//						else
//						{
//							dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
//							actionView.setError("This url is not valid");
//						}
//					}
//				};
//
//				@Override
//				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
//				{
//
//				}
//
//				@Override
//				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
//				{
//					actionView.setError(null);
//					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(Patterns.WEB_URL.matcher(charSequence).matches());
//					handler.removeCallbacksAndMessages(null);
//					handler.postDelayed(userStoppedTyping, 2000);
//				}
//
//				@Override
//				public void afterTextChanged(Editable editable)
//				{
//				}
//			});

		return dialog;
	}
}