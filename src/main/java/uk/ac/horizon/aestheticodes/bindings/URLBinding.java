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
import android.util.Patterns;
import android.view.View;

public class URLBinding
{
	public URLBinding(Context context, Object object, String property, View view, Object defaultValue)
	{
		//super(context, object, property, view, defaultValue);
	}

	//@Override
	public boolean isValid()
	{
		//if (Patterns.WEB_URL.matcher(field.getText().toString()).matches())
		//{
		//	return true;
		//}
		//field.setError("Invalid URL");
		return false;
	}

	//@Override
	protected String getText()
	{
		String text = "";//super.getText();
		if(text.startsWith("http://"))
		{
			text = text.substring(7);
		}

		if(text.startsWith("www."))
		{
			text = text.substring(4);
		}

		return text;
	}
}
