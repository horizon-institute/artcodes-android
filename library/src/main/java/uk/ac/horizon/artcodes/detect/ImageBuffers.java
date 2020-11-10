/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.detect;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ImageBuffers
{
	private byte[] buffer;

	/**
	 * cameraImage stores the full camera image in YUV (NV21, first 2/3 rows are Y data, final 1/3 rows is interleaved UV data)
	 */
	private Mat cameraImage;
	private Rect ROI;

	/**
	 * currentBuffer keeps track of which buffer contains the most recent result of processing
	 */
	private Mat currentBuffer;

	/**
	 * <p>yuvBuffer contains the area of the image to process in YUV (if ROI.height==height of the
	 * Y component this is a sub-Mat of cameraImage).</p>
	 * <p>Note YUV data is arranged in two panes: <ul><li>a Y pane (that is the image intensity
	 * [the same as cvtColor(BGR,GREY,COLOR_BGR2GRAY)]) one Y value per pixel, and</li><li>a VU
	 * pane, one VU pair per 4 pixels.</li></ul></p>
	 * <p>
	 * YYyyYYYY<br>
	 * YYyyYYYY<br>
	 * YYYYYYYY<br>
	 * YYYYYYYY<br>
	 * VUvuVUVU<br>
	 * VUVUVUVU
	 * </p>
	 * <p>Lower case letters show values that effect the same 4 pixels.</p>
	 */
	private Mat yuvBuffer;
	/**
	 * greyBuffer contains the area of the image to process as a single channel grey image (this is a sub-Mat of yuvBuffer)
	 */
	private Mat greyBuffer;
	/**
	 * bgrBuffer contains the area of the image to process as a 3 channel Blue-Green-Red image (this is not a sub-Mat and is only generated in getBgrBuffer() )
	 */
	private Mat bgrBuffer;

	private Mat overlay;
	private Bitmap overlayBitmap;
	private boolean overlayReady = false;
	private boolean detected = false;
	private boolean flip = false;
	private int rotations = 0;

	public void setImage(Mat image)
	{
		if (image == this.greyBuffer)
		{
			this.currentBuffer = greyBuffer;
		}
		else if (image == this.bgrBuffer)
		{
			this.currentBuffer = bgrBuffer;
		}
		else if (image == this.yuvBuffer)
		{
			this.currentBuffer = yuvBuffer;
		}
		else
		{
			Log.w(this.getClass().getSimpleName(), "setImage(Mat) called with buffer not provided by ImageBuffers.");
		}
	}

	public Mat getImageInAnyFormat()
	{
		if (currentBuffer == yuvBuffer || currentBuffer == greyBuffer)
		{
			return greyBuffer;
		}
		else if (currentBuffer == bgrBuffer)
		{
			return bgrBuffer;
		}
		else
		{
			Log.w(this.getClass().getSimpleName(), "In getImageInAnyFormat() 'currentBuffer' was not equal to any known buffer.");
			return null;
		}
	}

	public Mat getImageInGrey()
	{
		if (currentBuffer == yuvBuffer)
		{
			double[] rectArray = {0, 0, yuvBuffer.cols(), (yuvBuffer.rows() / 3) * 2};
			greyBuffer = yuvBuffer.submat(new Rect(rectArray));
		}
		else if (currentBuffer == bgrBuffer)
		{
			Imgproc.cvtColor(getBgrBuffer(), getGreyBuffer(), Imgproc.COLOR_BGR2GRAY);
		}
		else
		{
			getGreyBuffer();
		}
		currentBuffer = greyBuffer;
		return greyBuffer;
	}

	public Mat getGreyBuffer()
	{
		if (greyBuffer == null)
		{
			if (yuvBuffer != null)
			{
				double[] rectArray = {0, 0, yuvBuffer.cols(), (yuvBuffer.rows() / 3) * 2};
				greyBuffer = yuvBuffer.submat(new Rect(rectArray));
			}
			else
			{
				Log.w(getClass().getSimpleName(), "Creating grey buffer in getGreyBuffer()");
				greyBuffer = new Mat(ROI.height, ROI.width, CvType.CV_8UC1);
			}
		}
		return this.greyBuffer;
	}

	public Mat getImageInBgr()
	{
		if (currentBuffer == yuvBuffer)
		{
			Imgproc.cvtColor(yuvBuffer, getBgrBuffer(), Imgproc.COLOR_YUV2BGR_NV21);
		}
		else if (currentBuffer == greyBuffer)
		{
			Imgproc.cvtColor(greyBuffer, getBgrBuffer(), Imgproc.COLOR_GRAY2BGR);
		}
		currentBuffer = bgrBuffer;
		return bgrBuffer;
	}

	public Mat getBgrBuffer()
	{
		if (bgrBuffer == null)
		{
			bgrBuffer = new Mat(ROI.height, ROI.width, CvType.CV_8UC3);
		}
		return bgrBuffer;
	}

	public Mat getImageInYuv()
	{
		if (currentBuffer == greyBuffer)
		{
			Imgproc.cvtColor(greyBuffer, getBgrBuffer(), Imgproc.COLOR_GRAY2BGR);
			Imgproc.cvtColor(bgrBuffer, yuvBuffer, Imgproc.COLOR_BGR2YUV);
		}
		else if (currentBuffer == bgrBuffer)
		{
			Imgproc.cvtColor(bgrBuffer, yuvBuffer, Imgproc.COLOR_BGR2YUV);
		}
		currentBuffer = yuvBuffer;
		return yuvBuffer;
	}

	public void setImage(byte[] data)
	{
		overlayReady = false;
		cameraImage.put(0, 0, data);

		currentBuffer = yuvBuffer;
	}


	/**
	 * Set the image data using a Bitmap.
	 * Make sure the Bitmap is the right size for this ImageBuffers or if creating an ImageBuffers
	 * just for this call createBuffer(bitmap.getWidth(), bitmap.getHeight(), 8) then setROI(null).
	 *
	 * @param bitmap A Bitmap with format ARGB_8888 or RGB_565 (requirement from OpenCV).
	 */
	public void setImage(Bitmap bitmap)
	{
		overlayReady = false;
		// Utils.bitmapToMat creates a RGBA CV_8UC4 image.
		Mat rgbaImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
		Utils.bitmapToMat(bitmap, rgbaImage);
		// The 'bgrBuffer' is supposed to be BGR CV_8UC3, so convert.
		Mat bgrImage = this.getBgrBuffer();
		Imgproc.cvtColor(rgbaImage, bgrImage, Imgproc.COLOR_RGBA2BGR);
		this.setImage(bgrImage);
	}

	public byte[] createBuffer(int imageWidth, int imageHeight, int imageDepth)
	{
		Log.i("ImageBuffer", "createBuffer imageWidth: " + imageWidth + " imageHeight: " + imageHeight + " imageDepth: " + imageDepth);
		buffer = new byte[imageWidth * imageHeight * imageDepth / 8];
		cameraImage = new Mat(imageHeight + imageHeight / 2, imageWidth, CvType.CV_8UC1);
		Log.i("ImageBuffer", "cameraImage: " + cameraImage.cols() + "x" + cameraImage.rows() + "x" + cameraImage.channels());
		return buffer;
	}

	public void setROI(Rect rect)
	{
		this.ROI = rect;
		if (rect == null)
		{
			double[] roiArray = {0, 0, cameraImage.cols(), (cameraImage.rows() / 3) * 2};
			this.ROI = new Rect(roiArray);
			yuvBuffer = cameraImage;
			currentBuffer = yuvBuffer;
		}
		else
		{
			if (rect.height == (cameraImage.rows() / 3) * 2)
			{
				Log.i(this.getClass().getSimpleName(), "rect.height == camera image height (simple case)");

				double[] rectArray = {rect.x, rect.y, rect.width, rect.height + rect.height / 2};
				yuvBuffer = cameraImage.submat(new Rect(rectArray));
			}
			else
			{
				Log.i(this.getClass().getSimpleName(), "rect.height != camera image height (complicated case)");
				// TODO
				throw new UnsupportedOperationException();
			}
		}
	}

	public boolean hasDetected()
	{
		return detected;
	}

	public void setDetected(boolean detected)
	{
		this.detected = detected;
	}

	public Mat getOverlay()
	{
		return getOverlay(true);
	}

	public Bitmap createOverlayBitmap()
	{
		if (overlayReady)
		{
			if (overlayBitmap == null)
			{
				try
				{
					overlayBitmap = Bitmap.createBitmap(overlay.cols(), overlay.rows(), Bitmap.Config.ARGB_8888);
				}
				catch (OutOfMemoryError e)
				{
					Log.e("ImageBuffers", "Ignoring exception when creating overlay", e);
					return null;
				}
			}
			Utils.matToBitmap(overlay, overlayBitmap);
			return overlayBitmap;
		}
		else if (overlayBitmap != null)
		{
			overlayBitmap = null;
		}

		return null;
	}

	public void setRotation(int rotation)
	{
		this.rotations = rotation / 90;
	}

	public void setFrontFacing(boolean frontFacing)
	{
		this.flip = frontFacing;
	}

	public Mat getOverlay(boolean clear)
	{
		if (overlayReady)
		{
			return overlay;
		}

		if (currentBuffer == yuvBuffer)
		{
			this.setImage(this.getImageInGrey());
		}
		rotate(currentBuffer);

		if (overlay == null)
		{
			overlay = new Mat(currentBuffer.rows(), currentBuffer.cols(), CvType.CV_8UC4);
		}

		if (clear)
		{
			overlay.setTo(new Scalar(0, 0, 0, 0));
		}

		overlayReady = true;

		return overlay;
	}

	private void rotate(Mat image)
	{
		//0 : flip vertical; 1 flip horizontal
		int flip_horizontal_or_vertical = rotations > 0 ? 1 : 0;
		if (flip)
		{
			flip_horizontal_or_vertical = -1;
		}

		for (int i = 0; i != rotations; ++i)
		{
			Core.transpose(image, image);
			Core.flip(image, image, flip_horizontal_or_vertical);
		}
	}
}