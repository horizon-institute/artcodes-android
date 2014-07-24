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

import android.os.Bundle;
import android.util.Log;
import uk.ac.horizon.aestheticodes.model.MarkerAction;
import uk.ac.horizon.aestheticodes.model.MarkerSettings;
import uk.ac.horizon.aestheticodes.settings.ActivitySettingsItem;
import uk.ac.horizon.aestheticodes.settings.AddMarkerSettingsItem;
import uk.ac.horizon.aestheticodes.settings.MarkerSettingsItem;
import uk.ac.horizon.aestheticodes.settings.SettingsActivity;
import uk.ac.horizon.aestheticodes.settings.SettingsItem;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MarkerListActivity extends SettingsActivity
{
	final MarkerSettings settings = MarkerSettings.getSettings();

	@Override
	public void refresh()
	{
		adapter.clear();
		final List<MarkerAction> actions = new ArrayList<MarkerAction>(settings.getMarkers().values());
		Collections.sort(actions, new Comparator<MarkerAction>()
		{
			@Override
			public int compare(MarkerAction markerAction, MarkerAction markerAction2)
			{
				return markerAction.getCode().compareTo(markerAction2.getCode());
			}
		});
		for(MarkerAction action: actions)
		{
			if(action.isVisible())
			{
				adapter.add(new MarkerSettingsItem(this, action));
			}
		}

		if(settings.canAddMarker())
		{
			adapter.add(new AddMarkerSettingsItem(this, "Add Marker"));
		}

		adapter.add(new ActivitySettingsItem(this, "Settings", MarkerSettingsActivity.class));
		adapter.add(new ActivitySettingsItem(this, "About", AboutActivity.class));

		adapter.notifyDataSetChanged();

		MarkerSettingsHelper.saveSettings(this);
	}
}