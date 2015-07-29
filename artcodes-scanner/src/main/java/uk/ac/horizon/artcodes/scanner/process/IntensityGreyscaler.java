package uk.ac.horizon.artcodes.scanner.process;

import android.util.Log;
import org.opencv.core.Mat;

public class IntensityGreyscaler implements ImageProcessor
{
	public IntensityGreyscaler()
	{
		Log.i("", "Creating IntensityGreyscaler");
	}

	@Override
	public Mat process(Mat image, boolean detected)
	{
//		if (image == null || image.rows() != colorImage.rows() || image.cols() != colorImage.cols())
//		{
//			image = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
//		}
//		Imgproc.cvtColor(colorImage, image, Imgproc.COLOR_BGR2GRAY);
		return image;
	}
}
