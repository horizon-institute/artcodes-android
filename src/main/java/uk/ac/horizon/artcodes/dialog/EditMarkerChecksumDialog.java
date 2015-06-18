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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;

public class EditMarkerChecksumDialog extends DialogFragment
{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final ExperienceEditActivity activity = ((ExperienceEditActivity) getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		final View view = inflater.inflate(R.layout.experience_edit_checksum, null);

		//final Controller<Experience> controller = new ChildController<>(activity, view);
		//controller.bindView(R.id.slider, new IntAdapter<Experience>("checksumModulo", 1, 12));
		//controller.bindView(R.id.sliderValue, new MarkerChecksumAdapter(activity));
		//controller.bindView(R.id.embeddedChecksumSwitch, "embeddedChecksum");

		builder.setTitle(getString(R.string.property_set, getString(R.string.checksumModulo)));
		builder.setMessage(getString(R.string.checksumModulo_desc));
		builder.setView(view);

		return builder.create();
	}
}
