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

package uk.ac.horizon.artcodes.adapter;

import android.content.Context;

import uk.ac.horizon.artcodes.Feature;

public class FeatureItem {
	private final Feature feature;
	private final Context context;

	public FeatureItem(Feature feature, Context context) {
		this.feature = feature;
		this.context = context;
	}

	public boolean isEnabled() {
		return feature.isEnabled(context);
	}

	public void setEnabled(boolean value) {
		feature.setEnabled(context, value);
	}

	public String getName() {
		return feature.getName().replace('_', ' ');
	}
}
