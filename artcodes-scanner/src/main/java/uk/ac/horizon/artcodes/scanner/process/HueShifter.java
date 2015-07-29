package uk.ac.horizon.artcodes.scanner.process;

import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class HueShifter implements ImageProcessor
{
	private int hueShift;

	private transient Mat threeChannelBuffer = null;
	private transient byte[] colorPixelBuffer = null;

	public HueShifter()
	{
	}

	public HueShifter(int hueShift)
	{
		this.hueShift = hueShift;
	}

	public int getHueShift()
	{
		return hueShift;
	}

	public void setHueShift(int hueShift)
	{
		this.hueShift = hueShift;
	}

	@Override
	public Mat process(Mat image, boolean detected)
	{
		int desiredRows = (image.rows() / 3) * 2, desiredCols = image.cols();
		if (threeChannelBuffer == null || threeChannelBuffer.rows() != desiredRows || threeChannelBuffer.cols() != desiredCols)
		{
			Log.i("", "Creating new Mat buffer (1)");
			threeChannelBuffer = new Mat(desiredRows, desiredCols, CvType.CV_8UC3);
		}
		Imgproc.cvtColor(image, threeChannelBuffer, Imgproc.COLOR_YUV2BGR_NV21);
		this.justHueShiftImage(this.threeChannelBuffer, this.threeChannelBuffer);
		return threeChannelBuffer;
	}

	/**
	 * Shift the hue of an image.
	 *
	 * @param colorImage  A BGR image (CV_8UC3).
	 * @param resultImage A BGR image (CV_8UC3) where the result will be stored, can be the same object as colorImage.
	 */
	protected void justHueShiftImage(Mat colorImage, Mat resultImage)
	{
		if (this.hueShift != 0)
		{
			Imgproc.cvtColor(colorImage, resultImage, Imgproc.COLOR_BGR2HLS);

			int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
			if (this.colorPixelBuffer == null || this.colorPixelBuffer.length < desiredBufferSize)
			{
				Log.i("", "Creating new byte[" + desiredBufferSize + "] buffer (2)");
				this.colorPixelBuffer = new byte[desiredBufferSize];
			}

			colorImage.get(0, 0, this.colorPixelBuffer);
			for (int i = 0; i < this.colorPixelBuffer.length; i += colorImage.channels())
			{
				this.colorPixelBuffer[i] = (byte) ((this.colorPixelBuffer[i] + this.hueShift) % 181);
			}
			colorImage.put(0, 0, this.colorPixelBuffer);

			// convert back to BGR
			Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_HLS2BGR);
		}
	}
}
