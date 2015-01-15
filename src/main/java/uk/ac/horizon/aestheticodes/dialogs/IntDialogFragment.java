/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
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

public class IntDialogFragment extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final String propertyName = getArguments().getString("propertyName");
		final Property property = ((ExperienceEditActivity)getActivity()).getProperties().get(propertyName);

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		final View view = inflater.inflate(R.layout.settings_slider, null);

		property.bindTo(view.findViewById(R.id.sliderValue));
		property.bindTo(view.findViewById(R.id.slider));
		property.load();

		builder.setTitle(getString(R.string.property_set, property.getFormat().getTextString(null, null)));
		String description = property.getFormat().getTextString("desc", null);
		if (description != null)
		{
			builder.setMessage(description);
		}
		builder.setView(view);
		builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				property.unbind(R.id.sliderValue);
				property.unbind(R.id.slider);
				property.save();
			}
		});
		builder.setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				property.unbind(R.id.sliderValue);
				property.unbind(R.id.slider);
				property.load();
			}
		});

		return builder.create();
	}

	public static void create(FragmentManager fragmentManager, String propertyName)
	{
		DialogFragment newFragment = new IntDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("propertyName", propertyName);
		newFragment.setArguments(bundle);
		newFragment.show(fragmentManager, "IntDialog");
	}
}
