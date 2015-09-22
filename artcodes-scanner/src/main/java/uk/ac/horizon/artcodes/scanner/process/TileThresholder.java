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

package uk.ac.horizon.artcodes.scanner.process;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class TileThresholder implements ImageProcessor
{
	private transient int tiles = 1;

	@Override
	public Mat process(Mat image, boolean detected)
	{
		Imgproc.GaussianBlur(image, image, new Size(5, 5), 0);

		if (!detected)
		{
			tiles = (tiles % 9) + 1;
		}
		final int tileHeight = (int) image.size().height / tiles;
		final int tileWidth = (int) image.size().width / tiles;

		// Split image into tiles and apply process on each image tile separately.
		for (int tileRow = 0; tileRow < tiles; tileRow++)
		{
			final int startRow = tileRow * tileHeight;
			int endRow;
			if (tileRow < tiles - 1)
			{
				endRow = (tileRow + 1) * tileHeight;
			} else
			{
				endRow = (int) image.size().height;
			}

			for (int tileCol = 0; tileCol < tiles; tileCol++)
			{
				final int startCol = tileCol * tileWidth;
				int endCol;
				if (tileCol < tiles - 1)
				{
					endCol = (tileCol + 1) * tileWidth;
				} else
				{
					endCol = (int) image.size().width;
				}

				final Mat tileMat = image.submat(startRow, endRow, startCol, endCol);
				Imgproc.threshold(tileMat, tileMat, 127, 255, Imgproc.THRESH_OTSU);
				tileMat.release();
			}
		}

		return image;
	}
}
