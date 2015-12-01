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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import uk.ac.horizon.artcodes.scanner.ImageBuffers;
import uk.ac.horizon.artcodes.scanner.R;
import uk.ac.horizon.artcodes.scanner.TextAnimator;

public class TileThresholder implements ImageProcessor
{
	private enum Display
	{
		none, greyscale, threshold;

		private static Display[] vals = values();

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
		Imgproc.GaussianBlur(buffers.getImage(), buffers.getImage(), new Size(5, 5), 0);

		if (display == Display.greyscale)
		{
			Imgproc.cvtColor(buffers.getImage(), buffers.getOverlay(false), Imgproc.COLOR_GRAY2BGRA);
		}

		if (!buffers.hasDetected())
		{
			tiles = (tiles % 9) + 1;
		}
		final int tileHeight = (int) buffers.getImage().size().height / tiles;
		final int tileWidth = (int) buffers.getImage().size().width / tiles;

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
				endRow = (int) buffers.getImage().size().height;
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
					endCol = (int) buffers.getImage().size().width;
				}

				final Mat tileMat = buffers.getImage().submat(startRow, endRow, startCol, endCol);
				Imgproc.threshold(tileMat, tileMat, 127, 255, Imgproc.THRESH_OTSU);
				tileMat.release();
			}
		}

		if (display == Display.threshold)
		{
			Imgproc.cvtColor(buffers.getImage(), buffers.getOverlay(false), Imgproc.COLOR_GRAY2BGRA);
		}
	}

	@Override
	public void getSettings(List<ImageProcessorSetting> settings)
	{
		settings.add(new ImageProcessorSetting()
		{
			@Override
			public void nextValue()
			{
				display = display.next();
			}

			@Override
			public void updateUI(ImageButton button, TextAnimator textAnimator)
			{
				int text = 0;
				switch (display)
				{
					case none:
						button.setImageResource(R.drawable.ic_image_24dp);
						text = R.string.draw_threshold_off;
						break;
					case greyscale:
						button.setImageResource(R.drawable.ic_gradient_24dp);
						text = R.string.draw_threshold_greyscale;
						break;
					case threshold:
						button.setImageResource(R.drawable.ic_filter_b_and_w_24dp);
						text = R.string.draw_threshold_on;
						break;
				}

				if(text != 0 && textAnimator != null)
				{
					textAnimator.setText(text);
				}
			}
		});
	}
}
