package uk.ac.horizon.artcodes.scanner.overlay;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.detect.Marker;

import java.util.List;
import java.util.Map;

public class MarkerRegionLayer extends Layer
{
	@Override
	public void drawMarkers(Mat overlay, List<Marker> markers, List<MatOfPoint> contours)
	{
		for (Marker marker : markers)
		{
			for (Marker.MarkerDetails details : marker.getMarkerDetails())
			{
				if (regionColour != null)
				{
					// drawMarkers regions
					for (Map<String, Object> region : details.regions)
					{
						int currentRegionIndex = (Integer) region.get(Marker.MarkerDetails.REGION_INDEX);
						Imgproc.drawContours(overlay, contours, currentRegionIndex, outlineColour, 4);
						Imgproc.drawContours(overlay, contours, currentRegionIndex, regionColour, 2);
					}
				}

				if (detectedColour != null)
				{
					// drawMarkers marker outline
					Imgproc.drawContours(overlay, contours, details.markerIndex, outlineColour, 7);
					Imgproc.drawContours(overlay, contours, details.markerIndex, detectedColour, 5);
				}
			}
		}
	}

	@Override
	public int getIcon()
	{
		return R.drawable.ic_border_all_white_24dp;
	}

	@Override
	public Layer getNext()
	{
		return new MarkerNullLayer();
	}

	@Override
	public boolean hasOutput()
	{
		return true;
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_marker_regions;
	}
}
