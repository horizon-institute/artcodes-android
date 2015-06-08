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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.activities.ExperienceEditActivity;
import uk.ac.horizon.aestheticodes.controllers.ChildController;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.adapters.IntAdapter;
import uk.ac.horizon.aestheticodes.model.Experience;

public class EditMarkerRegionValueDialog extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final ExperienceEditActivity activity = ((ExperienceEditActivity) getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		final View view = inflater.inflate(R.layout.settings_slider, null);

		final Controller<Experience> controller = new ChildController<>(activity, view);
		controller.bindView(R.id.slider, new IntAdapter<Experience>("maxRegionValue", 3, 20));
		controller.bindView(R.id.sliderValue, "maxRegionValue");

		builder.setTitle(getString(R.string.property_set, getString(R.string.maxRegionValue)));
		String description = getString(R.string.maxRegionValue_desc);
		if (description != null)
		{
			builder.setMessage(description);
		}
		builder.setView(view);
		builder.setOnDismissListener(new DialogInterface.OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				controller.unbind();
			}
		});

		return builder.create();
	}
}
