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

package uk.ac.horizon.aestheticodes.grey;

import org.opencv.core.Mat;

public class RgbComponent implements ImageProcessingComponent
{
    final int singleChannel;

    public enum RgbChannel
    {
        red, green, blue
    }

    public RgbComponent(RgbChannel channel)
    {
        switch (channel)
        {
            case red:
                singleChannel=2;
                break;
            case green:
                singleChannel=1;
                break;
            case blue:
            default:
                singleChannel=0;
                break;
        }
    }

    @Override
    public void process(BufferManager bufferManager)
    {
        Mat greyscaleImage = bufferManager.getGreyBuffer();
        Mat colorImage = bufferManager.getMostRecentDataInBgr();

        int desiredColorBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
        byte[] pixelBuffer = bufferManager.getByteArray(desiredColorBufferSize);

        int desiredGreyBufferSize = colorImage.rows() * colorImage.cols();

        colorImage.get(0, 0, pixelBuffer);
        int c = this.singleChannel, g = 0, channels = colorImage.channels();
        while (g < desiredGreyBufferSize)
        {
            pixelBuffer[g] = pixelBuffer[c];
            ++g;
            c += channels;
        }
        greyscaleImage.put(0, 0, pixelBuffer);
        bufferManager.returnByteArray(pixelBuffer);
        bufferManager.setGreyBuffer(greyscaleImage);
    }

    @Override
    public boolean segmentSafe()
    {
        return true;
    }

    @Override
    public boolean segmentRecommended()
    {
        return false;
    }

    @Override
    public void release()
    {

    }
}
