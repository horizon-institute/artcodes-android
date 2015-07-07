package uk.ac.horizon.artcodes.scanner;

import java.util.HashSet;
import java.util.Set;

public final class Feature
{
	private static final Set<String> features = new HashSet<>();

	public static boolean isEnabled(Enum<?> feature)
	{
		return features.contains(feature.name());
	}
}
