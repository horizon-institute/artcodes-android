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
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
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
import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.process.ImageProcessor;
import uk.ac.horizon.artcodes.process.ImageProcessorFactory;
import uk.ac.horizon.artcodes.scanner.R;

import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.leaf;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.nestedRegion;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.ok;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.tooFewRegions;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.tooManyDots;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.tooManyEmptyRegions;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.tooManyRegions;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.unknown;
import static uk.ac.horizon.artcodes.detect.marker.MarkerDetector.ContourStatus.wrongChecksum;


/**
 * This class detects standard Artcodes.
 */
public class MarkerDetector implements ImageProcessor
{

	public enum ContourStatus {
		unknown,
		leaf,
		tooManyEmptyRegions,
		tooManyRegions,
		tooFewRegions,
		tooManyDots,
		nestedRegion,
		wrongChecksum,
		ok

	}

	private Experience experience;

	private double diagonalScreenSize;

	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "detect";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
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
		none, marker, regions, debug;

		private static final OutlineDisplay[] vals = values();

		public OutlineDisplay next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	static final int NEXT_NODE = 0;
	static final int PREV_NODE = 1;
	static final int FIRST_NODE = 2;
	static final int PARENT_NODE = 3;
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
		this.experience = experience;
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
			diagonalScreenSize = Math.sqrt(Math.pow(buffers.getImageInAnyFormat().cols(), 2) + Math.pow(buffers.getImageInAnyFormat().rows(), 2));


			ContourStatus[] contourStatus = new ContourStatus[contours.size()];

			for (int i = 0; i < contours.size(); i++)
			{
				final Marker marker = createMarkerForNode(i, contours, hierarchy, contourStatus, i);
				if (marker != null)
				{
					final String markerCode = getCodeKey(marker);
					if (validCodes.isEmpty() || validCodes.contains(markerCode))
					{
						// If this marker has a minimum size set and is smaller: continue in loop.
						Action action = experience.getActionForCode(markerCode);
						if (!this.isMarkerValidForAction(marker, action, contours, hierarchy))
						{
							continue;
						}

						foundMarkers.add(marker);

						if (outlineDisplay != OutlineDisplay.none && outlineDisplay != OutlineDisplay.debug)
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
							String textToShow = markerCode;
							if (action!=null && action.getName()!=null)
							{
								textToShow += " ("+action.getName()+")";
							}
							Imgproc.putText(overlay, textToShow, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
							Imgproc.putText(overlay, textToShow, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
						}
					}
				}
			}

			// Draw debug:
			if (outlineDisplay == OutlineDisplay.debug)
			{
				Mat overlay = buffers.getOverlay();
				drawDebug(overlay, contours, hierarchy, contourStatus, foundMarkers);
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

	/**
	 * Override this method to check a detected Marker is valid for any extra options set on an
	 * Action. E.g. base class checks for minimum size requirements. Remember to && your result
	 * with super.isMarkerValidForAction.
	 * @param marker
	 * @param action
	 * @param contours
	 * @param hierarchy
	 * @return If the Marker is valid based on the options in Action.
	 */
	protected boolean isMarkerValidForAction(final Marker marker, final Action action, final ArrayList<MatOfPoint> contours, final Mat hierarchy)
	{
		// check if marker meets minimum size requirement set on action
		if (diagonalScreenSize > 0 && action != null && action.getMinimumSize() != null)
		{
			double minimumSize = action.getMinimumSize();
			RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(marker.markerIndex).toArray()));
			double markerSize = Math.sqrt(Math.pow(rotatedRect.size.width, 2.0) + Math.pow(rotatedRect.size.height, 2.0));
			boolean result = markerSize / diagonalScreenSize >= minimumSize;
			Log.i("MIN_MARKER_SIZE", "markerSize/diagonalScreenSize >= minimumSize = "+markerSize+"/"+diagonalScreenSize+" >= "+minimumSize+" = "+result);
			return result;
		}
		return true;
	}

