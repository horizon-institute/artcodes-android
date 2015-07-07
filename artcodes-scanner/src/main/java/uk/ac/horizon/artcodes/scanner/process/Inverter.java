package uk.ac.horizon.artcodes.scanner.process;

import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * Created by kevin on 07/07/2015.
 */
public class Inverter implements ImageProcessor
{
	@Override
	public Mat process(Mat image, boolean detected)
	{
		Core.bitwise_not(image, image);
		return image;
	}
}
