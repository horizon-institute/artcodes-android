/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.scanner;

import android.util.Log;
import org.opencv.core.Mat;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.camera.FrameProcessor;
import uk.ac.horizon.artcodes.scanner.detect.Marker;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.scanner.detect.MarkerDetector;
import uk.ac.horizon.artcodes.scanner.overlay.Overlay;
import uk.ac.horizon.artcodes.scanner.process.ImageProcessor;
import uk.ac.horizon.artcodes.scanner.process.TileThresholder;

import java.util.List;

public class ExperienceFrameProcessor extends FrameProcessor
{
	private static MarkerDetector createMarkerDetector(Experience experience)
	{
		try
		{
			Class<?> clazz = Class.forName(experience.getDetector());
			return (MarkerDetector) clazz.newInstance();
		}
		catch (Exception e)
		{
			return new MarkerDetector();
		}
	}

	private final MarkerDetector detector;
	private final MarkerDetectionHandler handler;
	private final Overlay overlay;
	private final Experience experience;
	private boolean detected = false;

	public ExperienceFrameProcessor(Experience experience, MarkerDetectionHandler handler, Overlay overlay)
	{
		this.experience = experience;
		this.handler = handler;
		this.overlay = overlay;

		experience.update();
		Log.i("", "Regions " + experience.getMinRegions() + "-" + experience.getMaxRegions() + " using max of " + experience.getMaxRegionValue() + " and checksum of " + experience.getChecksumModulo());
		if (experience.getProcessors().isEmpty())
		{
			experience.getProcessors().add(new TileThresholder());
		}
		detector = createMarkerDetector(experience);
	}

	@Override
	public void process(Mat frame)
	{
		try
		{
			for (ImageProcessor imageProcessor : experience.getProcessors())
			{
				frame = imageProcessor.process(frame, detected);
			}

			if (overlay.hasOutput(frame))
			{
				rotate(frame, frame);
			}

			final List<Marker> markers = detector.findMarkers(frame, overlay, experience);
			detected = !markers.isEmpty();

			handler.onMarkersDetected(markers);
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage(), e);
		}

		// TODO Test if camera needs to be focused
//				if (CameraController.deviceNeedsManualAutoFocus && framesSinceLastMarker > 2 && System.currentTimeMillis() - timeOfLastAutoFocus >= 5000)
//				{
//					timeOfLastAutoFocus = System.currentTimeMillis();
//					camera.performManualAutoFocus(new Camera.AutoFocusCallback()
//					{
//						@Override
//						public void onAutoFocus(boolean b, Camera camera)
//						{
//						}
//					});
//				}
	}

	protected Mat crop(Mat image, int surfaceWidth, int surfaceHeight)
	{
		final int size = Math.min(image.width(), image.height());

		final int colStart = (image.width() - size) / 2;
		final int rowStart = (image.height() - size) / 2;

		float sizeRatio = Math.max(surfaceHeight, surfaceWidth) / Math.max(image.width(), image.height());
		int viewfinderSize = (int) Math.max(colStart * sizeRatio, rowStart * sizeRatio);
		Log.i("", "Size = " + size + ", Viewfinder size = " + viewfinderSize);
		overlay.setViewfinderSize(viewfinderSize);

		return image.submat(rowStart, rowStart + size, colStart, colStart + size);
	}
}