	private void drawDebug(Mat overlay, ArrayList<MatOfPoint> contours, Mat hierarchy, ContourStatus[] contourStatuses, List<Marker> markers)
	{

		ArrayList<String> messages = new ArrayList<>();
		ArrayList<Scalar> messageColors = new ArrayList<>();

		Scalar wrongCodeColor = new Scalar(255, 0, 0, 255);
		Scalar rightCodeColor = new Scalar(0, 255, 0, 255);
		Scalar wrongCodeSoftColor = new Scalar(255, 127, 127, 255);
		Scalar tooManyDotsColor = new Scalar(0, 0, 255, 255);
		Scalar tooFewRegionsColor = new Scalar(255, 0, 255, 255);
		Scalar toManyRegionsColor = new Scalar(255, 100, 255, 255);
		Scalar nestedRegionColor = new Scalar(0, 100, 255, 255);
		Scalar wrongChecksumColor = new Scalar(0, 255, 255, 255);

		if (!markers.isEmpty())
		{
			for (Marker marker: markers)
			{
				Action action = this.experience.getActionForCode(marker.toString());
				if (action == null || !action.getName().contains("*")) {
					// wrong code found (hard fail)
					Imgproc.drawContours(overlay, contours, marker.markerIndex, rightCodeColor, -1, 8, hierarchy, 2, new Point(0,0)); // draw blobs
					Imgproc.drawContours(overlay, contours, marker.markerIndex, wrongCodeColor, -1, 8, hierarchy, 1, new Point(0,0));
					String message = "Wrong code found (HARD FAIL) "+marker.toString()+(action!=null?"/"+action.getName():"");
					if (!messages.contains(message)) {messages.add(message); messageColors.add(wrongCodeColor);}
				}
				else
				{
					// right code found!
					Imgproc.drawContours(overlay, contours, marker.markerIndex, rightCodeColor, -1, 8, hierarchy, 1, new Point(0,0));
					String message = "Right code found "+marker.toString()+(action!=null?"/"+action.getName():"");
					if (!messages.contains(message)) {messages.add(message); messageColors.add(rightCodeColor);}
				}
			}
		}
		else
		{
			for (int i=0; i<contours.size(); ++i) {

				int contourDepth = 0;
				int j = i;
				while (hierarchy.get(0, j)[PARENT_NODE] != -1)
				{
					++contourDepth;
					j = (int) hierarchy.get(0, j)[PARENT_NODE];
				}

				if (contourStatuses[i]==ok) {
					// mark found codes as ok or soft fail
					Marker marker = null;
					for (Marker m : markers)
					{
						if (m.markerIndex == i)
						{
							marker = m;
						}
					}
					Action action = this.experience.getActionForCode(marker!=null?marker.toString():"");
					if (action == null || !action.getName().contains("*")) {
						// wrong code found (soft fail)
						Imgproc.drawContours(overlay, contours, i, rightCodeColor, -1, 8, hierarchy, 2, new Point(0,0)); // draw blobs
						Imgproc.drawContours(overlay, contours, i, wrongCodeSoftColor, -1, 8, hierarchy, 1, new Point(0,0));
						String message = "Unknown code found (soft fail) " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(wrongCodeSoftColor);}
					}
					else
					{
						// right code found!
						Imgproc.drawContours(overlay, contours, i, rightCodeColor, -1, 8, hierarchy, 1, new Point(0,0));
						String message = "Right code found " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(rightCodeColor);}
					}
				}
				else if (contourDepth%2==1 && (contourStatuses[i]==tooManyDots || contourStatuses[i]==tooFewRegions || contourStatuses[i]==tooManyRegions || contourStatuses[i]==nestedRegion || contourStatuses[i]==wrongChecksum))
				{
					Scalar color = new Scalar(255, 255, 255, 255);

					if (contourStatuses[i]==tooManyDots)
					{
						color = tooManyDotsColor;
						String message = "Too many dots " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(tooManyDotsColor);}
					}
					else if (contourStatuses[i]==tooFewRegions)
					{
						color = tooFewRegionsColor;
						String message = "Too few regions " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(tooFewRegionsColor);}
					}
					else if (contourStatuses[i]==tooManyRegions)
					{
						color = toManyRegionsColor;
						String message = "Too many regions " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(toManyRegionsColor);}
					}
					else if (contourStatuses[i]==nestedRegion)
					{
						color = nestedRegionColor;
						String message = "Nested regions " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(nestedRegionColor);}
					}
					else if (contourStatuses[i]==wrongChecksum)
					{
						color = wrongChecksumColor;
						String message = "Wrong checksum " + getDebugCode(i,contours,hierarchy);
						if (!messages.contains(message)) {messages.add(message); messageColors.add(wrongChecksumColor);}
					}

					Imgproc.drawContours(overlay, contours, i, rightCodeColor, -1, 8, hierarchy, 2, new Point(0,0)); // draw blobs
					Imgproc.drawContours(overlay, contours, i, color, -1, 8, hierarchy, 1, new Point(0,0)); // draw marker
				}
			}
		}

		for (int i=0; i<messages.size(); ++i)
		{
			String text = messages.get(i);
			Imgproc.putText(overlay, text, new Point(15, 30*(i+1)), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
			Imgproc.putText(overlay, text, new Point(15, 30*(i+1)), Core.FONT_HERSHEY_SIMPLEX, 1, messageColors.get(i), 3);
		}
	}

	public String getDebugCode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy)
	{

		if (hierarchy.get(0, nodeIndex)[FIRST_NODE] == -1)
		{
			return "leaf";
		}
		else
		{
			int leafCount = 0;
			String code = "";
			for (int currentNodeIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE]; currentNodeIndex >= 0; currentNodeIndex = (int) hierarchy.get(0, currentNodeIndex)[NEXT_NODE])
			{
				String r = getDebugCode(currentNodeIndex, contours, hierarchy);
				if (r.equals("leaf"))
				{
					leafCount++;
				}
				else
				{
					code += (code.equals("") ? "" : ".") + r;
				}
			}

			if (code.equals(""))
			{
				return "" + leafCount;
			}
			else if (leafCount == 0)
			{
				return "[" + code + "]";
			}
			else
			{
				return leafCount + "[" + code + "]";
			}
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
					case debug:
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
					case debug:
						return R.string.draw_marker_debug;
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
		ContourStatus[] temp = new ContourStatus[1];
		return createMarkerForNode(nodeIndex, contours, hierarchy, temp, 0);
	}
	protected Marker createMarkerForNode(int nodeIndex, List<MatOfPoint> contours, Mat hierarchy, ContourStatus[] status, int statusIndex)
	{
		List<MarkerRegion> regions = null;
		for (int currentNodeIndex = (int) hierarchy.get(0, nodeIndex)[FIRST_NODE]; currentNodeIndex >= 0; currentNodeIndex = (int) hierarchy.get(0, currentNodeIndex)[NEXT_NODE])
		{
			final MarkerRegion region = createRegionForNode(currentNodeIndex, contours, hierarchy, status, statusIndex);
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
					status[statusIndex] = tooManyRegions;
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
			if (isValidRegionList(marker, status, statusIndex))
			{
				status[statusIndex] = ok;
				return marker;
			}
		}
		else
		{
			status[statusIndex] = leaf;
		}

		return null;
	}

	protected MarkerRegion createRegionForNode(int regionIndex, List<MatOfPoint> contours, Mat hierarchy)
	{
		ContourStatus[] temp = new ContourStatus[1];
		return createRegionForNode(regionIndex, contours, hierarchy, temp, 0);
	}
	protected MarkerRegion createRegionForNode(int regionIndex, List<MatOfPoint> contours, Mat hierarchy, ContourStatus[] status, int statusIndex)
	{
		// Find the first dot index:
		double[] nodes = hierarchy.get(0, regionIndex);
		int currentNodeIndex = (int) nodes[FIRST_NODE];
		if (currentNodeIndex < 0 && !(this.ignoreEmptyRegions || this.maxEmptyRegions > 0))
		{
			status[statusIndex] = tooManyEmptyRegions;
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
					status[statusIndex] = tooManyDots;
					return null;
				}
			}
			else
			{
				// Not a dot
				status[statusIndex] = nestedRegion;
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
	protected boolean isValidRegionList(Marker marker) {
		ContourStatus[] temp = new ContourStatus[1];
		return isValidRegionList(marker, temp, 0);
	}
	protected boolean isValidRegionList(Marker marker, ContourStatus[] status, int statusIndex)
	{
		if (marker.regions == null)
		{
			status[statusIndex] = unknown;
			return false; // No CodeDisplay
		}
		else if (marker.regions.size() < minRegions)
		{
			status[statusIndex] = tooFewRegions;
			return false; // Too Short
		}
		else if (marker.regions.size() > maxRegions)
		{
			status[statusIndex] = tooManyRegions;
			return false; // Too long
		}

		int numberOfEmptyRegions = ignoreEmptyRegions ? -1000 : 0;
		for (MarkerRegion region : marker.regions)
		{
			//check if leaves are using in accepted range.
			if (region.value > maxRegionValue)
			{
				status[statusIndex] = tooManyDots;
				return false; // value is too Big
			}
			else if (region.value == 0 && ++numberOfEmptyRegions > this.maxEmptyRegions)
			{
				return false; // too many empty regions
			}
		}

		return hasValidChecksum(marker, status, statusIndex);
	}

	/**
	 * This function divides the total number of leaves in the marker by the
	 * value given in the checksum preference. CodeDisplay is valid if the modulo is 0.
	 *
	 * @return true if the number of leaves are divisible by the checksum value
	 * otherwise false.
	 */
	protected boolean hasValidChecksum(Marker marker) {
		ContourStatus[] temp = new ContourStatus[1];
		return hasValidChecksum(marker, temp, 0);
	}
	protected boolean hasValidChecksum(Marker marker, ContourStatus[] status, int statusIndex)
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

		if ((numberOfLeaves % checksum) == 0)
		{
			return true;
		}
		else
		{
			status[statusIndex] = wrongChecksum;
			return false;
		}
	}
}
