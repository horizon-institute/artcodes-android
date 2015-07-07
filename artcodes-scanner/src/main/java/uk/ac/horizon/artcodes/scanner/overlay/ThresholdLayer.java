package uk.ac.horizon.artcodes.scanner.overlay;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.artcodes.scanner.R;

public class ThresholdLayer extends Layer
{
	@Override
	public int getIcon()
	{
		return R.drawable.ic_filter_b_and_w_white_24dp;
	}

	@Override
	public Layer getNext()
	{
		return new ThresholdNullLayer();
	}

	@Override
	public boolean hasOutput()
	{
		return true;
	}

	@Override
	void drawThreshold(Mat image, Mat overlay)
	{
		Imgproc.cvtColor(image, overlay, Imgproc.COLOR_GRAY2BGRA);
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_threshold_on;
	}
}
