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

package uk.ac.horizon.aestheticodes.properties.bindings;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import uk.ac.horizon.aestheticodes.properties.Format;
import uk.ac.horizon.aestheticodes.properties.Property;

public class EditTextBinding extends ViewBinding
{
	public EditTextBinding(View view)
	{
		super(view);
	}

	@Override
	public void update(Object value, Format format)
	{
		if(view instanceof EditText)
		{
			EditText editText = (EditText)view;
			editText.setError(null);
			if(!editText.getText().toString().equals(value))
			{
				editText.setText(format.getDisplayString(value));
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
						property.set(editText.getText().toString());
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
			return true;
		}
		return false;
	}
}
