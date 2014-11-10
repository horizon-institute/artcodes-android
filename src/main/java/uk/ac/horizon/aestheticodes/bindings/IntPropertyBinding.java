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

package uk.ac.horizon.aestheticodes.bindings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewParent;
import uk.ac.horizon.aestheticodes.dialogs.IntDialogFragment;

public class IntPropertyBinding extends PropertyBinding
{
	private Object min;
	private Object max;
	private Object off;

	public IntPropertyBinding(String name, Object min, Object max)
	{
		super(name);
		this.min = min;
		this.max = max;
	}

	public IntPropertyBinding(String name, Object min, Object max, Object off)
	{
		super(name);
		this.min = min;
		this.max = max;
		this.off = off;
	}

	public Integer getIntValue(Object object)
	{
		if (object instanceof Integer)
		{
			return (Integer) object;
		}
		else if (object instanceof String)
		{
			//Object value = binding.get//
		}
		return null;
	}

	@Override
	public String getText()
	{
		Integer offValue = getIntValue(off);
		Object value = get();
		if (offValue != null && offValue.equals(value))
		{
			String text = getTextString(name + "_off",value);
			if(text == null)
			{
				return "Off";
			}
			else
			{
				return text;
			}
		}

		if (value instanceof Integer)
		{
			int resource = context.getResources().getIdentifier(getName() + "_text", "plurals", context.getPackageName());
			if (resource != 0)
			{
				return context.getResources().getQuantityString(resource, (Integer) value, value);
			}
		}
		return super.getText();
	}

	@Override
	protected void updateView()
	{
		super.updateView();

		ViewParent parent = view.getParent();
		if (parent instanceof View)
		{
			View view = (View) parent;
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(context instanceof FragmentActivity)
					{
						DialogFragment newFragment = new IntDialogFragment();
						Object value = get();
						Bundle bundle = new Bundle();
						setInt(bundle, "min", min);
						setInt(bundle, "max", max);
						setInt(bundle, "value", value);
						setInt(bundle, "off", off);
						bundle.putString("title", getTextString(name, value));
						bundle.putString("description", getTextString(name  +"_desc", value));
						bundle.putString("propertyName", name);
						newFragment.setArguments(bundle);
						newFragment.show(((FragmentActivity)context).getSupportFragmentManager(), "missiles");
					}
				}
			});
		}
	}

	private Integer getInteger(Object object)
	{
		if(object instanceof Integer)
		{
			return (Integer)object;
		}
		else if(object instanceof String)
		{
			Object result =  getProperty((String)object);
			if(result instanceof Integer)
			{
				return (Integer)result;
			}
		}
		return null;
	}

	private void setInt(Bundle bundle, String name, Object property)
	{
		Integer value = getInteger(property);
		if(value != null)
		{
			bundle.putInt(name, value);
		}
	}
}
