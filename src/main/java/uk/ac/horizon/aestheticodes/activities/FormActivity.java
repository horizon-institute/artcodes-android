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

package uk.ac.horizon.aestheticodes.activities;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public abstract class FormActivity extends ActionBarActivity
{
	static abstract class FieldValidator
	{
		protected final EditText field;

		public FieldValidator(EditText field)
		{
			this.field = field;
		}

		public void save()
		{
			save(field.getText().toString());
		}

		protected abstract boolean isValid();

		protected abstract void save(String value);
	}

	static abstract class BasicFieldValidator extends FieldValidator
	{
		public BasicFieldValidator(EditText field)
		{
			super(field);
		}

		protected boolean isValid()
		{
			return !field.getText().toString().isEmpty();
		}
	}

	static abstract class URLFieldValidator extends FieldValidator
	{
		public URLFieldValidator(EditText field)
		{
			super(field);
		}

		protected boolean isValid()
		{
			if(Patterns.WEB_URL.matcher(field.getText().toString()).matches())
			{
				return true;
			}
			field.setError("Invalid URL");
			return false;
		}
	}

	private final List<FieldValidator> validators = new ArrayList<FieldValidator>();
	private final TextWatcher watcher = new TextWatcher()
	{
		private final Handler handler = new Handler();

		final Runnable userStoppedTyping = new Runnable()
		{
			@Override
			public void run()
			{
				boolean valid = true;
				for(FieldValidator validator: validators)
				{
					if(!validator.isValid())
					{
						valid = false;
					}
				}

				setValid(valid);
			}
		};

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
		{

		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
		{
			for(FieldValidator validator: validators)
			{
				validator.field.setError(null);
			}
			handler.removeCallbacksAndMessages(null);
			handler.postDelayed(userStoppedTyping, 2000);
		}

		@Override
		public void afterTextChanged(Editable editable)
		{
		}
	};

	protected void save()
	{
		for(FieldValidator validator: validators)
		{
			validator.save();
		}
	}

	protected abstract void setValid(boolean valid);

	protected void addValidator(FieldValidator validator)
	{
		validators.add(validator);
		validator.field.addTextChangedListener(watcher);
	}
}
