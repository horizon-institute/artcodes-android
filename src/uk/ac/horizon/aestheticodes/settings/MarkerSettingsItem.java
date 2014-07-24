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
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.model.MarkerSettings;

public class MarkerSettingsItem extends SettingsItem
{
	private static final MarkerSettings settings = MarkerSettings.getSettings();

	public static class EditMarkerDialogFragment extends DialogFragment
	{
		final Handler handler = new Handler();

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// TODO builder.setMessage(getDescription());

			LayoutInflater inflater = getActivity().getLayoutInflater();

			String code = getArguments().getString("code");
			final MarkerAction action = settings.getMarkers().get(code);
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			builder.setTitle("Marker " + action.getCode());

			View view = inflater.inflate(R.layout.dialog_edit_marker, null);

			final EditText urlView = (EditText) view.findViewById(R.id.markerURL);
			urlView.setText(getDetail(action));

			builder.setView(view);
			builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
                    if(Patterns.WEB_URL.matcher("http://" + urlView.getText().toString()).matches())
                    {
                        action.setAction("http://" + urlView.getText().toString());
                    }
                    else
                    {
                        action.setAction(urlView.getText().toString());
                    }
					settings.setChanged(true);
					((SettingsActivity)getActivity()).refresh();
				}
			});
			builder.setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// User cancelled the dialog
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
						if(Patterns.WEB_URL.matcher(urlView.getText().toString()).matches())
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

	private MarkerAction action;

	public MarkerSettingsItem(SettingsActivity activity, MarkerAction action)
	{
		super(activity);
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
		if(action.isEditable())
		{
			return Type.two_line;
		}
		return Type.two_line_disabled;
	}

	@Override
	public void selected()
	{
		if(action.isEditable())
		{
			final EditMarkerDialogFragment dialogFragment = new EditMarkerDialogFragment();
			final Bundle bundle = new Bundle();
			bundle.putString("code", action.getCode());
			dialogFragment.setArguments(bundle);
			dialogFragment.show(activity.getSupportFragmentManager(), "missiles");
		}
	}

	private static String getDetail(final MarkerAction action)
	{
		if(action.getAction() != null && action.getAction().startsWith("http://"))
		{
			return action.getAction().substring("http://".length());
		}
		return action.getAction();
	}

	@Override
	public String getDetail()
	{
		return getDetail(action);
	}
}
