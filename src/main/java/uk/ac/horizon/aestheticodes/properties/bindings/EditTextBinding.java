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

package uk.ac.horizon.aestheticodes.properties.bindings;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Property;

public class EditTextBinding extends ViewBinding
{
	private Format format;

	public EditTextBinding(View view)
	{
		super(view);
	}

	@Override
	public void update(Object value, Format format)
	{
		this.format = format;
		if(view instanceof EditText)
		{
			EditText editText = (EditText)view;
			if(format instanceof InputFilter)
			{
				editText.setFilters(new InputFilter[] {(InputFilter)format});
			}
			editText.setError(null);
			String editString = format.getEditString(value);
			if(!editText.getText().toString().equals(editString))
			{
				editText.setText(editString);
			}
		}
	}

	@Override
	public void setError(String error)
	{
		if(view instanceof EditText)
		{
			EditText editText = (EditText)view;
			editText.setError(error);
		}
	}

	@Override
	public void save(Property property)
	{
		if(view instanceof EditText)
		{
			property.set(format.getSaveValue(((EditText)view).getText().toString()));
		}
	}

	@Override
	public boolean init(final Property property)
	{
		super.init(property);

		if(view instanceof EditText)
		{
			final EditText editText = (EditText)view;
			editText.addTextChangedListener(new TextWatcher()
			{
				private final Handler handler = new Handler();

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
				{

				}

				final Runnable userStoppedTyping = new Runnable()
				{
					@Override
					public void run()
					{
						save(property);
					}
				};

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
				{
					handler.removeCallbacksAndMessages(null);
					handler.postDelayed(userStoppedTyping, 2000);
				}

				@Override
				public void afterTextChanged(Editable editable)
				{
				}
			});
			editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if(!hasFocus)
					{
						save(property);
					}
				}
			});
			return true;
		}
		return false;
	}
}
