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

package uk.ac.horizon.aestheticodes.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.MarkerAction;

public class MarkerSettingsItem extends SettingsItem
{
	public static class EditMarkerDialogFragment extends DialogFragment
	{
		private final Handler handler = new Handler();

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// TODO builder.setMessage(getDescription());

			LayoutInflater inflater = getActivity().getLayoutInflater();
			final SettingsActivity settingsActivity = (SettingsActivity) getActivity();

			final String code = getArguments().getString("code");
			final String experienceID = getArguments().getString("experience");

			ExperienceManager experienceManager = ExperienceManager.get(getActivity());
			final Experience experience = experienceManager.get(experienceID);
			final MarkerAction action = experience.getMarkers().get(code);
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			builder.setTitle(getResources().getString(R.string.dialog_marker_title_prefix) + " " + action.getCode());

			View view = inflater.inflate(R.layout.dialog_edit_marker, null);

			final EditText urlView = (EditText) view.findViewById(R.id.markerURL);
			urlView.setText(getDetail(action));

			builder.setView(view);
			builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					if (Patterns.WEB_URL.matcher("http://" + urlView.getText().toString()).matches())
					{
						action.setAction("http://" + urlView.getText().toString());
					}
					else
					{
						action.setAction(urlView.getText().toString());
					}
					experience.setChanged(true);
					settingsActivity.saveChanges();
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
					confirmBuilder.setTitle(getResources().getString(R.string.confirmDeleteDialogTitlePrefix) + " " + code + "?");
					confirmBuilder.setMessage(R.string.confirmDeleteDialogMessage);
					confirmBuilder.setPositiveButton(R.string.confirmDeleteDialogPositive, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							// delete the marker:
							experience.deleteMarker(code);
							settingsActivity.saveChanges();
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

			urlView.addTextChangedListener(new TextWatcher()
			{
				Runnable userStoppedTyping = new Runnable()
				{
					@Override
					public void run()
					{
						if (Patterns.WEB_URL.matcher(urlView.getText().toString()).matches())
						{
							dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
							urlView.setError(null);
						}
						else
						{
							dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							urlView.setError("This url is not valid");
						}
					}
				};

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
				{

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
				{
					urlView.setError(null);
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(Patterns.WEB_URL.matcher(charSequence).matches());
					handler.removeCallbacksAndMessages(null);
					handler.postDelayed(userStoppedTyping, 2000);
				}

				@Override
				public void afterTextChanged(Editable editable)
				{
				}
			});

			return dialog;
		}
	}

	private static String getDetail(final MarkerAction action)
	{
		if (action.getAction() != null && action.getAction().startsWith("http://"))
		{
			return action.getAction().substring("http://".length());
		}
		return action.getAction();
	}

	private final Experience experience;
	private final MarkerAction action;

	public MarkerSettingsItem(SettingsActivity activity, Experience experience, MarkerAction action)
	{
		super(activity);
		this.experience = experience;
		this.action = action;
	}

	@Override
	public int getIcon()
	{
		return R.drawable.ic_action_labels;
	}

	@Override
	public String getTitle()
	{
		return "Marker " + action.getCode();
	}

	@Override
	public Type getType()
	{
		if (action.isEditable())
		{
			return Type.two_line;
		}
		return Type.two_line_disabled;
	}

	@Override
	public void selected()
	{
		if (action.isEditable())
		{
			final EditMarkerDialogFragment dialogFragment = new EditMarkerDialogFragment();
			final Bundle bundle = new Bundle();
			bundle.putString("experience", experience.getId());
			bundle.putString("code", action.getCode());
			dialogFragment.setArguments(bundle);
			dialogFragment.show(activity.getSupportFragmentManager(), "missiles");
		}
	}

	@Override
	public String getDetail()
	{
		return getDetail(action);
	}
}
