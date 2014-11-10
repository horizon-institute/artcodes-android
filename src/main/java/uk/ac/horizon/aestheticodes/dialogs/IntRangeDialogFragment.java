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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import uk.ac.horizon.aestheticodes.R;

public class IntRangeDialogFragment extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setTitle("Set " + getArguments().getString("title"));
		String description = getArguments().getString("description");
		if (description != null)
		{
			builder.setMessage(description);
		}

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.settings_slider, null);

		final TextView sliderValue = (TextView) view.findViewById(R.id.sliderValue);
		final SeekBar slider = (SeekBar) view.findViewById(R.id.slider);

		final String propertyName = getArguments().getString("propertyName");
		final Integer minValue = getInt(getArguments(), "min");
		final Integer maxValue = getInt(getArguments(), "max");
		final Integer value = getInt(getArguments(), "value");
		final Integer off = getInt(getArguments(), "off");

		sliderValue.setText(getDetail(getActivity(), propertyName, value, off));

		final int size = maxValue - minValue;
		slider.setMax(size);
		slider.setProgress(value - minValue);
		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b)
			{
				final int value = seekBar.getProgress() + minValue;
				sliderValue.setText(getDetail(getActivity(), propertyName, value, off));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});

		builder.setView(view);
		builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				final int value = slider.getProgress() + minValue;
				//((SettingsActivity)getActivity()).setProperty(propertyName, value);
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
		return builder.create();
	}

	private static String getDetail(Context context, String propertyName, Integer minValue, Integer maxValue)
	{
		if(minValue.intValue() == maxValue.intValue())
		{
			int resource = context.getResources().getIdentifier(propertyName + "_value", "plurals", context.getPackageName());
			if(resource == 0)
			{
				resource = context.getResources().getIdentifier(propertyName + "_value", "string", context.getPackageName());
				if(resource == 0)
				{
					return Integer.toString(minValue);
				}
				else
				{
					return context.getString(resource, minValue);
				}

			}
			else
			{
				return context.getResources().getQuantityString(resource, minValue, minValue);
			}
		}
		else
		{
			String value = minValue + "-" + maxValue;
			int resource = context.getResources().getIdentifier(propertyName + "_value", "plurals", context.getPackageName());
			if(resource == 0)
			{
				resource = context.getResources().getIdentifier(propertyName + "_value", "string", context.getPackageName());
				if(resource == 0)
				{
					return value;
				}
				else
				{
					return context.getString(resource, value);
				}

			}
			else
			{
				return context.getResources().getQuantityString(resource, maxValue, value);
			}
		}
	}

	private static Integer getInt(Bundle bundle, String name)
	{
		if(bundle.containsKey(name))
		{
			return bundle.getInt(name);
		}
		return null;
	}

}