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

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.marker.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class RGBFilter implements ImageProcessor
{
	public static class RedFactory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "filter:red";
		}

		public ImageProcessor create(Experience experience, MarkerDetectionHandler handler)
		{
			return new RGBFilter(Channel.red);
		}
	}

	public static class BlueFactory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "filter:blue";
		}

		public ImageProcessor create(Experience experience, MarkerDetectionHandler handler)
		{
			return new RGBFilter(Channel.blue);
		}
	}

	public static class GreenFactory implements ImageProcessorFactory
	{
		public String getName()
		{
			return "filter:green";
		}

		public ImageProcessor create(Experience experience, MarkerDetectionHandler handler)
		{
			return new RGBFilter(Channel.green);
		}
	}

	public enum Channel
	{
		red, green, blue
	}

	private final Channel channel;

	public RGBFilter(Channel channel)
	{
		this.channel = channel;
	}

	@Override
	public void process(ImageBuffers buffers)
	{
		Imgproc.cvtColor(buffers.getImage(), buffers.getTemp(), Imgproc.COLOR_YUV2BGR_YUY2);

		final Size sizeA = buffers.getTemp().size();
		for (int i = 0; i < sizeA.height; i++)
		{
			for (int j = 0; j < sizeA.width; j++)
			{
				final double[] data = buffers.getTemp().get(i, j);
				final double value = getValue(data);

				data[0] = value;
				data[1] = value;
				data[2] = value;
				buffers.getTemp().put(i, j, data);
			}
		}

		Imgproc.cvtColor(buffers.getTemp(), buffers.getImage(), Imgproc.COLOR_BGR2GRAY);
	}

	public Channel getChannel()
	{
		return channel;
	}

	@Override
	public void getSettings(List<DetectorSetting> settings)
	{
		//SettingButtonBinding
	}

	private double getValue(double[] data)
	{
		switch (channel)
		{
			case red:
				return data[2];
			case blue:
				return data[0];
			case green:
				return data[1];
		}
		return 0;
	}
}
