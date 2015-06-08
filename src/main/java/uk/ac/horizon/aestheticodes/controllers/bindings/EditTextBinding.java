/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
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

package uk.ac.horizon.aestheticodes.controllers.bindings;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import uk.ac.horizon.aestheticodes.controllers.adapters.ValueAdapter;

public class EditTextBinding<T> extends EditorBinding<EditText, T>
{
	public EditTextBinding(final EditText editText, final ValueAdapter<T> adapter)
	{
		super(editText, adapter);
		view.addTextChangedListener(new TextWatcher()
		{
			private final Handler handler = new Handler();
			private final Runnable userStoppedTyping = new Runnable()
			{
				@Override
				public void run()
				{
					adapter.setValue(controller, view.getText().toString());
				}
			};

			@Override
			public void afterTextChanged(Editable editable)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
				handler.removeCallbacksAndMessages(null);
				handler.postDelayed(userStoppedTyping, 1000);
			}
		});
		view.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!hasFocus)
				{
					adapter.setValue(controller, view.getText().toString());
				}
			}
		});

		if(adapter instanceof InputFilter)
		{
			view.setFilters(new InputFilter[] {(InputFilter)adapter});
		}
	}

//	@Override
//	public void disconnect()
//	{
//		// TODO view.removeTextChangedListener(textWatcher);
//		view.setOnFocusChangeListener(null);
//	}

	public String getText()
	{
		return view.getText().toString();
	}

	@Override
	public void updateView(Object value)
	{
		String editString;
		if (value == null || !(value instanceof String))
		{
			editString = "";
		}
		else
		{
			editString = (String) value;
		}
		if (!view.getText().toString().equals(editString))
		{
			view.setError(null);
			view.setText(editString);
		}
	}
}
