/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes;

import android.databinding.BindingAdapter;
import android.os.Build;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.koushikdutta.ion.Ion;

public final class Bindings
{
	@BindingAdapter("bind:imageUrl")
	public static void bindImageURL(ImageView view, String url)
	{
		Ion.with(view).fadeIn(true).load(url);
	}

	@BindingAdapter("bind:textWatcher")
	public static void bindTextWatcher(EditText view, TextWatcher watcher)
	{
		if(watcher != null)
		{
			view.addTextChangedListener(watcher);
		}
	}

	@BindingAdapter("bind:icon")
	public static void bindIcon(Button view, int icon)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
		}
		else
		{
			view.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
		}
	}
}
