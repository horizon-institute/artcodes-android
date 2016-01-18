/*
 * Artcodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.artcodes.ui;

import android.net.Uri;

import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.scanner.BR;

public class ActionEditor
{
	private final Action action;

	public ActionEditor(Action action)
	{
		this.action = action;
	}

	public SimpleTextWatcher getNameWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return action.getName();
			}

			@Override
			public void onTextChanged(String value)
			{
				if (value.trim().isEmpty())
				{
					if (action.getName() != null)
					{
						action.setName(null);
						action.notifyPropertyChanged(BR.name);
					}
				}
				else if (!value.equals(action.getName()))
				{
					action.setName(value);
					action.notifyPropertyChanged(BR.name);
				}
			}
		};
	}

	public SimpleTextWatcher getUrlWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return action.getDisplayUrl();
			}

			@Override
			public void onTextChanged(String value)
			{
				if (value.trim().isEmpty())
				{
					if (action.getUrl() != null)
					{
						action.setUrl(null);
						action.notifyPropertyChanged(BR.url);
						action.notifyPropertyChanged(BR.displayUrl);
					}
				}
				else
				{
					Uri uri = Uri.parse(value);
					String urlValue = value;
					if (uri.getScheme() == null)
					{
						urlValue = Action.HTTP_PREFIX + value;
					}
					if (!urlValue.equals(action.getUrl()))
					{
						action.setUrl(urlValue);
						action.notifyPropertyChanged(BR.url);
						action.notifyPropertyChanged(BR.displayUrl);
					}
				}
			}
		};
	}
}
