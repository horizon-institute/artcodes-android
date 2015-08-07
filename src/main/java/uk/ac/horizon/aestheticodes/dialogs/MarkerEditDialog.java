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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.activities.ExperienceEditActivity;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.properties.bindings.VisibilityBinding;
import uk.ac.horizon.aestheticodes.properties.MarkerFormat;
import uk.ac.horizon.aestheticodes.properties.Properties;
import uk.ac.horizon.aestheticodes.properties.URLFormat;

public class MarkerEditDialog extends DialogFragment
{
	Marker marker = null;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		final ExperienceEditActivity activity = (ExperienceEditActivity) getActivity();
		if (getArguments() != null)
		{
			final String code = getArguments().getString("code");

			marker = activity.getExperience().getMarkers().get(code);
		}

		if (marker == null)
		{
			marker = new Marker();
		}

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.marker_edit, null);

		final String originalCode = marker.getCode();
		final Properties properties = new Properties(getActivity(), marker, view);
		if (originalCode == null)
		{
			marker.setCode(activity.getExperience().getNextUnusedMarker());
		}

		Log.i("", marker.getCode());

		properties.get("code").formatAs(new MarkerFormat(activity.getExperience(), originalCode)).bindTo(R.id.markerCode);
		properties.get("showDetail").bindTo(R.id.markerShowDetail)
				.bindTo(new VisibilityBinding(R.id.markerDetails));

		properties.get("resetHistoryOnOpen").bindTo(R.id.resetHistoryOnOpen);

		properties.get("action").formatAs(new URLFormat()).bindTo(R.id.markerAction);
		properties.get("image").formatAs(new URLFormat()).bindTo(R.id.markerImage);
		properties.get("title").bindTo(R.id.markerTitle);
		properties.get("description").bindTo(R.id.markerDescription);
		properties.load();

		builder.setView(view);
		builder.setPositiveButton(R.string.dialog_action_set, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				if (properties.isValid())
				{
					Marker marker = (Marker) properties.save();
					ExperienceEditActivity activity = (ExperienceEditActivity) getActivity();
					if(originalCode != null)
					{
						activity.getExperience().getMarkers().remove(originalCode);
					}
					activity.getExperience().getMarkers().put(marker.getCode(), marker);
					activity.updateMarkers();
				}
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
				confirmBuilder.setTitle(activity.getString(R.string.markerDeleteConfirmTitle, marker.getCode()));
				confirmBuilder.setMessage(activity.getString(R.string.markerDeleteConfirmMessage, marker.getCode()));
				confirmBuilder.setPositiveButton(R.string.deleteConfirm, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						activity.getExperience().deleteMarker(marker.getCode());
						activity.updateMarkers();
					}
				});
				confirmBuilder.setNegativeButton(R.string.deleteCancel, new DialogInterface.OnClickListener()
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
//		properties.set()
//		{
//			@Override
//			protected void setValid(boolean isValid)
//			{
//				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(properties.isValid());
//			}
//		};

		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}
}
