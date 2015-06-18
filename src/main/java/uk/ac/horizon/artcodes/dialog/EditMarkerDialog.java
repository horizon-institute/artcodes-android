/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.aestheticodes.databinding.MarkerEditBinding;
import uk.ac.horizon.artcodes.model.Marker;

public class EditMarkerDialog extends DialogFragment
{

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final ExperienceEditActivity activity = ((ExperienceEditActivity) getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final MarkerEditBinding binding = MarkerEditBinding.inflate(getActivity().getLayoutInflater());

		Marker marker = null;
		if (getArguments() != null)
		{
			final String code = getArguments().getString("code");
			if(code != null)
			{
				// TODO marker = activity.getModel().getMarker(code);
			}
		}

		if(marker == null)
		{
			marker = new Marker();
			// TODO marker.setCode(activity.getModel().getNextUnusedMarker());
		}

		binding.setMarker(marker);

//		controller.bindView(R.id.markerCode, new MarkerCodeAdapter(activity.getModel()));
//		controller.bindView(R.id.markerShowDetail, "showDetail");
//		controller.bindView(R.id.markerDetails, new VisibilityAdapter<Marker>("showDetail"));
//
//		controller.bindView(R.id.markerAction, new URLAdapter<Marker>("action"));
//		controller.bindView(R.id.markerImage, new URLAdapter<Marker>("image"));
//		controller.bindView(R.id.markerTitle, "title");
//		controller.bindView(R.id.markerDescription, "description");

		builder.setView(binding.getRoot());

		final AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

	public static EditMarkerDialog create(Marker marker)
	{
		EditMarkerDialog newFragment = new EditMarkerDialog();
		Bundle bundle = new Bundle();
		bundle.putString("code", marker.getCode());
		newFragment.setArguments(bundle);

		return newFragment;
	}
}
