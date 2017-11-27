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

import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class CmykColourFilter
{
    public enum Channel
    {
        cyan, magenta, yellow, black
    }

    public static class CyanCmykColourFilterFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "cyanKFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new CmykColourFilter_UpdatedCMYImpl(Channel.cyan);
        }
    }
    public static class MagentaCmykColourFilterFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "magentaKFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new CmykColourFilter_UpdatedCMYImpl(Channel.magenta);
        }
    }
    public static class YellowCmykColourFilterFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "yellowKFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new CmykColourFilter_UpdatedCMYImpl(Channel.yellow);
        }
    }
    public static class BlackCmykColourFilterFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "blackKFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new CmykColourFilter_UpdatedKImpl();
        }
    }

    /**
     * This is the implementation from Storicodes.
     *
     * It's very slow (average 490ms/frame on Nexus S). Possibly due to unnecessary use of floats
     * and using Mat.put when greyscale buffer is not continuous (as it's a sub-Mat of a YUV image
     * here, I think it is continuous in Storicodes).
     */
    public static class CmykColourFilter_StoricodesImpl implements ImageProcessor
    {
        private final Channel channel;

        public CmykColourFilter_StoricodesImpl(Channel channel)
        {
            this.channel = channel;
        }

        private byte[] pixelBuffer;

        @Override
        public void process(ImageBuffers buffers)
        {
            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat colorImage = buffers.getImageInBgr();

            int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
            if (pixelBuffer==null || pixelBuffer.length<desiredBufferSize)
            {
                pixelBuffer = new byte[desiredBufferSize];
            }

            colorImage.get(0, 0, pixelBuffer);

            if (this.channel==Channel.black) // k only
            {
                for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                    pixelBuffer[j] = (byte) Math.min(255 - (pixelBuffer[i] & 0xFF), Math.min(255 - (pixelBuffer[i+1] & 0xFF), 255 - (pixelBuffer[i+2] & 0xFF)));
                }
            }
            else // c, y or m only
            {
                int channelIndex = channel==Channel.cyan ? 2 : (channel==Channel.magenta ? 1 : 0);
                float k;
                float[] bgr = new float[3];
                for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                    bgr[0] = (pixelBuffer[i] & 0xFF) / 255f;
                    bgr[1] = (pixelBuffer[i + 1] & 0xFF) / 255f;
                    bgr[2] = (pixelBuffer[i + 2] & 0xFF) / 255f;

                    k = Math.min(1f - bgr[0], Math.min(1f - bgr[1], 1f - bgr[2]));
                    float result = k==1 ? 0 : (1f - bgr[channelIndex] - k);
                    pixelBuffer[j] = (byte) (result*255f);
                }
            }

            greyscaleImage.put(0, 0, pixelBuffer);
            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {

        }
    }

    /**
     * This is an updated version of the Storicodes implementation that just uses integers and
     * only handles cyan, magenta and yellow.
     *
     * It's slow (average 100ms/frame on Nexus S).
     */
    public static class CmykColourFilter_UpdatedCMYImpl implements ImageProcessor
    {
        private final Channel channel;
        private final int channelIndex;

        public CmykColourFilter_UpdatedCMYImpl(Channel channel)
        {
            this.channel = channel;
            this.channelIndex = channel==Channel.cyan ? 2 : (channel==Channel.magenta ? 1 : 0);
        }

        private byte[] colorPixelBuffer, rowBuffer;

        @Override
        public void process(ImageBuffers buffers)
        {
            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat colorImage = buffers.getImageInBgr();

            int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
            int desiredRowBufferSize = greyscaleImage.cols();
            if (colorPixelBuffer ==null || colorPixelBuffer.length<desiredBufferSize)
            {
                colorPixelBuffer = new byte[desiredBufferSize];
            }
            if (rowBuffer==null || rowBuffer.length!=desiredBufferSize)
            {
                rowBuffer = new byte[desiredRowBufferSize];
            }

            colorImage.get(0, 0, colorPixelBuffer);

            int k, result;
            int colorPixelBufferIndex = 0, colIndex = 0, rowIndex = 0;
            while (colorPixelBufferIndex < desiredBufferSize) {
                colIndex = 0;
                while (colIndex < desiredRowBufferSize)
                {
                    k = 255 - Math.max(colorPixelBuffer[colorPixelBufferIndex] & 0xFF, Math.max(colorPixelBuffer[colorPixelBufferIndex + 1] & 0xFF, colorPixelBuffer[colorPixelBufferIndex + 2] & 0xFF));
                    result = k == 255 ? 0 : (255 - (colorPixelBuffer[colorPixelBufferIndex + channelIndex] & 0xFF) - k);
                    rowBuffer[colIndex] = (byte) (result > 255 ? 255 : (result < 0 ? 0 : result));

                    colorPixelBufferIndex += 3;
                    ++colIndex;
                }
                greyscaleImage.put(rowIndex++, 0, rowBuffer);
            }

            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {

        }
    }

    /**
     * This is an updated version of the Storicodes implementation that just uses integers and
     * only handles black.
     *
     * It's reasonably fast (average 75ms/frame on Nexus S).
     */
    public static class CmykColourFilter_UpdatedKImpl implements ImageProcessor
    {
        private byte[] pixelBuffer, rowBuffer;

        @Override
        public void process(ImageBuffers buffers)
        {
            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat colorImage = buffers.getImageInBgr();

            final int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
            final int desiredRowBufferSize = greyscaleImage.cols();
            if (pixelBuffer==null || pixelBuffer.length<desiredBufferSize)
            {
                pixelBuffer = new byte[desiredBufferSize];
            }
            if (rowBuffer==null || rowBuffer.length!=desiredBufferSize)
            {
                rowBuffer = new byte[desiredRowBufferSize];
            }

            colorImage.get(0, 0, pixelBuffer);

            final int rows = colorImage.rows();
            final int colorChannels = colorImage.channels();
            int colorImageIndex = 0;
            for (int rowIndex = 0; rowIndex < rows; ++rowIndex)
            {
                for (int colIndex = 0; colIndex < desiredRowBufferSize; ++colIndex)
                {
                    rowBuffer[colIndex] = (byte) (255 - Math.max(pixelBuffer[colorImageIndex] & 0xFF, Math.max(pixelBuffer[colorImageIndex + 1] & 0xFF, pixelBuffer[colorImageIndex + 2] & 0xFF)));
                    colorImageIndex += colorChannels;
                }
                greyscaleImage.put(rowIndex, 0, rowBuffer);
            }

            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {

        }
    }

}
