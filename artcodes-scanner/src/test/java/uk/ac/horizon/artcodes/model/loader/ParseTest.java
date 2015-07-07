package uk.ac.horizon.artcodes.model.loader;

import com.google.gson.Gson;
import org.junit.Test;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class ParseTest
{

	@Test
	public void testExperienceRoundTrip() throws IOException
	{
		Experience experience = new Experience();

		Action action = new Action();
		experience.getActions().add(action);

		Gson gson = ExperienceLoader.createParser();
		String json1 = gson.toJson(experience);

		Experience parsed = gson.fromJson(json1, Experience.class);
		String json2 = gson.toJson(parsed);

		assert json1.equals(json2);
	}

	@Test
	public void testParseOld() throws IOException
	{
		URL url = getClass().getResource("test_old.json");
		assert url != null;

		Gson gson = ExperienceLoader.createParser();

		Experience experience = gson.fromJson(new FileReader(new File(url.getFile())), Experience.class);

		assert experience.getName().equals("Test");
		assert !experience.getActions().isEmpty();
	}

	@Test
	public void testParser() throws IOException
	{
		URL url = getClass().getResource("test.json");
		assert url != null;

		Gson gson = ExperienceLoader.createParser();

		Experience experience = gson.fromJson(new FileReader(new File(url.getFile())), Experience.class);

		assert experience.getName().equals("Test");
		assert !experience.getActions().isEmpty();
	}

	@Test
	public void testThresholderParse()
	{
		ImageProcessor imageProcessor = new TileThresholder();

		Gson gson = ExperienceLoader.createParser();

		String json = gson.toJson(imageProcessor, ImageProcessor.class);

		ImageProcessor parsed = gson.fromJson(json, ImageProcessor.class);
		assert parsed != null;
		assert parsed.getClass().equals(TileThresholder.class);
	}
}
