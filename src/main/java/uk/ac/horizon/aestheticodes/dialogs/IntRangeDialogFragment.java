/*
 * Aestheticodes recognises a different marker scheme that allows the
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
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.activities.ExperienceEditActivity;
import uk.ac.horizon.aestheticodes.properties.Property;

public class IntRangeDialogFragment extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final String minPropertyName = getArguments().getString("minProperty");
		final Property minProperty = ((ExperienceEditActivity)getActivity()).getProperties().get(minPropertyName);
		final String maxPropertyName = getArguments().getString("maxProperty");
		final Property maxProperty = ((ExperienceEditActivity)getActivity()).getProperties().get(maxPropertyName);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		final View view = inflater.inflate(R.layout.settings_range, null);

		minProperty.bindTo(view.findViewById(R.id.sliderValue));
		minProperty.bindTo(view.findViewById(R.id.sliderMin));

		maxProperty.bindTo(view.findViewById(R.id.sliderValue));
		maxProperty.bindTo(view.findViewById(R.id.sliderMax));

		minProperty.load();
		maxProperty.load();

		builder.setTitle(getString(R.string.property_set, minProperty.getFormat().getTextString(null, null)));
		String description = minProperty.getFormat().getTextString("desc", null);
		if (description != null)
		{
			builder.setMessage(description);
		}
		builder.setView(view);
		builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				minProperty.unbind(R.id.sliderValue);
				minProperty.unbind(R.id.sliderMin);

				maxProperty.unbind(R.id.sliderValue);
				maxProperty.unbind(R.id.sliderMax);

				minProperty.save();
				maxProperty.save();
			}
		});
		builder.setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				minProperty.unbind(R.id.sliderValue);
				minProperty.unbind(R.id.sliderMin);

				maxProperty.unbind(R.id.sliderValue);
				maxProperty.unbind(R.id.sliderMax);

				minProperty.load();
				maxProperty.load();
			}
		});
		return builder.create();
	}

	public static void create(FragmentManager fragmentManager, String minProperty, String maxProperty)
	{
		DialogFragment newFragment = new IntRangeDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("minProperty", minProperty);
		bundle.putString("maxProperty", maxProperty);
		newFragment.setArguments(bundle);
		newFragment.show(fragmentManager, "IntDialog");
	}
}
