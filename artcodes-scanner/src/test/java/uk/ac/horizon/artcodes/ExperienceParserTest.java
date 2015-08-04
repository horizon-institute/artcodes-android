package uk.ac.horizon.artcodes;

import com.google.gson.Gson;
import org.junit.Test;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

import java.io.IOException;

public class ExperienceParserTest
{
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
	public void testParseOld() throws IOException
	{
//		URL url = getClass().getResource("test_old.json");
//		assert url != null;
//
//		final JsonParser<Experience> parser = new ExperienceParserFactory(null).parserFor(Experience.class);
//		Experience experience = parser.parse(new FileReader(new File(url.getFile())));
//
//		assert experience.getName().equals("Test");
//		assert !experience.getActions().isEmpty();
	}

	@Test
	public void testParser() throws IOException
	{
//		URL url = getClass().getResource("test.json");
//		assert url != null;
//
//		final JsonParser<Experience> parser = new ExperienceParserFactory(null).parserFor(Experience.class);
//		Experience experience = parser.parse(new FileReader(new File(url.getFile())));
//
//		assert experience.getName().equals("Test");
//		assert !experience.getActions().isEmpty();
	}

	@Test
	public void testThresholderExperienceParse()
	{
		Experience experience = new Experience();
		experience.getProcessors().clear();
		experience.getProcessors().add(new TileThresholder());

		final Gson gson = ExperienceParser.createGson(null);

		String json = gson.toJson(experience);
		System.out.println(json);

		Experience parsed = gson.fromJson(json, Experience.class);
		assert parsed != null;
		assert parsed.getProcessors().get(0).getClass().equals(TileThresholder.class);
	}
}