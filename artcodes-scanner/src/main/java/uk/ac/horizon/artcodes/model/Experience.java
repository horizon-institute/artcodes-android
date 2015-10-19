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
package uk.ac.horizon.artcodes.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.scanner.BR;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;

public class Experience extends BaseObservable
{
	private final List<Action> actions = new ArrayList<>();
	private final List<Availability> availabilities = new ArrayList<>();
	private final List<ImageProcessor> processors = new ArrayList<>();
	private final List<String> pipeline = new ArrayList<>();
	private String id;
	private String name;
	private String icon;
	private String image;
	private String description;
	private String author;
	private String callback;
	private String originalID;

	private int checksumModulo = 3;
	private boolean embeddedChecksum = false;
	private boolean editable = false;
	private String detector;

	public Experience()
	{
	}

	public List<Action> getActions()
	{
		return actions;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public List<Availability> getAvailabilities()
	{
		return availabilities;
	}

	public String getCallback()
	{
		return callback;
	}

	public void setCallback(String callback)
	{
		this.callback = callback;
	}

	@Bindable
	public int getChecksumModulo()
	{
		return checksumModulo;
	}

	public void setChecksumModulo(int checksumModulo)
	{
		this.checksumModulo = checksumModulo;
	}

	@Bindable
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<String> getPipeline()
	{
		return pipeline;
	}

	public String getDetector()
	{
		return detector;
	}

	public boolean getEmbeddedChecksum()
	{
		return embeddedChecksum;
	}

	public void setEmbeddedChecksum(boolean embeddedChecksum)
	{
		this.embeddedChecksum = embeddedChecksum;
	}

	@Bindable
	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
		notifyPropertyChanged(BR.icon);
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@Bindable
	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
		notifyPropertyChanged(BR.image);
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

	public String getOriginalID()
	{
		return originalID;
	}

	public void setOriginalID(String originalID)
	{
		this.originalID = originalID;
	}

	public boolean isSharable()
	{
		return id != null && (id.startsWith("http:") || id.startsWith("https:"));
	}

	public List<ImageProcessor> getProcessors()
	{
		return processors;
	}

	private boolean hasCode(String code, Action.Match... matches)
	{
		for (Action action : actions)
		{
			for (Action.Match match : matches)
			{
				if (match == action.getMatch())
				{
					if (action.getCodes().contains(code))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public enum Status
	{
		loaded, modified, saving
	}
}