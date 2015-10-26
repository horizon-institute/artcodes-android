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

public class CmykComponent implements ImageProcessingComponent
{
    final int singleChannel;

    public enum CmykChannel
    {
        cyan, magenta, yellow, black
    }

    public CmykComponent(CmykChannel channel)
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
                singleChannel=0;
                break;
            case black:
            default:
                singleChannel=3;
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

        if (this.singleChannel==3) // k only
        {
            for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                pixelBuffer[j] = (byte) Math.min(255 - (pixelBuffer[i] & 0xFF), Math.min(255 - (pixelBuffer[i+1] & 0xFF), 255 - (pixelBuffer[i+2] & 0xFF)));
            }
        }
        else // c, y or m only
        {
            float k;
            float[] bgr = new float[3];
            for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                bgr[0] = (pixelBuffer[i] & 0xFF) / 255f;
                bgr[1] = (pixelBuffer[i + 1] & 0xFF) / 255f;
                bgr[2] = (pixelBuffer[i + 2] & 0xFF) / 255f;

                k = Math.min(1f - bgr[0], Math.min(1f - bgr[1], 1f - bgr[2]));
                float result = k==1 ? 0 : (1f - bgr[this.singleChannel] - k);
                pixelBuffer[j] = (byte) (result*255f);
            }
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
