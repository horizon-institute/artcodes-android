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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;
import uk.ac.horizon.artcodes.detect.handler.MarkerDetectionHandler;
import uk.ac.horizon.artcodes.model.Experience;

public class WhiteBalanceImageProcessor implements ImageProcessor
{

    public static class WhiteBalanceImageProcessorFactory implements ImageProcessorFactory
    {

        @Override
        public String getName()
        {
            return "whiteBalance";
        }

        @Override
        public ImageProcessor create(Context context, Experience experience, MarkerDetectionHandler handler, CameraFocusControl cameraFocusControl, Map<String, String> args)
        {
            return new WhiteBalanceImageProcessor();
        }
    }

    protected MatOfInt[] channels = null;
    protected Mat[] histograms = null;
    protected Mat emptyMatMask = null;
    protected MatOfInt size = null;
    protected MatOfFloat range = null;
    protected Mat lut = null;
    private byte[] lutBufferArray;

    private void setup()
    {
        channels = new MatOfInt[] {new MatOfInt(0), new MatOfInt(1), new MatOfInt(2), new MatOfInt(3)};
        histograms = new Mat[] {new Mat(), new Mat(), new Mat(), new Mat()};
        emptyMatMask = new Mat();
        size = new MatOfInt(256);
        range = new MatOfFloat(0,256);
    }

    @Override
    public void process(ImageBuffers buffers)
    {
        Mat image = buffers.getImageInBgr();
        if (this.histograms==null)
        {
            this.setup();
        }
        List<Mat> listOfMat = new ArrayList<>();
        listOfMat.add(image);

        // create a histogram for each channel:
        // (oddly it seems ~10x faster to do 3 channels separately rather than all 3 in one calcHist call)
        for (int channel=0; channel<image.channels(); ++channel)
        {
            Imgproc.calcHist(listOfMat, channels[channel], emptyMatMask, histograms[channel], size, range);
        }

        float[] a = new float[image.channels()];
        float[] b = new float[image.channels()];

        final int desiredHistogramBufferSize = histograms[0].rows()*histograms[0].cols()*histograms[0].channels();
        float[] pixelHistogramBuffer = new float[desiredHistogramBufferSize];

        // get the values to remap the histograms:
        for (int channel=0; channel<image.channels(); ++channel)
        {
            histograms[channel].get(0, 0, pixelHistogramBuffer);
            getHistogramRemap(pixelHistogramBuffer, desiredHistogramBufferSize, image.total(), a, channel, b, channel);
        }

        // Use a Look Up Table to re-map values
        // (it's a lot faster to workout and save what the 256 possible values transform into
        // than to do the math image.cols*rows times)

        if (lut==null)
        {
            lut = new Mat(1, 256, CvType.CV_8UC3);
        }
        final int lutSize = lut.cols() * lut.rows() * lut.channels();
        int lutIndex = -1;
        if (lutBufferArray==null || lutBufferArray.length!=lutSize)
        {
            lutBufferArray = new byte[lutSize];
        }
        for (int i=0; i<256; ++i)
        {
            for (int channel=0; channel<image.channels(); ++channel)
            {
                lutBufferArray[++lutIndex] = (byte) Math.min(Math.max(a[channel] * ((i) - b[channel]), 0), 255);
            }
        }
        lut.put(0, 0, lutBufferArray);
        Core.LUT(image, lut, image);
        buffers.setImage(image);
    }

    private static void getHistogramRemap(float[] histogram, int size, long total, float[] resultA, int resultAIndex, float[] resultB, int resultBIndex)
    {
        if (total==-1)
        {
            total = 0;
            for (int i = 0; i < size; ++i)
            {
                total += histogram[i];
            }
        }

        final float p5 = total*0.05f, p95 = total*0.95f;
        resultB[resultBIndex] = resultA[resultAIndex] = -1;
        int count = 0;


        for (int i=0; i<size; ++i)
        {
            count += histogram[i];
            if (resultB[resultBIndex]==-1 && count>=p5)
            {
                resultB[resultBIndex] = i;
            }
            else if (count>=p95)
            {
                resultA[resultAIndex] = 255f/(i-resultB[resultBIndex]);
                break;
            }
        }
    }

    @Override
    public void getSettings(List<DetectorSetting> settings)
    {

    }

    public void release()
    {
        lut.release();
        for (Mat channel : channels)
        {
            channel.release();
        }
        emptyMatMask.release();
        for (Mat histogram : histograms)
        {
            histogram.release();
        }
        size.release();
        range.release();
    }
}
