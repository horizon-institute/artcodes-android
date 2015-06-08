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

package uk.ac.horizon.aestheticodes.controllers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.ViewController;
import uk.ac.horizon.aestheticodes.controllers.bindings.BindingFactory;
import uk.ac.horizon.aestheticodes.controllers.bindings.ListBinding;
import uk.ac.horizon.aestheticodes.controllers.bindings.ViewBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAdapter<T, U> extends PropertyAdapter<T>
{
	private class Binding
	{
		final int viewID;
		final BindingFactory<U> factory;

		private Binding(int viewID, BindingFactory<U> factory)
		{
			this.viewID = viewID;
			this.factory = factory;
		}
	}

	private final Comparator<U> comparator;
	private final int layout;
	private final List<Binding> bindings = new ArrayList<>();
	private final Map<U, ViewController<U>> controllers = new HashMap<>();

	public ListAdapter(String property, int layout)
	{
		super(property);
		this.comparator = null;
		this.layout = layout;
	}

	public ListAdapter(String property, Comparator<U> comparator, int layout)
	{
		super(property);
		this.comparator = comparator;
		this.layout = layout;
	}

	@Override
	public Object adapt(Object object)
	{
		if (comparator != null)
		{
			List<U> sorted = null;
			if (object instanceof Collection)
			{
				sorted = new ArrayList<>((Collection<U>) object);
			}
			else if (object instanceof Map)
			{
				sorted = new ArrayList<>(((Map) object).values());
			}
			if (sorted != null)
			{
				Collections.sort(sorted, comparator);
				return sorted;
			}
		}
		else if (object instanceof Collection)
		{
			return object;
		}
		else if (object instanceof Map)
		{
			return ((Map) object).values();
		}
		return object;
	}

	public Controller<U> getController(U item)
	{
		return controllers.get(item);
	}

	public void bindView(int viewID, BindingFactory<U> factory)
	{
		bindings.add(new Binding(viewID, factory));
	}

	public void bindView(int viewID, String propertyName)
	{
		bindView(viewID, new PropertyAdapter<U>(propertyName));
	}

	@Override
	public ViewBinding<?, T> createBinding(View view)
	{
		return new ListBinding<>((ViewGroup) view, this);
	}

	public ViewController<U> createController(ViewGroup root, U item)
	{
		ViewController<U> controller = controllers.get(item);
		if(controller == null)
		{
			LayoutInflater layoutInflater = (LayoutInflater) root.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(layout, root, false);
			controller = new ViewController<>(view);
			for (Binding binding : bindings)
			{
				controller.bindView(binding.viewID, binding.factory);
			}
			controller.setModel(item);
			controllers.put(item, controller);
		}
		root.addView(controller.getView());
		return controller;
	}

	public void retainAll(List<ViewController<U>> controllers)
	{
		this.controllers.values().retainAll(controllers);
		Log.i("", "Controllers size = " + this.controllers.size());
	}
}
