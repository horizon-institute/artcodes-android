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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import uk.ac.horizon.aestheticodes.R;

/**
 * Created by kevin on 22/10/2014.
 */
public class OldPropertyThings
{
//	public abstract void selected();
//
//	String getString(String suffix, String defaultValue)
//	{
//		String propertyName = name;
//		if(suffix != null)
//		{
//			propertyName = propertyName +"_" + suffix;
//		}
//		int resource = context.getResources().getIdentifier(propertyName, "string", context.getPackageName());
//		if (resource == 0)
//		{
//			return defaultValue;
//		}
//		else
//		{
//			return context.getString(resource);
//		}
//	}
//
//	public View createView(ViewGroup viewGroup)
//	{
//		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = inflater.inflate(R.layout.marker_listitem, viewGroup, false);
//
//		final TextView markerCode = (TextView) view.findViewById(R.id.markerCode);
//		detailView = (TextView) view.findViewById(R.id.markerAction);
//
//		markerCode.setText(getString(null, name));
//		detailView.setText(getDetail());
//		view.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				selected();
//			}
//		});
//
//		return view;
//	}
//
//	public String getDetail()
//	{
//		String propertyName = name;
//		int resource = context.getResources().getIdentifier(propertyName + "_value", "string", context.getPackageName());
//		if (resource == 0)
//		{
//			return property.getString();
//		}
//		else
//		{
//			return context.getString(resource, property.get());
//		}
//	}
}
