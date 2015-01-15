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

package uk.ac.horizon.aestheticodes.controllers;

import android.util.Log;
import uk.ac.horizon.aestheticodes.model.Experience;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ExperienceListController
{
	public static interface Listener
	{
		void experienceListChanged();
	}

	private final Map<String, Experience> experiences = new HashMap<>();
	private final Collection<Listener> listeners = new HashSet<>();

	public void add(Experience experience)
	{
		if (experience == null || experience.getId() == null)
		{
			return;
		}
		Log.i("", "Adding " + experience.getId());
		synchronized (experiences)
		{
			experiences.put(experience.getId(), experience);
		}

		for (final Listener listener : listeners)
		{
			listener.experienceListChanged();
		}
	}

	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public Experience get(String id)
	{
		synchronized (experiences)
		{
			if (experiences.containsKey(id))
			{
				return experiences.get(id);
			}
		}
		return null;
	}

	public Collection<Experience> get()
	{
		synchronized (experiences)
		{
			return experiences.values();
		}
	}

	public void remove(String experienceID)
	{
		experiences.remove(experienceID);

		for (final Listener listener : listeners)
		{
			listener.experienceListChanged();
		}
	}

	public void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}
}

