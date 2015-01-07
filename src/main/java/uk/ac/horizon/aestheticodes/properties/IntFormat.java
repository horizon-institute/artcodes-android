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

package uk.ac.horizon.aestheticodes.properties;

public class IntFormat extends Format
{
	private Object min;
	private Object max;
	private Object off;

	public IntFormat(Object min, Object max)
	{
		this.min = min;
		this.max = max;
	}

	public IntFormat(Object min, Object max, Object off)
	{
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
		else if (object instanceof Property)
		{
			return getIntValue(((Property)object).get());
		}
		return null;
	}

	@Override
	public String getDisplayString(Object value)
	{
		Integer offValue = getIntValue(off);
		if (offValue != null && offValue.equals(value))
		{
			String text = getTextString("off", value);
			if (text == null)
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
			int resource = context.getResources().getIdentifier(name + "_text", "plurals", context.getPackageName());
			if (resource != 0)
			{
				return context.getResources().getQuantityString(resource, (Integer) value, value);
			}
		}
		return super.getDisplayString(value);
	}

	public int getMin()
	{
		return getIntValue(min);
	}

	public int getMax()
	{
		return getIntValue(max);
	}

//
//	@Override
//	protected void updateView()
//	{
//		super.updateView();
//
//		ViewParent parent = view.getParent();
//		if (parent instanceof View)
//		{
//			View view = (View) parent;
//			view.setOnClickListener(new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					if(context instanceof FragmentActivity)
//					{
//						DialogFragment newFragment = new IntDialogFragment();
//						Object value = get();
//						Bundle bundle = new Bundle();
//						setInt(bundle, "min", min);
//						setInt(bundle, "max", max);
//						setInt(bundle, "value", value);
//						setInt(bundle, "off", off);
//						bundle.putString("title", getTextString(name, value));
//						bundle.putString("description", getTextString(name  +"_desc", value));
//						bundle.putString("propertyName", name);
//						newFragment.setArguments(bundle);
//						newFragment.show(((FragmentActivity)context).getSupportFragmentManager(), "missiles");
//					}
//				}
//			});
//		}
//	}
}
