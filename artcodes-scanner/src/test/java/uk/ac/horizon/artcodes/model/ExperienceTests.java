package uk.ac.horizon.artcodes.model;

import org.junit.Test;

public class ExperienceTests
{
	@Test
	public void test()
	{
		Experience experience = new Experience();
		experience.setChecksumModulo(3);
		experience.setMinRegions(4);
		experience.setMaxRegions(4);
		experience.setMaxRegionValue(6);
	}
}
