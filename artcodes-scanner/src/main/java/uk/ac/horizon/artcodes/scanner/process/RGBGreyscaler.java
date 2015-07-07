package uk.ac.horizon.artcodes.scanner.process;

import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class RGBGreyscaler extends Greyscaler
{
	private final int singleChannel;
	private byte[] singleChannelGreyPixelBuffer = null;

	public RGBGreyscaler(double hueShift, int channel, boolean invert)
	{
		super();
		Log.i(KEY, "Creating RGBGreyscaler");

		if (0 <= channel && channel <= 2)
		{
			this.singleChannel = channel;
		}
		else
		{
			Log.e(KEY, "RGBGreyscaler received channel index out of range (" + channel + "), using 0.");
			this.singleChannel = 0;
		}
	}

	@Override
	protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
	{
	        /*
            // This method is much simpler (and a little faster) but randomly crashes :(
            List<Mat> channels = new ArrayList<>(3);
            Core.split(colorImage, channels);
            return channels.get(this.singleChannel);
            */

		if (greyscaleImage == null || greyscaleImage.rows() != colorImage.rows() || greyscaleImage.cols() != colorImage.cols())
		{
			Log.i(KEY, "Creating new Mat buffer (3)");
			greyscaleImage = new Mat(colorImage.size(), CvType.CV_8UC1);
		}

		int desiredColorBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
		if (this.colorPixelBuffer == null || this.colorPixelBuffer.length < desiredColorBufferSize)
		{
			Log.i(KEY, "Creating new byte[" + desiredColorBufferSize + "] buffer (4)");
			this.colorPixelBuffer = new byte[desiredColorBufferSize];
		}

		int desiredGreyBufferSize = colorImage.rows() * colorImage.cols();
		if (this.singleChannelGreyPixelBuffer == null || this.singleChannelGreyPixelBuffer.length != desiredGreyBufferSize)
		{
			Log.i(KEY, "Creating new byte[" + desiredGreyBufferSize + "] buffer (5)");
			this.singleChannelGreyPixelBuffer = new byte[desiredGreyBufferSize];
		}

		colorImage.get(0, 0, this.colorPixelBuffer);
		int c = this.singleChannel, g = 0, channels = colorImage.channels();
		while (g < desiredGreyBufferSize)
		{
			this.singleChannelGreyPixelBuffer[g] = this.colorPixelBuffer[c];
			++g;
			c += channels;
		}
		greyscaleImage.put(0, 0, this.singleChannelGreyPixelBuffer);


		return greyscaleImage;
	}

	@Override
	protected boolean useIntensityShortcut()
	{
		return false;
	}
}
