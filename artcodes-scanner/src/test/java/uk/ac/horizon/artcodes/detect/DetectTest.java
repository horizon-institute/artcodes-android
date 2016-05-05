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

package uk.ac.horizon.artcodes.detect;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import uk.ac.horizon.artcodes.ExperienceParser;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class DetectTest
{
	@Test
	public void testImage() throws IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("test.json").getFile());

		Gson gson = ExperienceParser.createGson(null);

		Experience experience = gson.fromJson(new FileReader(file), Experience.class);


		ArtcodeDetector detector = new ArtcodeDetector(experience, new MarkerDetectionHandler()
		{
			@Override
			public void onMarkersDetected(Collection<String> markers)
			{

			}
		});


	}
}
