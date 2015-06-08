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
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.activities.ExperienceEditActivity;
import uk.ac.horizon.aestheticodes.controllers.ChildController;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.adapters.MarkerCodeAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.URLAdapter;
import uk.ac.horizon.aestheticodes.controllers.adapters.VisibilityAdapter;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.controllers.ViewController;

public class EditMarkerDialog extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final ExperienceEditActivity activity = ((ExperienceEditActivity) getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		final View view = inflater.inflate(R.layout.marker_edit, null);

		Marker marker = null;
		if (getArguments() != null)
		{
			final String code = getArguments().getString("code");
			if(code != null)
			{
				marker = activity.getModel().getMarker(code);
			}
		}

		Controller<Marker> controller;
		if(marker != null)
		{
			Controller<Marker> parent = activity.getMarker(marker);
			if(parent != null)
			{
				controller = new ChildController<>(parent, view);
			}
			else
			{
				controller = new ViewController<>(view);
				controller.setModel(marker);
			}
		}
		else
		{
			controller = new ViewController<>(view);
			marker = new Marker();
			marker.setCode(activity.getModel().getNextUnusedMarker());
			controller.setModel(marker);
		}

		controller.bindView(R.id.markerCode, new MarkerCodeAdapter(activity.getModel()));
		controller.bindView(R.id.markerShowDetail, "showDetail");
		controller.bindView(R.id.markerDetails, new VisibilityAdapter<Marker>("showDetail"));

		controller.bindView(R.id.markerAction, new URLAdapter<Marker>("action"));
		controller.bindView(R.id.markerImage, new URLAdapter<Marker>("image"));
		controller.bindView(R.id.markerTitle, "title");
		controller.bindView(R.id.markerDescription, "description");

		builder.setView(view);

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
