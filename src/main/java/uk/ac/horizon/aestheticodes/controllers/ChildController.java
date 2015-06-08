/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
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

package uk.ac.horizon.aestheticodes.controllers;

import android.view.View;
import uk.ac.horizon.aestheticodes.controllers.bindings.Binding;

public class ChildController<T> extends ControllerBase<T> implements Binding<T>
{
	private final View root;
	private final Controller<T> parent;

	public ChildController(Controller<T> parent, View root)
	{
		this.root = root;
		this.parent = parent;
		parent.bind(this);
	}

	public void setModel(T model)
	{
		parent.setModel(model);
		notifyChanges();
	}

	@Override
	public void notifyChanges(String... properties)
	{
		parent.notifyChanges(properties);
	}

	public T getModel()
	{
		return parent.getModel();
	}

	@Override
	protected View findView(int viewID)
	{
		return root.findViewById(viewID);
	}

	@Override
	public void updateView(Controller<T> controller, String... properties)
	{
		for(Binding<T> binding: bindings)
		{
			binding.updateView(controller, properties);
		}
	}
}
