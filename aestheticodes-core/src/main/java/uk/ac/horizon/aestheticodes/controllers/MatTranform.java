/*
 * Aestheticodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.aestheticodes.controllers;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Arrays;

public class MatTranform
{
	static Mat crop(Mat imgMat)
	{
		final int size = Math.min(imgMat.rows(), imgMat.cols());

		final int colStart = (imgMat.cols() - size) / 2;
		final int rowStart = (imgMat.rows() - size) / 2;

		return imgMat.submat(rowStart, rowStart + size, colStart, colStart + size);
	}

	public static Mat cropNV12Data(byte[] data, int oRows, int oCols)
	{
		int size = Math.min(oRows,oCols);
		size -= size%2;

		int colStart = (oCols - size) / 2;
		int rowStart = (oRows - size) / 2;
		colStart -= colStart%2;
		rowStart -= rowStart%2;

		if (rowStart==0) // landscape
		{
			Mat o = new Mat(oRows+oRows/2, oCols, CvType.CV_8UC1);
			o.put(0,0, data);
			return o.submat(0, o.rows(), colStart, colStart + size);
		}
		else if (colStart==0) // portrait
		{
			int yStart = /*num of cells in a row*/oCols * /*num of rows to skip*/rowStart;
			int yEnd = yStart+size*size;
			int uvOffset = oRows * oCols;
			int uvStart = uvOffset + /*num of cells in a row*/oCols * /*num of rows to skip*/(rowStart/2);
			int uvEnd = uvStart+size*(size/2);
			Mat output = new Mat(size + size / 2, size, CvType.CV_8UC1);
			output.put(0, 0, Arrays.copyOfRange(data, yStart, yEnd));
			output.put(size, 0, Arrays.copyOfRange(data, uvStart, uvEnd));
			return output;
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public static void rotate(Mat src, Mat dst, int angle, boolean flip)
	{
		if (src != dst)
		{
			src.copyTo(dst);
		}

		angle = ((angle / 90) % 4) * 90;

		//0 : flip vertical; 1 flip horizontal

		int flip_horizontal_or_vertical = angle > 0 ? 1 : 0;
		if (flip)
		{
			flip_horizontal_or_vertical = -1;
		}
		int number = Math.abs(angle / 90);

		for (int i = 0; i != number; ++i)
		{
			Core.transpose(dst, dst);
			Core.flip(dst, dst, flip_horizontal_or_vertical);
		}
	}
}
