package uk.ac.horizon.artcodes.json;

import org.junit.Test;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class JsonTest
{
	@Test
	public void testExperienceRoundTrip() throws IOException
	{
		final Experience experience = new Experience();
		final Action action = new Action();
		experience.getActions().add(action);

		final JsonParser<Experience> parser = new ExperienceParserFactory(null).parserFor(Experience.class);
		final String json1 = parser.toJson(experience);
		final Experience parsed = parser.parse(json1);
		final String json2 = parser.toJson(parsed);

		assert json1.equals(json2);
	}

	@Test
	public void testParseOld() throws IOException
	{
		URL url = getClass().getResource("test_old.json");
		assert url != null;

		final JsonParser<Experience> parser = new ExperienceParserFactory(null).parserFor(Experience.class);
		Experience experience = parser.parse(new FileReader(new File(url.getFile())));

		assert experience.getName().equals("Test");
		assert !experience.getActions().isEmpty();
	}

	@Test
	public void testParser() throws IOException
	{
		URL url = getClass().getResource("test.json");
		assert url != null;

		final JsonParser<Experience> parser = new ExperienceParserFactory(null).parserFor(Experience.class);
		Experience experience = parser.parse(new FileReader(new File(url.getFile())));

		assert experience.getName().equals("Test");
		assert !experience.getActions().isEmpty();
	}

	@Test
	public void testThresholderParse()
	{
		ImageProcessor imageProcessor = new TileThresholder();

		final JsonParser<ImageProcessor> parser = new ExperienceParserFactory(null).parserFor(ImageProcessor.class);

		String json = parser.toJson(imageProcessor);

		ImageProcessor parsed = parser.parse(json);
		assert parsed != null;
		assert parsed.getClass().equals(TileThresholder.class);
	}
}