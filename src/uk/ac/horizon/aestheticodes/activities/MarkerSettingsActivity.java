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
import uk.ac.horizon.aestheticodes.model.MarkerSettings;
import uk.ac.horizon.aestheticodes.settings.IntPropertySettingsItem;
import uk.ac.horizon.aestheticodes.settings.Property;
import uk.ac.horizon.aestheticodes.settings.SettingsActivity;
import uk.ac.horizon.aestheticodes.settings.SettingsItem;

public class MarkerSettingsActivity extends SettingsActivity
{
	private final static MarkerSettings settings = MarkerSettings.getSettings();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		adapter.add(new IntPropertySettingsItem(this, settings, "minRegions", 1, "maxRegions"));
		adapter.add(new IntPropertySettingsItem(this, settings, "maxRegions", "minRegions", 12));
		adapter.add(new IntPropertySettingsItem(this, settings, "maxRegionValue", 1, 9));
		adapter.add(new IntPropertySettingsItem(this, settings, "maxEmptyRegions", 0, "maxRegions", 0));
		adapter.add(new IntPropertySettingsItem(this, settings, "validationRegions", 0, "maxRegions", 0));
		adapter.add(new IntPropertySettingsItem(this, settings, "validationRegionValue", 1, "maxRegionValue"));
		adapter.add(new IntPropertySettingsItem(this, settings, "checksumModulo", 1, 12, 1));

		adapter.notifyDataSetChanged();
	}

	@Override
	public void refresh()
	{
		MarkerSettingsHelper.saveSettings(this);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void setProperty(String propertyName, Object value)
	{
		Property property = new Property(settings, propertyName);
		property.set(value);
	}
}
