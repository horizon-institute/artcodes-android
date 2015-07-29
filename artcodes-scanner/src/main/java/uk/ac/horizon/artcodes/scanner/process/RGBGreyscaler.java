package uk.ac.horizon.artcodes.scanner.process;

import org.opencv.core.Mat;

public class RGBGreyscaler implements ImageProcessor
{
	public enum Channel
	{
		red, green, blue
	}

	private final Channel channel;
	private byte[] singleChannelGreyPixelBuffer = null;

	public RGBGreyscaler(Channel channel)
	{
		this.channel = channel;
	}

	@Override
	public Mat process(Mat image, boolean detected)
	{
//	        /*
//	        // This method is much simpler (and a little faster) but randomly crashes :(
//            List<Mat> channels = new ArrayList<>(3);
//            Core.split(colorImage, channels);
//            return channels.get(this.singleChannel);
//            */
//
//		if (image == null || image.rows() != colorImage.rows() || image.cols() != colorImage.cols())
//		{
//			Log.i(KEY, "Creating new Mat buffer (3)");
//			image = new Mat(colorImage.size(), CvType.CV_8UC1);
//		}
//
//		Mat colorImage = image;
//
//		int desiredColorBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
//		if (this.colorPixelBuffer == null || this.colorPixelBuffer.length < desiredColorBufferSize)
//		{
//			Log.i("", "Creating new byte[" + desiredColorBufferSize + "] buffer (4)");
//			this.colorPixelBuffer = new byte[desiredColorBufferSize];
//		}
//
//		int desiredGreyBufferSize = colorImage.rows() * colorImage.cols();
//		if (this.singleChannelGreyPixelBuffer == null || this.singleChannelGreyPixelBuffer.length != desiredGreyBufferSize)
//		{
//			Log.i("", "Creating new byte[" + desiredGreyBufferSize + "] buffer (5)");
//			this.singleChannelGreyPixelBuffer = new byte[desiredGreyBufferSize];
//		}
//
//		colorImage.get(0, 0, this.colorPixelBuffer);
//		int c = this.singleChannel, g = 0, channels = colorImage.channels();
//		while (g < desiredGreyBufferSize)
//		{
//			this.singleChannelGreyPixelBuffer[g] = this.colorPixelBuffer[c];
//			++g;
//			c += channels;
//		}
//		image.put(0, 0, this.singleChannelGreyPixelBuffer);


		return image;
	}
}
