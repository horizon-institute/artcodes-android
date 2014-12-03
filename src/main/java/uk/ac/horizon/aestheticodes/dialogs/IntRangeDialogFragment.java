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
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.activities.ExperienceEditActivity;

public class IntRangeDialogFragment extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Set " + getArguments().getString("title"));
		String description = getArguments().getString("description");
		if (description != null)
		{
			builder.setMessage(description);
		}

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.settings_range, null);

//		sliderValue = (TextView) view.findViewById(R.id.sliderValue);
//		sliderMin = (SeekBar) view.findViewById(R.id.sliderMin);
//		sliderMax = (SeekBar) view.findViewById(R.id.sliderMax);
//
//		minPropertyName = getArguments().getString("minPropertyName");
//		final String maxPropertyName = getArguments().getString("maxPropertyName");
//		min = getInt(getArguments(), "min");
//		final int max = getInt(getArguments(), "max");
//		final int minValue = getInt(getArguments(), "minValue");
//		final int maxValue = getInt(getArguments(), "maxValue");
//
//		sliderMin.setMax(max - min);
//		sliderMin.setProgress(minValue - min);
//		sliderMin.setOnSeekBarChangeListener(listener);
//
//		sliderMax.setMax(max - min);
//		sliderMax.setProgress(maxValue - min);
//		sliderMax.setOnSeekBarChangeListener(listener);
//
//		update();

		builder.setView(view);
		builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
//				final int minValue = sliderMin.getProgress() + min;
//				final int maxValue = sliderMax.getProgress() + min;
//				((ExperienceEditActivity)getActivity()).getProperties().get(minPropertyName).set(minValue);
//				((ExperienceEditActivity)getActivity()).getProperties().get(maxPropertyName).set(maxValue);
//				//((SettingsActivity)getActivity()).setProperty(minPropertyName, value);
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

	public static void create(FragmentManager fragmentManager, String propertyName)
	{
		DialogFragment newFragment = new IntRangeDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("propertyName", propertyName);
		newFragment.setArguments(bundle);
		newFragment.show(fragmentManager, "IntDialog");
	}
}