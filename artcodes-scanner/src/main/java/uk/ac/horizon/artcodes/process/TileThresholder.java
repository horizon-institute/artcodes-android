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

package uk.ac.horizon.artcodes.process;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.R;

public class TileThresholder implements ImageProcessor
{
	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "tile";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, Map<String, String> args)
		{
			return new TileThresholder();
		}
	}

	private enum Display
	{
		none, greyscale, threshold;

		private static final Display[] vals = values();

		public Display next()
		{
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	private transient int tiles = 1;
	private Display display = Display.none;

	public TileThresholder()
	{
	}

	@Override
	public void process(ImageBuffers buffers)
	{
		Mat image = buffers.getImageInGrey();
		Imgproc.GaussianBlur(image, image, new Size(5, 5), 0);

		if (display == Display.greyscale)
		{
			Imgproc.cvtColor(image, buffers.getOverlay(false), Imgproc.COLOR_GRAY2BGRA);
		}

		if (!buffers.hasDetected())
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
			}
			else
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

		if (display == Display.threshold)
		{
			Imgproc.cvtColor(image, buffers.getOverlay(false), Imgproc.COLOR_GRAY2BGRA);
		}

		buffers.setImage(image);
	}

	@Override
	public void getSettings(List<DetectorSetting> settings)
	{
		settings.add(new DetectorSetting()
		{
			@Override
			public void nextValue()
			{
				display = display.next();
			}

			@Override
			public int getIcon()
			{
				switch (display)
				{
					case none:
						return R.drawable.ic_image_24dp;
					case greyscale:
						return R.drawable.ic_gradient_24dp;
					case threshold:
						return R.drawable.ic_filter_b_and_w_24dp;
				}
				return 0;
			}

			@Override
			public int getText()
			{
				switch (display)
				{
					case none:
						return R.string.draw_threshold_off;
					case greyscale:
						return R.string.draw_threshold_greyscale;
					case threshold:
						return R.string.draw_threshold_on;
				}
				return 0;
			}
		});
	}
}
