/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
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

package uk.ac.horizon.artcodes.scanner.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

@SuppressWarnings("deprecation")
public abstract class FrameProcessor implements Camera.PreviewCallback
{
	private byte[] buffer;
	private Mat image;
	private Mat croppedImage;
	private boolean flip = false;
	private int rotations = 0;

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		image.put(0, 0, data);
		process(croppedImage);
		camera.addCallbackBuffer(buffer);
	}

	public abstract void process(Mat image);

	protected Mat crop(Mat image, int surfaceWidth, int surfaceHeight)
	{
		return image;
	}

	protected void onCameraChanged(Camera camera, int surfaceWidth, int surfaceHeight)
	{
		final Camera.Parameters params = camera.getParameters();
		final int imageWidth = params.getPreviewSize().width;
		final int imageHeight = params.getPreviewSize().height;

		final int size = imageWidth * imageHeight * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
		buffer = new byte[size];
		image = new Mat(imageHeight, imageWidth, CvType.CV_8UC1);
		croppedImage = crop(image, surfaceWidth, surfaceHeight);

		camera.addCallbackBuffer(buffer);
		camera.setPreviewCallbackWithBuffer(this);
	}

	protected void rotate(Mat src, Mat dst)
	{
		if (src != dst)
		{
			src.copyTo(dst);
		}

		//0 : flip vertical; 1 flip horizontal
		int flip_horizontal_or_vertical = rotations > 0 ? 1 : 0;
		if (flip)
		{
			flip_horizontal_or_vertical = -1;
		}

		for (int i = 0; i != rotations; ++i)
		{
			Core.transpose(dst, dst);
			Core.flip(dst, dst, flip_horizontal_or_vertical);
		}
	}

	void setFacing(int facing)
	{
		this.flip = facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	void setOrientation(int orientation)
	{
		this.rotations = orientation / 90;
	}
}
