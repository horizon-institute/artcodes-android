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

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class HlsEditImageProcessor implements ImageProcessor
{
    public static class HlsEditImageProcessorFactory implements ImageProcessorFactory
    {

        @Override
        public String getName()
        {
            return "hlsEdit";
        }

        private static final String[] HUE_KEYS = {"hue", "hueShift", "h"};
        private static final String[] LIGHTNESS_KEYS = {"lightness", "l"};
        private static final String[] SATURATION_KEYS = {"saturation", "s"};

        @Override
        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, Map<String, String> args)
        {
            int hueShift=0, lightnessAddition=0, saturationAddition=0;

            if (args != null)
            {
                for (String key : HUE_KEYS)
                {
                    if (args.containsKey(key))
                    {
                        hueShift = Integer.parseInt(args.get(key));
                    }
                }
                for (String key : LIGHTNESS_KEYS)
                {
                    if (args.containsKey(key))
                    {
                        lightnessAddition = Integer.parseInt(args.get(key));
                    }
                }
                for (String key : SATURATION_KEYS)
                {
                    if (args.containsKey(key))
                    {
                        saturationAddition = Integer.parseInt(args.get(key));
                    }
                }
            }

            return new HlsEditImageProcessor(hueShift, lightnessAddition, saturationAddition);
        }
    }

    protected final int hueShift, lightnessAddition, saturationAddition;
    protected final Mat lut;

    public HlsEditImageProcessor(int hueShift, int lightnessAddition, int saturationAddition)
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
    public void process(ImageBuffers buffers)
    {
        if (this.hueShift!=0 || this.lightnessAddition!=0 || this.saturationAddition!=0)
        {
            // Convert to HLS:
            Mat threeChannelBuffer = buffers.getImageInBgr();
            Imgproc.cvtColor(threeChannelBuffer, threeChannelBuffer, Imgproc.COLOR_BGR2HLS);

            // Apply look-up-table:
            Core.LUT(threeChannelBuffer, lut, threeChannelBuffer);

            // Convert back to BGR:
            Imgproc.cvtColor(threeChannelBuffer, threeChannelBuffer, Imgproc.COLOR_HLS2BGR);
            buffers.setImage(threeChannelBuffer);
        }
    }

    @Override
    public void getSettings(List<DetectorSetting> settings)
    {

    }

    public void release()
    {
        this.lut.release();
    }
}
