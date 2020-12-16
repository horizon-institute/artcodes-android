/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public enum ScannerFeatures implements Feature {
	load_old_experiences(true),
	combined_markers(false);

	private final boolean defaultValue;

	ScannerFeatures(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getName() {
		return name();
	}

	public boolean isEnabled(@NonNull Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(Feature.class.getName(), Context.MODE_PRIVATE);
		final String featureName = name();
		if (preferences.contains(featureName)) {
			return preferences.getBoolean(featureName, defaultValue);
		}
		return defaultValue;
	}

	public void setEnabled(@NonNull Context context, boolean enabled) {
		final SharedPreferences preferences = context.getSharedPreferences(Feature.class.getName(), Context.MODE_PRIVATE);
		final String featureName = name();
		preferences.edit().putBoolean(featureName, enabled).apply();
	}
}
