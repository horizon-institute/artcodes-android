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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class HlsEditComponent implements ImageProcessingComponent
{

    protected final int hueShift, lightnessAddition, saturationAddition;
    protected final Mat lut;

    public HlsEditComponent(int hueShift, int lightnessAddition, int saturationAddition)
    {
        // hue input range: [0,360] change to range: [0,180]
        this.hueShift = (hueShift / 2) % 181;

        // lightness input range: [-100,100] change to range: [-255,255]
        this.lightnessAddition = Math.min(Math.max((int) (lightnessAddition*2.55),-255),255);

        // saturation input range: [-100,100] change to range: [-255,255]
        this.saturationAddition = Math.min(Math.max((int) (saturationAddition*2.55),-255),255);

        float lightnessMultiplyer = ((this.lightnessAddition+255.0f)/255.0f);
        float saturationMultiplyer = ((this.lightnessAddition+255.0f)/255.0f);

        this.lut = new Mat(1, 256, CvType.CV_8UC3);
        int lutSize = lut.cols() * lut.rows() * lut.channels();
        byte[] lutBuffer = new byte [lutSize];
        for (int i=0, lutIndex = -1; i<256; ++i)
        {
            lutBuffer[++lutIndex] = (byte) (((i)+this.hueShift)%181);
            lutBuffer[++lutIndex] = (byte) Math.min(Math.max(
                    (int) ((i) * lightnessMultiplyer)
                    , 0), 255);
            lutBuffer[++lutIndex] = (byte) Math.min(Math.max(
                    (int) ((i) * saturationMultiplyer)
                    , 0), 255);
        }
        lut.put(0, 0, lutBuffer);
    }

    @Override
    public void process(BufferManager bufferManager)
    {

        if (this.hueShift!=0 || this.lightnessAddition!=0 || this.saturationAddition!=0)
        {
            Mat threeChannelBuffer = bufferManager.getMostRecentDataInBgr();
            Imgproc.cvtColor(threeChannelBuffer, threeChannelBuffer, Imgproc.COLOR_BGR2HLS);

            Core.LUT(threeChannelBuffer, lut, threeChannelBuffer);

            // convert back to BGR
            Imgproc.cvtColor(threeChannelBuffer, threeChannelBuffer, Imgproc.COLOR_HLS2BGR);
            bufferManager.setBgrBuffer(threeChannelBuffer);
        }
    }

    @Override
    public boolean segmentSafe()
    {
        return true;
    }

    @Override
    public boolean segmentRecommended()
    {
        return true;
    }

    @Override
    public void release()
    {
        this.lut.release();
    }
}
