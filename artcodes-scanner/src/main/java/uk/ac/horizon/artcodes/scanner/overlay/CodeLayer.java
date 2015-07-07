package uk.ac.horizon.artcodes.scanner.overlay;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.detect.Marker;

import java.util.List;

public class CodeLayer extends Layer
{
	@Override
	public void drawMarkers(Mat overlay, List<Marker> markers, List<MatOfPoint> contours)
	{
		for (Marker marker : markers)
		{
			for (Marker.MarkerDetails details : marker.getMarkerDetails())
			{
				Rect bounds = Imgproc.boundingRect(contours.get(details.markerIndex));
				String markerCode = marker.getCodeKey();

				Core.putText(overlay, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
				Core.putText(overlay, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
			}
		}
	}

	@Override
	public int getIcon()
	{
		return 0;
	}

	@Override
	public Layer getNext()
	{
		return new CodeNullLayer();
	}

	@Override
	public boolean hasOutput()
	{
		return true;
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_code;
	}
}
