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

public class IntRangeFormat extends IntFormat
{
	private Property maxProperty;

	public IntRangeFormat(Property maxProperty, Object min, Object max)
	{
		super(min, max);
		this.maxProperty = maxProperty;
	}

	@Override
	public String getDisplayString(Object value)
	{
		int minValue = getIntValue(value);
		int maxValue = getIntValue(maxProperty);
		if(minValue == maxValue)
		{
			int resource = context.getResources().getIdentifier(name + "_text", "plurals", context.getPackageName());
			if(resource == 0)
			{
				resource = context.getResources().getIdentifier(name + "_text", "string", context.getPackageName());
				if(resource == 0)
				{
					return Integer.toString(minValue);
				}
				else
				{
					return context.getString(resource, minValue);
				}

			}
			else
			{
				return context.getResources().getQuantityString(resource, minValue, Integer.toString(minValue));
			}
		}
		else
		{
			String valueString = minValue + "-" + maxValue;
			int resource = context.getResources().getIdentifier(name + "_text", "plurals", context.getPackageName());
			if(resource == 0)
			{
				resource = context.getResources().getIdentifier(name + "_text", "string", context.getPackageName());
				if(resource == 0)
				{
					return valueString;
				}
				else
				{
					return context.getString(resource, valueString);
				}

			}
			else
			{
				return context.getResources().getQuantityString(resource, maxValue, value);
			}
		}
	}

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
//						DialogFragment newFragment = new IntRangeDialogFragment();
//						Object value = get();
//						Bundle bundle = new Bundle();
//						setInt(bundle, "min", min);
//						setInt(bundle, "max", max);
//						setInt(bundle, "minValue", value);
//						setInt(bundle, "maxValue", maxName);
//						bundle.putString("title", getTextString(name, value));
//						bundle.putString("description", getTextString(name  +"_desc", value));
//						bundle.putString("minPropertyName", name);
//						bundle.putString("maxPropertyName", maxName);
//						newFragment.setArguments(bundle);
//						newFragment.show(((FragmentActivity)context).getSupportFragmentManager(), "missiles");
//					}
//				}
//			});
//		}
//	}
}