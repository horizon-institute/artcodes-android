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

package uk.ac.horizon.artcodes.detect.marker;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;
import uk.ac.horizon.artcodes.scanner.R;

/**
 * This class detects standard Artcodes.
 */
public class MarkerDetector implements ImageProcessor
{

	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "detect";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, Map<String, String> args)
		{
			return new MarkerDetector(experience, handler);
		}
	}

	private enum CodeDisplay
	{
		hidden, visible;

		private static final CodeDisplay[] vals = values();

		public CodeDisplay next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	private enum OutlineDisplay
	{
		none, marker, regions;

		private static final OutlineDisplay[] vals = values();

		public OutlineDisplay next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	static final int NEXT_NODE = 0;
	static final int FIRST_NODE = 2;
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private static final Scalar regionColour = new Scalar(255, 128, 0, 255);
	private static final Scalar outlineColour = new Scalar(0, 0, 0, 255);
	protected final int checksum;
	protected final Collection<String> validCodes = new HashSet<>();
	protected final int minRegions;
	protected final int maxRegions;
	protected final int maxRegionValue;
	protected final int maxEmptyRegions;
	protected final boolean ignoreEmptyRegions;

	private final MarkerDetectionHandler handler;

	private CodeDisplay codeDisplay = CodeDisplay.hidden;
	private OutlineDisplay outlineDisplay = OutlineDisplay.none;

	public MarkerDetector(Experience experience, MarkerDetectionHandler handler)
	{
		int maxValue = 3;
		int minRegionCount = 20;
		int maxRegionCount = 3;
		int checksum = 0;
		int maxEmptyRegions = 0;
		for (Action action : experience.getActions())
		{
			for (String code : action.getCodes())
			{
				int total = 0;
				String[] values = code.split(":");
				minRegionCount = Math.min(minRegionCount, values.length);
				maxRegionCount = Math.max(maxRegionCount, values.length);
				int emptyRegions = 0;
				for (String value : values)
				{
					try
					{
						int codeValue = Integer.parseInt(value);
						maxValue = Math.max(maxValue, codeValue);
						total += codeValue;
						if (codeValue == 0)
						{
							++emptyRegions;
						}
					}
					catch (Exception e)
					{
						Log.w("", e.getMessage(), e);
					}
				}
				maxEmptyRegions = Math.max(maxEmptyRegions, emptyRegions);

				if (total > 0)
				{
					checksum = gcd(checksum, total);
				}

				validCodes.add(code);
			}
		}

		this.handler = handler;

		if (minRegionCount == 20 && maxRegionCount == 3)
		{
			minRegionCount = 3;
			maxRegionCount = 20;
			maxValue = 20;
		}

		this.maxRegionValue = maxValue;
		this.minRegions = minRegionCount;
		this.maxRegions = maxRegionCount;
		this.checksum = checksum;
		this.maxEmptyRegions = maxEmptyRegions;
		this.ignoreEmptyRegions = maxEmptyRegions == 0;
		Log.i("detect", "Regions " + minRegionCount + "-" + maxRegionCount + ", <" + maxValue + ", checksum " + checksum);
	}

	private static int gcd(int a, int b)
	{
		if (b == 0)
		{
			return a;
		}
		return gcd(b, a % b);
	}

	@Override
	public void process(ImageBuffers buffers)
	{
		final ArrayList<MatOfPoint> contours = new ArrayList<>();
		final Mat hierarchy = new Mat();
		// Make sure the image is rotated before the contours are generated, if necessary
		//if (Feature.get(this.context, R.bool.feature_combined_markers).isEnabled() || outlineDisplay != OutlineDisplay.none || codeDisplay == CodeDisplay.visible)
		//{
		// if statement commented out as there was a bug where the overlay was not getting cleared
		// after cycling through the threshold views.
		buffers.getOverlay();
		//}
		try
		{
			final List<Marker> foundMarkers = new ArrayList<>();
			Imgproc.findContours(buffers.getImageInGrey(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
			for (int i = 0; i < contours.size(); i++)
			{
				final Marker marker = createMarkerForNode(i, contours, hierarchy);
				if (marker != null)
				{
					final String markerCode = getCodeKey(marker);
					if (validCodes.isEmpty() || validCodes.contains(markerCode))
					{
						foundMarkers.add(marker);

						if (outlineDisplay != OutlineDisplay.none)
						{
							Mat overlay = buffers.getOverlay();
							if (outlineDisplay == OutlineDisplay.regions)
							{
								double[] nodes = hierarchy.get(0, i);
								int currentRegionIndex = (int) nodes[FIRST_NODE];

								while (currentRegionIndex >= 0)
								{
									Imgproc.drawContours(overlay, contours, currentRegionIndex, outlineColour, 4);
									Imgproc.drawContours(overlay, contours, currentRegionIndex, regionColour, 2);

									nodes = hierarchy.get(0, currentRegionIndex);
									currentRegionIndex = (int) nodes[NEXT_NODE];
								}
							}

							Imgproc.drawContours(overlay, contours, i, outlineColour, 7);
							Imgproc.drawContours(overlay, contours, i, detectedColour, 5);
						}

						if (codeDisplay == CodeDisplay.visible)
						{
							Mat overlay = buffers.getOverlay();
							Rect bounds = Imgproc.boundingRect(contours.get(i));
							Imgproc.putText(overlay, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
							Imgproc.putText(overlay, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
						}
					}
				}
			}

			buffers.setDetected(!foundMarkers.isEmpty());
			handler.onMarkersDetected(foundMarkers, contours, hierarchy, buffers.getImageInGrey().size());
		}
		finally
		{
			contours.clear();
			hierarchy.release();
		}
	}

	public String getCodeKey(Marker marker)
	{
		sortCode(marker);
		StringBuilder builder = new StringBuilder(marker.regions.size() * 2);
		for (MarkerRegion region : marker.regions)
		{
			builder.append(region.value);
			builder.append(':');
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}

	@Override
	public void getSettings(List<DetectorSetting> settings)
	{
		settings.add(new DetectorSetting()
		{
			@Override
			public void nextValue()
			{
				outlineDisplay = outlineDisplay.next();
			}

			@Override
			public int getIcon()
			{
				switch (outlineDisplay)
				{
					case none:
						return R.drawable.ic_border_clear_24dp;
					case marker:
						return R.drawable.ic_border_outer_24dp;
					case regions:
						return R.drawable.ic_border_all_24dp;
				}

				return 0;
			}

			@Override
			public int getText()
			{
				switch (outlineDisplay)
				{
					case none:
						return R.string.draw_marker_off;
					case marker:
						return R.string.draw_marker_outline;
					case regions:
						return R.string.draw_marker_regions;
				}
				return 0;
			}
		});
		settings.add(new DetectorSetting()
		{
			@Override
			public void nextValue()
			{
				codeDisplay = codeDisplay.next();
			}

			@Override
			public int getIcon()
			{
				switch (codeDisplay)
				{
					case hidden:
						return R.drawable.ic_filter_none_black_24dp;
					case visible:
						return R.drawable.ic_filter_1_black_24dp;
				}
				return 0;
			}

			@Override
			public int getText()
			{
				switch (codeDisplay)
				{
					case hidden:
						return R.string.draw_code_off;
					case visible:
						return R.string.draw_code;
				}

				return 0;
			}
		});
	}

	protected boolean isValidDot(int nodeIndex, Mat hierarchy)
	{
		double[] nodes = hierarchy.get(0, nodeIndex);
		return nodes[FIRST_NODE] < 0;
	}

	protected Marker createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy)
	{
		List<MarkerRegion> regions = null;
		for (int currentNodeIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE]; currentNodeIndex >= 0; currentNodeIndex = (int) hierarchy.get(0, currentNodeIndex)[NEXT_NODE])
		{
			final MarkerRegion region = createRegionForNode(currentNodeIndex, contours, hierarchy);
			if (region != null)
			{
				if (this.ignoreEmptyRegions && region.value == 0)
				{
					continue;
				}
				else if (regions == null)
				{
					regions = new ArrayList<>();
				}
				else if (regions.size() >= maxRegions)
				{
					return null;
				}

				regions.add(region);
			}
			else
			{
				return null;
			}
		}

		if (regions != null)
		{
			Marker marker = new Marker(nodeIndex, regions);
			sortCode(marker);
			if (isValidRegionList(marker))
			{
				return marker;
			}
		}

		return null;
	}

	protected MarkerRegion createRegionForNode(int regionIndex, List<MatOfPoint> contours, Mat hierarchy)
	{
		// Find the first dot index:
		double[] nodes = hierarchy.get(0, regionIndex);
		int currentNodeIndex = (int) nodes[FIRST_NODE];
		if (currentNodeIndex < 0 && !(this.ignoreEmptyRegions || this.maxEmptyRegions > 0))
		{
			return null; // There are no dots in this region, and empty regions are not allowed.
		}

		// Count all the dots and check if they are leaf nodes in the hierarchy:
		int dotCount = 0;
		while (currentNodeIndex >= 0)
		{
			if (isValidDot(currentNodeIndex, hierarchy))
			{
				dotCount++;
				// Get next dot node:
				nodes = hierarchy.get(0, currentNodeIndex);
				currentNodeIndex = (int) nodes[NEXT_NODE];

				if (dotCount > maxRegionValue)
				{
					// Too many dots
					return null;
				}
			}
			else
			{
				// Not a dot
				return null;
			}
		}

		return new MarkerRegion(regionIndex, dotCount);
	}

	/**
	 * Override this method to change the sorted order of the code.
	 */
	protected void sortCode(Marker marker)
	{
		Collections.sort(marker.regions, new Comparator<MarkerRegion>()
		{
			@Override
			public int compare(MarkerRegion region1, MarkerRegion region2)
			{
				return region1.value < region2.value ? -1 : (region1.value == region2.value ? 0 : 1);
			}
		});
	}

	/**
	 * Override this method to change validation method.
	 */
	protected boolean isValidRegionList(Marker marker)
	{
		if (marker.regions == null)
		{
			return false; // No CodeDisplay
		}
		else if (marker.regions.size() < minRegions)
		{
			return false; // Too Short
		}
		else if (marker.regions.size() > maxRegions)
		{
			return false; // Too long
		}

		int numberOfEmptyRegions = 0;
		for (MarkerRegion region : marker.regions)
		{
			//check if leaves are using in accepted range.
			if (region.value > maxRegionValue)
			{
				return false; // value is too Big
			}
			else if (region.value == 0 && ++numberOfEmptyRegions > this.maxEmptyRegions)
			{
				return false; // too many empty regions
			}
		}

		return hasValidChecksum(marker);
	}

	/**
	 * This function divides the total number of leaves in the marker by the
	 * value given in the checksum preference. CodeDisplay is valid if the modulo is 0.
	 *
	 * @return true if the number of leaves are divisible by the checksum value
	 * otherwise false.
	 */
	protected boolean hasValidChecksum(Marker marker)
	{
		if (checksum <= 1)
		{
			return true;
		}
		int numberOfLeaves = 0;
		for (MarkerRegion region : marker.regions)
		{
			numberOfLeaves += region.value;
		}
		return (numberOfLeaves % checksum) == 0;
	}
}
