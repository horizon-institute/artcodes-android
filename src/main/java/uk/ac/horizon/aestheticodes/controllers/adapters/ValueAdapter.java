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
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import uk.ac.horizon.aestheticodes.controllers.Controller;
import uk.ac.horizon.aestheticodes.controllers.bindings.Binding;
import uk.ac.horizon.aestheticodes.controllers.bindings.BindingFactory;
import uk.ac.horizon.aestheticodes.controllers.bindings.EditTextBinding;
import uk.ac.horizon.aestheticodes.controllers.bindings.ImageBinding;
import uk.ac.horizon.aestheticodes.controllers.bindings.SwitchBinding;
import uk.ac.horizon.aestheticodes.controllers.bindings.TextBinding;
import uk.ac.horizon.aestheticodes.controllers.bindings.ViewBinding;

import java.util.List;

public abstract class ValueAdapter<T> implements BindingFactory<T>
{
	public Binding<T> createBinding(View view)
	{
		if(view instanceof EditText)
		{
			return new EditTextBinding<>((EditText)view, this);
		}
		else if(view instanceof SwitchCompat)
		{
			return new SwitchBinding<>((SwitchCompat)view, this);
		}
		else if(view instanceof ImageView)
		{
			return new ImageBinding<>((ImageView)view, this);
		}
		else if(view instanceof TextView)
		{
			return new TextBinding<>((TextView)view, this);
		}
		return null;
	}

	public abstract Object getValue(T object);

	public void setValue(Controller<T> controller, Object value)
	{

	}

	public boolean shouldUpdate(List<String> properties)
	{
		return true;
	}
}
