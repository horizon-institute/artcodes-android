/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.controller;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.horizon.artcodes.model.Experience;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;

public class ExperienceSaver
{
	public static void save(Context context, ExperienceListController experiences)
	{
		try
		{
			final File experienceFile = new File(context.getFilesDir(), "experiences.json");
			final FileWriter writer = new FileWriter(experienceFile);
			final Collection<Experience> saveExperiences = new ArrayList<>();
			for (Experience experience : experiences.get())
			{
				saveExperiences.add(experience);
			}
			Gson gson = new GsonBuilder().create();
			gson.toJson(saveExperiences, writer);

			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}
	}
}
