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
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

/**
 * This class contains a few implementations of reducing a colour image to greyscale by taking a
 * single channel. Use the ImageProcessorFactory classes to get the required ImageProcessor.
 * The MixChannels implementation seems to be the fastest, other implementations left here for
 * reference.
 */
public class RgbColourFilter
{
    public enum Channel
    {
        red, green, blue
    }

    public static class RedFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "redFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new RgbColourFilter_MixChannelsImpl(Channel.red);
        }
    }

    public static class GreenFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "greenFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new RgbColourFilter_MixChannelsImpl(Channel.green);
        }
    }

    public static class BlueFactory implements ImageProcessorFactory
    {
        public String getName()
        {
            return "blueFilter";
        }

        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new RgbColourFilter_MixChannelsImpl(Channel.blue);
        }
    }


    /**
     * This implementation uses OpenCV's mixChannels. It converts to BGR and then uses mixChannels
     * to separate the channels, a 2 channel buffer is require so mixChannels has somewhere to put
     * the undesired channels (crashes without it).
     *
     * It appears to be the fastest (average 50ms/frame on Nexus S).
     */
    public static class RgbColourFilter_MixChannelsImpl implements ImageProcessor
    {
        private final Channel channel;

        public Channel getChannel()
        {
            return channel;
        }

        public RgbColourFilter_MixChannelsImpl(Channel channel)
        {
            this.channel = channel;
        }

        private Mat extraChannelMat;
        private MatOfInt mix;

        @Override
        public void process(ImageBuffers buffers)
        {
            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat colorImage = buffers.getImageInBgr();

            /*
            Imgproc.putText(colorImage, "RED", new Point(40,40), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,0,255));
            Imgproc.putText(colorImage, "GREEN", new Point(60,60), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,0));
            Imgproc.putText(colorImage, "BLUE", new Point(80,80), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255,0,0));
            */

            List<Mat> src = new ArrayList<>(1), dst = new ArrayList<>(1);
            src.add(colorImage);
            dst.add(greyscaleImage);

            if (mix==null)
            {
                if (colorImage.channels()==3)
                {
                    if (channel == Channel.red)
                    {
                        // mix is a list of pairs that tell openCV to map input channel to output channels
                        mix = new MatOfInt(2, 0, 1, 1, 0, 2);
                    }
                    else if (channel == Channel.green)
                    {
                        mix = new MatOfInt(2, 1, 1, 0, 0, 2);
                    }
                    else if (channel == Channel.blue)
                    {
                        mix = new MatOfInt(2, 2, 1, 1, 0, 0);
                    }
                }
                else if (colorImage.channels()==4)
                {
                    if (channel == Channel.red)
                    {
                        mix = new MatOfInt(2, 0, 1, 1, 0, 2, 3, 3);
                    }
                    else if (channel == Channel.green)
                    {
                        mix = new MatOfInt(2, 1, 1, 0, 0, 2, 3, 3);
                    }
                    else if (channel == Channel.blue)
                    {
                        mix = new MatOfInt(2, 2, 1, 1, 0, 0, 3, 3);
                    }
                }
                else
                {
                    Log.w(getClass().getSimpleName(), "Colour image has unsupported number of channels: "+colorImage.channels());
                }
            }

            if (extraChannelMat == null)
            {

                if (colorImage.channels()==3)
                {
                    extraChannelMat = new Mat(greyscaleImage.rows(), greyscaleImage.cols(), CvType.CV_8UC2);
                }
                else if (colorImage.channels()==4)
                {
                    extraChannelMat = new Mat(greyscaleImage.rows(), greyscaleImage.cols(), CvType.CV_8UC3);
                }
            }
            dst.add(extraChannelMat);

            Core.mixChannels(src, dst, mix);
            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {

        }
    }


    /**
     * This implementation used OpenCV's split. Split seems to always re-allocate Mats even if you
     * populate the destination list so you either have to copy the data or use the Mat it creates.
     *
     * It's pretty fast (average 60ms/frame on Nexus S).
     */
    public static class RgbColourFilter_SplitImpl implements ImageProcessor
    {
        private final Channel channel;

        public RgbColourFilter_SplitImpl(Channel channel)
        {
            this.channel = channel;
        }

        @Override
        public void process(ImageBuffers buffers)
        {

            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat colorImage = buffers.getImageInBgr();

            List<Mat> dst = new ArrayList<>(3);

            Core.split(colorImage, dst);
            // Note: Split seems to always re-allocate Mats even if you populate the list. :(

            if (channel == Channel.red)
            {
                dst.get(2).copyTo(greyscaleImage);
            }
            else if (channel == Channel.green)
            {
                dst.get(1).copyTo(greyscaleImage);
            }
            else if (channel == Channel.blue)
            {
                dst.get(0).copyTo(greyscaleImage);
            }

            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {
            // TODO
        }
    }


    /**
     * Convert YUV data straight to a single channel.
     *
     * It's pretty fast (average 70ms/frame on Nexus S).
     */
    public static class RgbColourFilter_YUV2ChannelImpl implements ImageProcessor
    {
        private final Channel channel;
        private final UvProcessor uvProcessor;

        private static interface UvProcessor
        {
            int process(byte[] uvData, int index);
        }

        public RgbColourFilter_YUV2ChannelImpl(Channel channel)
        {
            this.channel = channel;
            if (channel==Channel.red)
            {
                this.uvProcessor = new UvProcessor()
                {
                    @Override
                    public int process(byte[] uvData, int index)
                    {
                        return (int) (1.370705 * ((uvData[index] & 0xFF) - 128));
                    }
                };
            }
            else if (channel==Channel.green)
            {
                this.uvProcessor = new UvProcessor()
                {
                    @Override
                    public int process(byte[] uvData, int index)
                    {
                        return (int) (- (0.698001 * ((uvData[index] & 0xFF) - 128)) - (0.337633 * ((uvData[index+1] & 0xFF) - 128)));
                    }
                };
            }
            else // if (channel==Channel.blue)
            {
                this.uvProcessor = new UvProcessor()
                {
                    @Override
                    public int process(byte[] uvData, int index)
                    {
                        return (int) (1.732446 * ((uvData[index+1] & 0xFF) - 128));
                    }
                };
            }
        }

        private byte[] yRow1;
        private byte[] yRow2;
        private byte[] uvRow;

        @Override
        public void process(ImageBuffers buffers)
        {
            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat yuvImage = buffers.getImageInYuv();
            final int yBufferSize = yuvImage.cols();
            final int uvBufferSize = yuvImage.cols();

            if (yRow1 == null || yRow1.length != yBufferSize)
                yRow1 = new byte[yBufferSize];
            if (yRow2 == null || yRow2.length != yBufferSize)
                yRow2 = new byte[yBufferSize];
            if (uvRow == null || uvRow.length != uvBufferSize)
                uvRow = new byte[uvBufferSize];

            final int totalUvRows = yuvImage.rows() / 3;
            final int uvRowOffset = totalUvRows * 2;
            final int width = yuvImage.cols();
            int uvRowIndex = 0;
            while (uvRowIndex<totalUvRows)
            {
                yuvImage.get(uvRowIndex*2, 0, yRow1);
                yuvImage.get(uvRowIndex*2+1, 0, yRow2);
                yuvImage.get(uvRowIndex+uvRowOffset, 0, uvRow);

                int uvColIndex = 0, uvComponent = 0;
                while (uvColIndex < width)
                {
                    //uvComponent = (int) (1.370705 * ((uvRow[uvColIndex] & 0xFF) - 128));
                    uvComponent = this.uvProcessor.process(uvRow, uvColIndex);
                    yRow1[uvColIndex] =     (byte) ((yRow1[uvColIndex]     & 0xFF) + uvComponent);
                    yRow1[uvColIndex + 1] = (byte) ((yRow1[uvColIndex + 1] & 0xFF) + uvComponent);
                    yRow2[uvColIndex] =     (byte) ((yRow2[uvColIndex]     & 0xFF) + uvComponent);
                    yRow2[uvColIndex + 1] = (byte) ((yRow2[uvColIndex + 1] & 0xFF) + uvComponent);

                    uvColIndex += 2;
                }
                greyscaleImage.put(uvRowIndex*2, 0, yRow1);
                greyscaleImage.put(uvRowIndex*2+1, 0, yRow2);
                ++uvRowIndex;
            }

            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {

        }
    }

    /**
     * This is the implementation from Storicodes, it converts to BGR and then uses a single byte[]
     * buffer to write the chosen channel back to the grey buffer Mat.
     *
     * It's reasonably fast (average 75ms/frame on Nexus S).
     */
    public static class RgbColourFilter_StoricodesImpl implements ImageProcessor
    {
        private final Channel channel;

        public RgbColourFilter_StoricodesImpl(Channel channel)
        {
            this.channel = channel;
        }

        private byte[] pixelBuffer;

        @Override
        public void process(ImageBuffers buffers)
        {
            Mat greyscaleImage = buffers.getGreyBuffer();
            Mat colorImage = buffers.getImageInBgr();

            int desiredColorBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
            if (pixelBuffer == null || pixelBuffer.length < desiredColorBufferSize)
                pixelBuffer = new byte[desiredColorBufferSize]; //bufferManager.getByteArray(desiredColorBufferSize);

            int desiredGreyBufferSize = colorImage.rows() * colorImage.cols();

            colorImage.get(0, 0, pixelBuffer);
            int c = channel == Channel.red ? 2 : (channel == Channel.green ? 1 : 0), g = 0, channels = colorImage.channels();
            while (g < desiredGreyBufferSize)
            {
                pixelBuffer[g] = pixelBuffer[c];
                ++g;
                c += channels;
            }
            greyscaleImage.put(0, 0, pixelBuffer);
            //bufferManager.returnByteArray(pixelBuffer);

            buffers.setImage(greyscaleImage);
        }

        @Override
        public void getSettings(List<DetectorSetting> settings)
        {

        }
    }

}
