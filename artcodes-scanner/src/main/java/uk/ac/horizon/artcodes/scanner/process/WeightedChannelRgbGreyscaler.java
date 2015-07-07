package uk.ac.horizon.artcodes.scanner.process;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

// TODO: Investigate why this crashes when allocating the weight Mat.
public class WeightedChannelRgbGreyscaler extends Greyscaler
{
	private final Mat weight;

	public WeightedChannelRgbGreyscaler(double hueShift, double redMultiplier, double greenMultiplier, double blueMultiplier, boolean invert)
	{
		super();
		Log.i(KEY, "Creating WeightedChannelRgbGreyscaler");

		this.weight = new Mat(1, 3, CvType.CV_32FC1, new Scalar(0));
		this.weight.put(0, 0, blueMultiplier);
		this.weight.put(0, 1, greenMultiplier);
		this.weight.put(0, 2, redMultiplier);
	}

	@Override
	protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
	{
		if (greyscaleImage == null || greyscaleImage.rows() != colorImage.rows() || greyscaleImage.cols() != colorImage.cols())
		{
			greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
		}
		Core.transform(colorImage, greyscaleImage, this.weight);
		return greyscaleImage;
	}

	@Override
	protected boolean useIntensityShortcut()
	{
		return false;
	}
}