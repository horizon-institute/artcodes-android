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

package uk.ac.horizon.aestheticodes.controllers.bindings;

import android.util.Log;
import android.view.View;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.adapters.ValueAdapter;

import java.util.Arrays;

public abstract class ViewBinding<T extends View, U> implements Binding<U>
{
	protected final T view;
	protected final ValueAdapter<U> adapter;

	public ViewBinding(T view, ValueAdapter<U> adapter)
	{
		this.view = view;
		this.adapter = adapter;
	}

	public void updateView(final Controller<U> controller, String... properties)
	{
		if(properties == null || properties.length == 0 || adapter.shouldUpdate(Arrays.asList(properties)))
		{
			Log.i("", "Updating " + view + " for " + Arrays.toString(properties));
			updateView(adapter.getValue(controller.getModel()));
		}
	}

	protected abstract void updateView(Object object);
}
