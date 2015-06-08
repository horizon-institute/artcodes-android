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

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import uk.ac.horizon.aestheticodes.controllers.adapters.PropertyAdapter;
import uk.ac.horizon.aestheticodes.controllers.bindings.Binding;
import uk.ac.horizon.aestheticodes.controllers.bindings.BindingFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class ControllerActivity<T> extends AppCompatActivity implements Controller<T>
{
	private final List<Binding<T>> bindings = new ArrayList<>();
	private T model;

	public void bindView(int viewID, String propertyName)
	{
		final View view = findViewById(viewID);
		if (view != null)
		{
			final PropertyAdapter<T> adapter = new PropertyAdapter<>(propertyName);
			bind(adapter.createBinding(view));
		}
	}

	public void bind(Binding<T> binding)
	{
		if(binding != null)
		{
			bindings.add(binding);
			binding.updateView(this);
		}
	}

	public void unbind(Binding<T> binding)
	{
		bindings.remove(binding);
	}

	public void unbind()
	{
		bindings.clear();
	}

	public void bindView(int viewID, BindingFactory<T> adapter)
	{
		final View view = findViewById(viewID);
		if (view != null)
		{
			final Binding<T> viewBinding = adapter.createBinding(view);
			if (viewBinding != null)
			{
				viewBinding.updateView(this);
				bindings.add(viewBinding);
			}
		}
	}

	public T getModel()
	{
		return model;
	}

	public void setModel(T model)
	{
		this.model = model;
		notifyChanges();
	}

	public void notifyChanges(String... properties)
	{
		for(Binding<T> binding: bindings)
		{
			binding.updateView(this, properties);
		}
	}
}
