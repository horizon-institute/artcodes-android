/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

import android.databinding.BaseObservable;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.BR;

public class ExperienceEditor extends BaseObservable
{
	private final Experience experience;

	public ExperienceEditor(Experience experience)
	{
		this.experience = experience;
	}

	public SimpleTextWatcher getDescWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return experience.getDescription();
			}

			@Override
			public void onTextChanged(String value)
			{
				if (!value.equals(experience.getDescription()))
				{
					experience.setDescription(value);
					//experience.notifyPropertyChanged(BR.description);
				}
			}
		};
	}

	public SimpleTextWatcher getNameWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return experience.getName();
			}

			@Override
			public void onTextChanged(String value)
			{
				if (!value.equals(experience.getName()))
				{
					experience.setName(value);
					//experience.notifyPropertyChanged(BR.name);
				}
			}
		};
	}

}
