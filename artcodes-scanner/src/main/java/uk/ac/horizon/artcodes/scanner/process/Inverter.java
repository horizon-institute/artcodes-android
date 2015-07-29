package uk.ac.horizon.artcodes.scanner.process;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Inverter implements ImageProcessor
{
	@Override
	public Mat process(Mat image, boolean detected)
	{
		Core.bitwise_not(image, image);
		return image;
	}
}
