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

package uk.ac.horizon.artcodes.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

public class CameraInfo
{
	private final int rotation;
	private final int imageWidth;
	private final int imageHeight;
	private final int imageDepth;
	private final boolean frontFacing;

	@SuppressWarnings("deprecation")
	public CameraInfo(final Camera.CameraInfo cameraInfo, final Camera.Parameters parameters, final int deviceRotation)
	{
		frontFacing = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
		imageWidth = parameters.getPreviewSize().width;
		imageHeight = parameters.getPreviewSize().height;
		imageDepth = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat());
		rotation = getRotation(cameraInfo, deviceRotation);
	}

	@SuppressWarnings("deprecation")
	private int getRotation(Camera.CameraInfo info, int deviceRotation)
	{
		int rotation;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			rotation = (info.orientation + deviceRotation) % 360;
			rotation = (360 - rotation) % 360;  // compensate the mirror
		}
		else
		{  // back-facing
			rotation = (info.orientation - deviceRotation + 360) % 360;
		}

		return rotation;
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	public int getImageHeight()
	{
		return imageHeight;
	}

	public int getImageDepth()
	{
		return imageDepth;
	}

	public int getRotation()
	{
		return rotation;
	}

	public boolean isFrontFacing()
	{
		return frontFacing;
	}
}