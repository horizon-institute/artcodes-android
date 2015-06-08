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

package uk.ac.horizon.aestheticodes.controllers.bindings;

import android.view.ViewGroup;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.ViewController;
import uk.ac.horizon.aestheticodes.controllers.adapters.ListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListBinding<T, U> extends ViewBinding<ViewGroup, T>
{
	private final ListAdapter<T, U> listAdapter;
	private final List<ViewController<U>> controllers = new ArrayList<>();

	public ListBinding(ViewGroup view, ListAdapter<T, U> adapter)
	{
		super(view, adapter);
		this.listAdapter = adapter;
	}

	@Override
	public void updateView(Object object)
	{
		if (object instanceof Collection)
		{
			final Collection<U> list = (Collection<U>) object;
			int index = 0;

			if (list.isEmpty())
			{
				for(ViewController<U> controller: controllers)
				{
					view.removeView(controller.getView());
				}

				controllers.clear();
			}
			else
			{
				for (U item : list)
				{
					while (index < controllers.size() && !controllers.get(index).getModel().equals(item))
					{
						view.removeView(controllers.get(index).getView());
						controllers.remove(index);
					}

					if (index == controllers.size())
					{
						ViewController<U> viewController = listAdapter.createController(view, item);
						controllers.add(viewController);
					}

					index++;
				}

			}

			listAdapter.retainAll(controllers);
		}
	}
}
