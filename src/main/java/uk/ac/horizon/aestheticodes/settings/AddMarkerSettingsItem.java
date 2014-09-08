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
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.MarkerAction;

public class AddMarkerSettingsItem extends SettingsItem
{
	private final Experience experience;

	private static class MarkerCodeInputFilter implements InputFilter
	{
		private final Experience experience;

		public MarkerCodeInputFilter(Experience experience)
		{
			this.experience = experience;
		}

		@Override
		public CharSequence filter(CharSequence source, int sourceStart, int sourceEnd, Spanned destination, int destinationStart, int destinationEnd)
		{
			String sourceValue = source.subSequence(sourceStart, sourceEnd).toString();
			if (sourceValue.equals(" "))
			{
				sourceValue = ":";
			}

			String result = destination.subSequence(0, destinationStart).toString() + sourceValue +
					destination.subSequence(destinationEnd, destination.length()).toString();
			if(result.equals(""))
			{
				return sourceValue;
			}
			boolean resultValid = experience.isValidMarker(result, true);

			if (!resultValid && !sourceValue.startsWith(":"))
			{
				sourceValue = ":" + sourceValue;
				resultValid = experience.isValidMarker(destination.subSequence(0, destinationStart).toString() + sourceValue +
						destination.subSequence(destinationEnd, destination.length()).toString(), true);
			}

			if (resultValid && !source.subSequence(sourceStart, sourceEnd).toString().equals(sourceValue))
			{
				return sourceValue;
			}

			if (resultValid)
			{
				return null;
			}
			return "";
		}
	}

	public static class AddMarkerDialogFragment extends DialogFragment
	{
		final Handler handler = new Handler();

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// TODO builder.setMessage(getDescription());

			LayoutInflater inflater = getActivity().getLayoutInflater();
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			builder.setTitle("Add New Marker");

			final String experienceID = getArguments().getString("experience");

			ExperienceManager experienceManager = new ExperienceManager(getActivity(),null);
			final Experience experience = experienceManager.get(experienceID);

			View view = inflater.inflate(R.layout.dialog_add_marker, null);

			final EditText markerCode = (EditText) view.findViewById(R.id.markerCode);
			markerCode.setFilters(new InputFilter[] { new MarkerCodeInputFilter(experience) });
            final EditText urlView = (EditText) view.findViewById(R.id.markerURL);
            if (this.presetCode != null)
            {
                markerCode.setText(this.presetCode);
                markerCode.setEnabled(false);
                urlView.requestFocus();
            }

			builder.setView(view);
			builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					MarkerAction action = new MarkerAction();
					action.setCode(markerCode.getText().toString());
                    if(Patterns.WEB_URL.matcher("http://" + urlView.getText().toString()).matches())
                    {
                        action.setAction("http://" + urlView.getText().toString());
                    }
                    else
                    {
                        action.setAction(urlView.getText().toString());
                    }
					experience.setChanged(true);
					experience.getMarkers().put(action.getCode(), action);
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

			final TextWatcher watcher = new TextWatcher()
			{
				Runnable userStoppedTyping = new Runnable()
				{
					@Override
					public void run()
					{
						dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValid());

						if(Patterns.WEB_URL.matcher(urlView.getText().toString()).matches())
						{
							urlView.setError(null);
						}
						else
						{
							urlView.setError("This url is not valid");
						}

						if(experience.isValidMarker(markerCode.getText().toString(), false))
						{
							markerCode.setError(null);
						}
						else
						{
							markerCode.setError("This code is not valid");
						}

						if(!experience.getMarkers().containsKey(markerCode.getText().toString()))
						{
							markerCode.setError(null);
						}
						else
						{
							markerCode.setError("The code already exists");
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
					markerCode.setError(null);
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValid());
					handler.removeCallbacksAndMessages(null);
					handler.postDelayed(userStoppedTyping, 2000);
				}

				@Override
				public void afterTextChanged(Editable editable)
				{
				}

				private boolean isValid()
				{
					return Patterns.WEB_URL.matcher(urlView.getText().toString()).matches() && experience.isValidMarker(markerCode.getText().toString(), false) && !experience.getMarkers().containsKey(markerCode.getText().toString());
				}
			};

			markerCode.addTextChangedListener(watcher);
			urlView.addTextChangedListener(watcher);
            // disable the positive open_button when shown as the URL field will be empty
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (positiveButton != null)
                    {
                        positiveButton.setEnabled(false);
                    }
                }
            });

			return dialog;
		}

        private String presetCode = null;
        public void presetCode(String code) {
            this.presetCode = code;
        }
	}


	public AddMarkerSettingsItem(SettingsActivity activity, Experience experience, String title)
	{
		super(activity, title);
		this.experience = experience;
	}

	@Override
	public int getIcon()
	{
		return R.drawable.ic_action_new;
	}

	@Override
	public void selected()
	{
		final AddMarkerDialogFragment dialogFragment = new AddMarkerDialogFragment();
		final Bundle bundle = new Bundle();
		bundle.putString("experience", experience.getId());
		dialogFragment.setArguments(bundle);
		dialogFragment.show(activity.getSupportFragmentManager(), "missiles");
	}

	@Override
	public Type getType()
	{
		return Type.single_line;
	}
}
