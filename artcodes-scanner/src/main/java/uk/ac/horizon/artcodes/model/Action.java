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

package uk.ac.horizon.artcodes.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.ArrayList;
import java.util.List;

public class Action extends BaseObservable
{
	public enum Match
	{
		any, all, sequence
	}

	public static final String HTTP_PREFIX = "http://";
	private List<String> codes = new ArrayList<>();
	private Match match = Match.any;
	private String url;
	private String name;
	private String description;
	private String image;
	private String owner;
	//private boolean showDetail = false;

	public List<String> getCodes()
	{
		return codes;
	}

	@Bindable
	public String getDescription()
	{
		return description;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public Match getMatch()
	{
		return match;
	}

	@Bindable
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

//	public boolean getShowDetail()
//	{
//		return showDetail;
//	}

	@Bindable
	public String getUrl()
	{
		return url;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	@Bindable
	public String getDisplayUrl()
	{
		if (url != null && url.startsWith(HTTP_PREFIX))
		{
			return url.substring(HTTP_PREFIX.length());
		}
		return url;
	}

	public void setMatch(Match match)
	{
		this.match = match;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("Action ");
		builder.append(name);
		builder.append(" (");
		boolean comma = false;
		for (String code : codes)
		{
			if (comma)
			{
				if (match == Match.all)
				{
					builder.append(" + ");
				}
				else if (match == Match.any)
				{
					builder.append(", ");
				}
				else if (match == Match.sequence)
				{
					builder.append(" -> ");
				}
			}
			else
			{
				comma = true;
			}

			builder.append(code);
		}
		builder.append(")");
		return builder.toString();
	}
}