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

package uk.ac.horizon.aestheticodes.settings;

public class SettingsItem
{
	public enum Type
	{
		header, single_line, two_line, two_line_disabled
	}

	final SettingsActivity activity;
	private String title;

	SettingsItem(SettingsActivity activity)
	{
		this.activity = activity;
	}

	SettingsItem(SettingsActivity activity, String title)
	{
		this.activity = activity;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public Type getType()
	{
		return Type.header;
	}

	public void selected()
	{

	}

	public int getIcon()
	{
		return 0;
	}

	public String getDetail()
	{
		return null;
	}
}
