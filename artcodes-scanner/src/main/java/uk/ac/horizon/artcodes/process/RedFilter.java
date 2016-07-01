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

import org.opencv.core.Size;

import java.util.List;

import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class RedFilter implements ImageProcessor
{
	public static class Factory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "filter:red";
		}

		public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler)
		{
			return new RedFilter();
		}
	}

	public RedFilter()
	{

	}

	@Override
	public void process(ImageBuffers buffers)
	{
		final Size size = buffers.getTemp().size();
		for (int row = 0; row < size.height; row+=1)
		{
			for (int col = 0; col < size.width; col+=2)
			{
				//final double[] data = buffers.getImage().get(row, col);
				final byte[] data = new byte[4];
				buffers.getImage().get(row, col, data);

				byte y1 = data[0];
				byte y2 = data[2];
				//byte u = data[1];
				byte v = data[2];

				byte rcomp = (byte)(1.14 * v);
				int r1 = y1 + rcomp;
				int r2 = y2 + rcomp;

				data[0] = (byte) (0.299 * r1);
				data[1] = 0;
				data[2] = (byte) (0.299 * r2);
				data[3] = 0;

				buffers.getImage().put(row, col, data);
			}
		}
	}

	@Override
	public void getSettings(List<DetectorSetting> settings)
	{
		//SettingButtonBinding
	}
}
