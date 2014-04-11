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

package uk.ac.horizon.aestheticodes.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.data.DtouchMarkersDataSource;

import java.util.List;

public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target)
	{
		// TODO loadHeadersFromResource(R.xml.preference_headers, target);
	}

	private void resetPrefs()
	{
		new AlertDialog.Builder(this).setTitle("Reset MarkerPreferences?").setMessage("Are you sure you want to reset all preferences to their default value?")
				.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Reset", new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int whichButton)
			{
				resetToDefault();
			}
		}).setNegativeButton("Cancel", null).show();

	}

	// Clear the prefs and reset to default
	private void resetToDefault()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().clear().commit();
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference, true);
	}

	private void updateMarkerPreferences()
	{
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);

		for(String key: preferences.getAll().keySet())
		{
			if(key.startsWith("marker_"))
			{

			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// Set flag to reinitialise the markers next time they are scanned
		DtouchMarkersDataSource.prefsChanged = true;
	}

}
