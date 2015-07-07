package uk.ac.horizon.artcodes.scanner.process;

import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class IntensityGreyscaler extends Greyscaler
{
	public IntensityGreyscaler(double hueShift, boolean invert)
	{
		super();
		Log.i(KEY, "Creating IntensityGreyscaler");
	}

	@Override
	protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
	{
		if (greyscaleImage == null || greyscaleImage.rows() != colorImage.rows() || greyscaleImage.cols() != colorImage.cols())
		{
			greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
		}
		Imgproc.cvtColor(colorImage, greyscaleImage, Imgproc.COLOR_BGR2GRAY);
		return greyscaleImage;
	}

	@Override
	protected boolean useIntensityShortcut()
	{
		return true;
	}
}
