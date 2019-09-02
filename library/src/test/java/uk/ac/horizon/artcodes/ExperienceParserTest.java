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

package uk.ac.horizon.artcodes;

import android.annotation.SuppressLint;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceParserTest
{
	@SuppressLint("Assert")
	@Test
	public void testExperienceRoundTrip() throws IOException
	{
		final Experience experience = new Experience();
		final Action action = new Action();
		experience.getActions().add(action);

		final Gson gson = ExperienceParser.createGson(null);
		final String json1 = gson.toJson(experience);
		final Experience parsed = gson.fromJson(json1, Experience.class);
		final String json2 = gson.toJson(parsed);

		assert json1.equals(json2);
	}

	@Test
	public void loadingParseTest() throws IOException
	{
		Experience experience = TestUtils.loadExperience("test");
		Assert.assertTrue(experience.getName().equals("Test"));

		//Experience experience = new Experience();
		//experience.setChecksumModulo(3);
		// TODO
		//experience.setMinRegions(4);
		//experience.setMaxRegions(4);
		//experience.setMaxRegionValue(6);
	}

	@Test
	public void loadingOldParseTest() throws IOException
	{
		Experience experience = TestUtils.loadExperience("test_old");
		Assert.assertTrue(experience.getName().equals("Test"));
		final Gson gson = ExperienceParser.createGson(null);
		System.out.println(gson.toJson(experience));

		//Experience experience = new Experience();
		//experience.setChecksumModulo(3);
		// TODO
		//experience.setMinRegions(4);
		//experience.setMaxRegions(4);
		//experience.setMaxRegionValue(6);
	}
}