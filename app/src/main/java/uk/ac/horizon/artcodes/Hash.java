package uk.ac.horizon.artcodes;

import java.util.HashMap;
import java.util.Map;

public class Hash
{
	public static Map<String,String> salts;
	static
	{
		salts = new HashMap<>();
		salts.put("timehash1", "42ad0d1dz581a8ed");
	}
}
