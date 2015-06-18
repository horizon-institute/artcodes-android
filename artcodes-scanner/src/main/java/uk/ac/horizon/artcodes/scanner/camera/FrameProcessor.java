/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.scanner.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public abstract class FrameProcessor implements Camera.PreviewCallback
{
	protected static Mat crop(Mat imgMat)
	{
		final int size = Math.min(imgMat.rows(), imgMat.cols());

		final int colStart = (imgMat.cols() - size) / 2;
		final int rowStart = (imgMat.rows() - size) / 2;

		return imgMat.submat(rowStart, rowStart + size, colStart, colStart + size);
	}

	private byte[] buffer;
	private Mat image;
	private boolean flip = false;
	private int rotation = 0;

	void startProcessing(Camera camera)
	{
		Camera.Parameters params = camera.getParameters();
		int imageWidth = params.getPreviewSize().width;
		int imageHeight = params.getPreviewSize().height;

		int size = imageWidth * imageHeight;
		size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
		buffer = new byte[size];
		image = new Mat(imageHeight, imageWidth, CvType.CV_8UC1);

		//int rotation = 360 + 90 - camera.getRotation();
		//int angle = ((rotation / 90) % 4) * 90;
		//boolean flip = camera.isFront();
		rotation = 0;
		flip = false;

		camera.addCallbackBuffer(buffer);
		camera.setPreviewCallbackWithBuffer(this);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		image.put(0, 0, data);
		processFrame(image);
		camera.addCallbackBuffer(buffer);
	}

	public abstract void processFrame(Mat image);

	protected void rotate(Mat src, Mat dst)
	{
		if (src != dst)
		{
			src.copyTo(dst);
		}

		//0 : flip vertical; 1 flip horizontal
		int flip_horizontal_or_vertical = rotation > 0 ? 1 : 0;
		if (flip)
		{
			flip_horizontal_or_vertical = -1;
		}
		int number = Math.abs(rotation / 90);

		for (int i = 0; i != number; ++i)
		{
			Core.transpose(dst, dst);
			Core.flip(dst, dst, flip_horizontal_or_vertical);
		}
	}
}
