package uk.ac.horizon.artcodes.scanner.detect;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class MarkerDetectionHandler
{
	protected static final int REQUIRED = 5;
	protected static final int MAX = REQUIRED * 4;
	private final Multiset<String> markerCounts = HashMultiset.create();

	public void onMarkersDetected(List<Marker> markers)
	{
		final Collection<String> removals = new HashSet<>(markerCounts.elementSet());

		for (Marker markerCode : markers)
		{
			final String marker = markerCode.getCodeKey();
			final int count = markerCounts.count(marker);
			if (count > MAX)
			{
				markerCounts.setCount(marker, MAX);
			}

			//increase occurrence if this marker is already in the list.
			markerCounts.add(marker);
			removals.remove(marker);
		}

		markerCounts.removeAll(removals);

		onMarkersDetected(markerCounts);
	}

	public abstract void onMarkersDetected(Multiset<String> markers);

	public void reset()
	{
		markerCounts.clear();
	}
}
