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

package uk.ac.horizon.artcodes.scanner.threshold;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class TemporalTileThresholder implements Thresholder
{
	private int cumulativeFramesWithoutMarker;

	@Override
	public void threshold(Mat image, int framesSinceLastMarker)
	{
		Imgproc.GaussianBlur(image, image, new Size(5, 5), 0);

		final int numberOfTiles = (cumulativeFramesWithoutMarker % 9) + 1;
		final int tileHeight = (int) image.size().height / numberOfTiles;
		final int tileWidth = (int) image.size().width / numberOfTiles;

		// Split image into tiles and apply threshold on each image tile separately.
		for (int tileRowCount = 0; tileRowCount < numberOfTiles; tileRowCount++)
		{
			final int startRow = tileRowCount * tileHeight;
			int endRow;
			if (tileRowCount < numberOfTiles - 1)
			{
				endRow = (tileRowCount + 1) * tileHeight;
			}
			else
			{
				endRow = (int) image.size().height;
			}

			for (int tileColCount = 0; tileColCount < numberOfTiles; tileColCount++)
			{
				final int startCol = tileColCount * tileWidth;
				int endCol;
				if (tileColCount < numberOfTiles - 1)
				{
					endCol = (tileColCount + 1) * tileWidth;
				}
				else
				{
					endCol = (int) image.size().width;
				}

				final Mat tileMat = image.submat(startRow, endRow, startCol, endCol);
				Imgproc.threshold(tileMat, tileMat, 127, 255, Imgproc.THRESH_OTSU);
				tileMat.release();
			}
		}

		Imgproc.threshold(image, image, 127, 255, Imgproc.THRESH_OTSU);
	}
}
