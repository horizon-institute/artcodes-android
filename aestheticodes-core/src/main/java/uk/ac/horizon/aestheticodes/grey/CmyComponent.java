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

public class CmyComponent implements ImageProcessingComponent
{
    final int singleChannel;

    public enum CmyChannel
    {
        cyan, magenta, yellow
    }

    public CmyComponent(CmyChannel channel)
    {
        switch (channel)
        {
            case cyan:
                singleChannel=2;
                break;
            case magenta:
                singleChannel=1;
                break;
            case yellow:
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

        int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
        byte[] pixelBuffer = bufferManager.getByteArray(desiredBufferSize);

        colorImage.get(0, 0, pixelBuffer);

        for (int c = 0, g = 0; c < desiredBufferSize; c += 3, ++g)
        {
            pixelBuffer[g] = (byte) (255 - pixelBuffer[c + this.singleChannel]);
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
