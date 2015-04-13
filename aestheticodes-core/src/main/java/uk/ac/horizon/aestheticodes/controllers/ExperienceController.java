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

package uk.ac.horizon.aestheticodes.controllers;

import android.util.Log;
import com.google.gson.Gson;
import uk.ac.horizon.aestheticodes.model.Experience;

import java.util.Collection;
import java.util.HashSet;

public class ExperienceController
{
	public static interface Listener
	{
		void experienceSelected(Experience experience);
	}

	private final Collection<Listener> listeners = new HashSet<>();
	private Experience experience = new Experience();

	public ExperienceController()
	{
	}

	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public Experience get()
	{
		return experience;
	}

	public void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	public void set(final Experience experience)
	{
		this.experience = experience;
		Gson gson = ExperienceParser.createParser();
		Log.i("", "Set experience to " + gson.toJson(experience));

		for (final Listener listener : listeners)
		{
			listener.experienceSelected(experience);
		}
	}
}

