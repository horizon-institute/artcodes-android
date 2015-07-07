package uk.ac.horizon.artcodes.scanner.overlay;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import uk.ac.horizon.artcodes.scanner.R;

public class ThresholdNullLayer extends Layer
{
	@Override
	public int getIcon()
	{
		return R.drawable.ic_filter_b_and_w_off_white_24dp;
	}

	@Override
	public Layer getNext()
	{
		return new ThresholdLayer();
	}

	@Override
	void drawThreshold(Mat image, Mat overlay)
	{
		overlay.setTo(new Scalar(0, 0, 0));
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_threshold_off;
	}
}
