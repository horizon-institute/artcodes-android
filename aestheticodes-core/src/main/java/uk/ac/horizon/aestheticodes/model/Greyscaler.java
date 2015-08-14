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

package uk.ac.horizon.aestheticodes.model;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class containing the method to greyscale an image.
 * This is a base abstract class, use a subclass like Greyscaler.IntensityGreyscaler or Greyscaler.SingleChannelRgbGreyscaler.
 */
public abstract class Greyscaler
{
    protected static final String KEY = "GREY";

    protected final int hueShift;
    protected final boolean invert;

    protected Mat threeChannelBuffer = null;
    /**
     * This is a buffer that can be used by sub-classes for storing intermediate color data.
     * If the array is not long enough (or null) recreate it at the desired length but never make it smaller.
     * Data in this buffer may be overwritten.
     */
    protected byte[] colorPixelBuffer = null;

    public static Greyscaler getGreyscaler(double hueShift, List<Object> greyscaleOptions, boolean invert)
    {
        int threadCount = 10;

        if (greyscaleOptions!=null)
        {
            if (greyscaleOptions.size() == 4 && greyscaleOptions.get(0).equals("RGB"))
            {
                double r=(double)greyscaleOptions.get(1), g=(double)greyscaleOptions.get(2), b=(double)greyscaleOptions.get(3);
                if (r==1 && g==0 && b==0)
                {
                    List<Greyscaler> greyscalers = new ArrayList<>();
                    while (greyscalers.size() < threadCount)
                    {
                        greyscalers.add(new SingleChannelRgbGreyscaler(hueShift, 2, invert));
                    }
                    return new ThreadedGreyscaler(greyscalers);
                }
                else if (r==0 && g==1 && b==0)
                {
                    List<Greyscaler> greyscalers = new ArrayList<>();
                    while (greyscalers.size() < threadCount)
                    {
                        greyscalers.add(new SingleChannelRgbGreyscaler(hueShift, 1, invert));
                    }
                    return new ThreadedGreyscaler(greyscalers);
                }
                else if (r==0 && g==0 && b==1)
                {
                    List<Greyscaler> greyscalers = new ArrayList<>();
                    while (greyscalers.size() < threadCount)
                    {
                        greyscalers.add(new SingleChannelRgbGreyscaler(hueShift, 0, invert));
                    }
                    return new ThreadedGreyscaler(greyscalers);
                }
                else if (r==0.299 && g==0.587 && b==0.114)
                {
                    return new IntensityGreyscaler(hueShift, invert);
                }
                else
                {
                    return new Greyscaler.WeightedChannelRgbGreyscaler(hueShift, ((Number) greyscaleOptions.get(1)).doubleValue(), ((Number) greyscaleOptions.get(2)).doubleValue(), ((Number) greyscaleOptions.get(3)).doubleValue(), invert);
                }
            }
            else if (greyscaleOptions.size() == 5 && greyscaleOptions.get(0).equals("CMYK")) {
                List<Greyscaler> greyscalers = new ArrayList<>();
                while (greyscalers.size() < threadCount)
                {
                    greyscalers.add(new Greyscaler.CmykGreyscaler(hueShift, ((Number) greyscaleOptions.get(1)).doubleValue(), ((Number) greyscaleOptions.get(2)).doubleValue(), ((Number) greyscaleOptions.get(3)).doubleValue(), ((Number) greyscaleOptions.get(4)).doubleValue(), invert));
                }
                return new ThreadedGreyscaler(greyscalers);
            }
            else if (greyscaleOptions.size() == 4 && greyscaleOptions.get(0).equals("CMY")) {
                List<Greyscaler> greyscalers = new ArrayList<>();
                while (greyscalers.size() < threadCount)
                {
                    greyscalers.add(new Greyscaler.CmyGreyscaler(hueShift, ((Number) greyscaleOptions.get(1)).doubleValue(), ((Number) greyscaleOptions.get(2)).doubleValue(), ((Number) greyscaleOptions.get(3)).doubleValue(), invert));
                }
                return new ThreadedGreyscaler(greyscalers);
            }
        }
        return new IntensityGreyscaler(hueShift, invert);
    }

    public Greyscaler(double hueShift, boolean invert)
    {
        // hue should be in range [0-180]
        while (hueShift<0)
        {
            hueShift +=180;
        }
        while (hueShift>180)
        {
            hueShift -=180;
        }
        this.hueShift=(int)hueShift;
        this.invert=invert;
    }

    /**
     * Create a greyscale image.
     * @param yuvImage A YUV NV12 image (CV_8UC1). Should not be null.
     * @param greyscaleImage A greyscale image (CV_8UC1) to place the result in, depending on the method used this may or may not be used (can be null).
     * @return A greyscale image. Depending on the method used this may or may not be the same buffer passed in as greyscaleImage.
     */
    public Mat greyscaleImage(Mat yuvImage, Mat greyscaleImage)
    {
        if (this.useIntensityShortcut() && hueShift==0)
        {
            if (greyscaleImage != null)
            {
                greyscaleImage.release();
            }
            // cut off the UV components
            greyscaleImage = yuvImage.submat(0, (yuvImage.rows()/3)*2, 0, yuvImage.cols());
        }
        else
        {
            int desiredRows = (yuvImage.rows() / 3) * 2, desiredCols = yuvImage.cols();
            if (this.threeChannelBuffer==null || this.threeChannelBuffer.rows()!=desiredRows || this.threeChannelBuffer.cols()!=desiredCols)
            {
                Log.i(KEY, "Creating new Mat buffer (1)");
                this.threeChannelBuffer = new Mat(desiredRows, desiredCols, CvType.CV_8UC3);
            }
            Imgproc.cvtColor(yuvImage, this.threeChannelBuffer, Imgproc.COLOR_YUV2BGR_NV21);
            this.justHueShiftImage(this.threeChannelBuffer, this.threeChannelBuffer);
            greyscaleImage = this.justGreyscaleImage(this.threeChannelBuffer, greyscaleImage);
        }

        if (this.invert)
        {
            Core.bitwise_not(greyscaleImage, greyscaleImage);
        }

        return greyscaleImage;
    }

    /**
     * Shift the hue of an image.
     * @param colorImage A BGR image (CV_8UC3).
     * @param resultImage A BGR image (CV_8UC3) where the result will be stored, can be the same object as colorImage.
     */
    protected void justHueShiftImage(Mat colorImage, Mat resultImage)
    {
        if (this.hueShift!=0)
        {
            Imgproc.cvtColor(colorImage, resultImage, Imgproc.COLOR_BGR2HLS);

            int desiredBufferSize = colorImage.rows()*colorImage.cols()*colorImage.channels();
            if (this.colorPixelBuffer==null || this.colorPixelBuffer.length<desiredBufferSize)
            {
                Log.i(KEY, "Creating new byte["+desiredBufferSize+"] buffer (2)");
                this.colorPixelBuffer = new byte[desiredBufferSize];
            }

            colorImage.get(0,0,this.colorPixelBuffer);
            for (int i=0; i<this.colorPixelBuffer.length; i+=colorImage.channels())
            {
                this.colorPixelBuffer[i] = (byte) (((this.colorPixelBuffer[i] & 0xFF) + this.hueShift) % 181);
            }
            colorImage.put(0,0,this.colorPixelBuffer);

            // convert back to BGR
            Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_HLS2BGR);
        }
    }

    /**
     * Sub-classes should implement this.
     * @param colorImage A BGR image (CV_8UC3).
     * @param greyscaleImage A greyscale image (CV_8UC1).
     */
    protected abstract Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage);

    /**
     * Sub-classes should implement this.
     * @return true if the super-class should a more efficient way to create an intensity greyscale image, false if the subclass is going to use another method.
     */
    protected abstract boolean useIntensityShortcut();


    /**
     * Release any resources held.
     */
    public void release()
    {
        if (this.threeChannelBuffer != null)
        {
            this.threeChannelBuffer.release();
            this.threeChannelBuffer = null;
        }
        colorPixelBuffer = null;
    }

    public static class IntensityGreyscaler extends Greyscaler
    {
        public IntensityGreyscaler(double hueShift, boolean invert)
        {
            super(hueShift, invert);
            Log.i(KEY, "Creating IntensityGreyscaler");
        }

        @Override
        protected boolean useIntensityShortcut()
        {
            return true;
        }

        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            if (greyscaleImage==null || greyscaleImage.rows()!=colorImage.rows() || greyscaleImage.cols()!=colorImage.cols())
            {
                greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
            }
            Imgproc.cvtColor(colorImage, greyscaleImage, Imgproc.COLOR_BGR2GRAY);
            return greyscaleImage;
        }
    }

    // TODO: Investigate why this crashes when allocating the weight Mat.
    public static class WeightedChannelRgbGreyscaler extends Greyscaler
    {
        private final Mat weight;
        public WeightedChannelRgbGreyscaler(double hueShift, double redMultiplier, double greenMultiplier, double blueMultiplier, boolean invert)
        {
            super(hueShift, invert);
            Log.i(KEY, "Creating WeightedChannelRgbGreyscaler");

            this.weight = new Mat(1, 3,  CvType.CV_32FC1, new Scalar(0));
            this.weight.put(0, 0, blueMultiplier);
            this.weight.put(0, 1, greenMultiplier);
            this.weight.put(0, 2, redMultiplier);
        }

        @Override
        protected boolean useIntensityShortcut() {
            return false;
        }

        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            if (greyscaleImage==null || greyscaleImage.rows()!=colorImage.rows() || greyscaleImage.cols()!=colorImage.cols())
            {
                greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
            }
            Core.transform(colorImage, greyscaleImage, this.weight);
            return greyscaleImage;
        }
    }

    public static class SingleChannelRgbGreyscaler extends Greyscaler
    {
        private final int singleChannel;
        public SingleChannelRgbGreyscaler(double hueShift, int channel, boolean invert)
        {
            super(hueShift, invert);
            Log.i(KEY, "Creating SingleChannelRgbGreyscaler");

            if (0<=channel && channel<=2)
            {
                this.singleChannel = channel;
            }
            else
            {
                Log.e(KEY, "SingleChannelRgbGreyscaler received channel index out of range ("+channel+"), using 0.");
                this.singleChannel = 0;
            }
        }

        @Override
        protected boolean useIntensityShortcut() {
            return false;
        }

        private byte[] singleChannelGreyPixelBuffer = null;
        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            /*
            // This method is much simpler (and a little faster) but randomly crashes :(
            List<Mat> channels = new ArrayList<>(3);
            Core.split(colorImage, channels);
            return channels.get(this.singleChannel);
            */

            if (greyscaleImage==null || greyscaleImage.rows()!=colorImage.rows() || greyscaleImage.cols()!=colorImage.cols())
            {
                Log.i(KEY, "Creating new Mat buffer (3)");
                greyscaleImage = new Mat(colorImage.size(), CvType.CV_8UC1);
            }

            int desiredColorBufferSize = colorImage.rows()*colorImage.cols()*colorImage.channels();
            if (this.colorPixelBuffer ==null || this.colorPixelBuffer.length<desiredColorBufferSize)
            {
                Log.i(KEY, "Creating new byte["+desiredColorBufferSize+"] buffer (4)");
                this.colorPixelBuffer = new byte[desiredColorBufferSize];
            }

            int desiredGreyBufferSize = colorImage.rows()*colorImage.cols();
            if (this.singleChannelGreyPixelBuffer==null || this.singleChannelGreyPixelBuffer.length!=desiredGreyBufferSize)
            {
                Log.i(KEY, "Creating new byte["+desiredGreyBufferSize+"] buffer (5)");
                this.singleChannelGreyPixelBuffer = new byte[desiredGreyBufferSize];
            }

            colorImage.get(0, 0, this.colorPixelBuffer);
            int c=this.singleChannel, g=0, channels=colorImage.channels();
            while (g<desiredGreyBufferSize)
            {
                this.singleChannelGreyPixelBuffer[g] = this.colorPixelBuffer[c];
                ++g;
                c+=channels;
            }
            greyscaleImage.put(0, 0, this.singleChannelGreyPixelBuffer);


            return greyscaleImage;
        }
    }

    public static class CmykGreyscaler extends Greyscaler
    {
        private final float cMultiplier, mMultiplier, yMultiplier, kMultiplier;
        private final int singleChannel;
        public CmykGreyscaler(double hueShift, double cMultiplier, double mMultiplier, double yMultiplier, double kMultiplier, boolean invert)
        {
            super(hueShift, invert);
            Log.i(KEY, "Creating CmykGreyscaler");

            this.cMultiplier=(float)cMultiplier;
            this.mMultiplier=(float)mMultiplier;
            this.yMultiplier=(float)yMultiplier;
            this.kMultiplier=(float)kMultiplier;
            if (cMultiplier==1&&mMultiplier==0&&yMultiplier==0&&kMultiplier==0)
            {
                this.singleChannel = 2;
            }
            else if (cMultiplier==0&&mMultiplier==1&&yMultiplier==0&&kMultiplier==0)
            {
                this.singleChannel = 1;
            }
            else if (cMultiplier==0&&mMultiplier==0&&yMultiplier==1&&kMultiplier==0)
            {
                this.singleChannel = 0;
            }
            else if (cMultiplier==0&&mMultiplier==0&&yMultiplier==0&&kMultiplier==1)
            {
                this.singleChannel = 3;
            }
            else
            {
                this.singleChannel = -1;
            }
        }

        @Override
        protected boolean useIntensityShortcut() {
            return false;
        }

        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            if (greyscaleImage==null || greyscaleImage.rows()!=colorImage.rows() || greyscaleImage.cols()!=colorImage.cols())
            {
                Log.i(KEY, "Creating new Mat buffer (6)");
                greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
            }

            /*
            It seems faster to get/set the whole image as an array.
            Here the same array is used as both input and output.
             */

            int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
            if (this.colorPixelBuffer==null || this.colorPixelBuffer.length<desiredBufferSize)
            {
                Log.i(KEY, "Creating new byte["+desiredBufferSize+"] buffer (7)");
                this.colorPixelBuffer = new byte[colorImage.rows() * colorImage.cols() * colorImage.channels()];
            }
            colorImage.get(0, 0, this.colorPixelBuffer);
            float k, r, g, b;
            if (this.singleChannel==-1) {
                for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                    b = (this.colorPixelBuffer[i] & 0xFF) / 255.0f;
                    g = (this.colorPixelBuffer[i + 1] & 0xFF) / 255.0f;
                    r = (this.colorPixelBuffer[i + 2] & 0xFF) / 255.0f;
                    k = Math.min(1 - r, Math.min(1 - g, 1 - b));
                    this.colorPixelBuffer[j] = (byte) (k * kMultiplier * 255);
                    if (k!=1)
                    {
                        this.colorPixelBuffer[j] += (byte) (255 * cMultiplier * (1 - r - k));
                        this.colorPixelBuffer[j] += (byte) (255 * mMultiplier * (1 - g - k));
                        this.colorPixelBuffer[j] += (byte) (255 * yMultiplier * (1 - b - k));
                    }
                }
            }
            else if (this.singleChannel==3) // k only
            {
                for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                    this.colorPixelBuffer[j] = (byte) Math.min(255 - (this.colorPixelBuffer[i] & 0xFF), Math.min(255 - (this.colorPixelBuffer[i+1] & 0xFF), 255 - (this.colorPixelBuffer[i+2] & 0xFF)));
                }
            }
            else // c, y or m only
            {
                float[] bgr = new float[3];
                for (int i = 0, j = 0; i < desiredBufferSize; i += 3, ++j) {
                    bgr[0] = (this.colorPixelBuffer[i] & 0xFF) / 255f;
                    bgr[1] = (this.colorPixelBuffer[i + 1] & 0xFF) / 255f;
                    bgr[2] = (this.colorPixelBuffer[i + 2] & 0xFF) / 255f;

                    k = Math.min(1f - bgr[0], Math.min(1f - bgr[1], 1f - bgr[2]));
                    float result = k==1 ? 0 : (1f - bgr[this.singleChannel] - k);
                    this.colorPixelBuffer[j] = (byte) (result*255f);
                }
            }
            greyscaleImage.put(0, 0, this.colorPixelBuffer);

            return greyscaleImage;
        }
    }

    public static class CmyGreyscaler extends Greyscaler
    {
        private final float cMultiplier, mMultiplier, yMultiplier;
        private final int singleChannel;
        public CmyGreyscaler(double hueShift, double cMultiplier, double mMultiplier, double yMultiplier, boolean invert)
        {
            super(hueShift, invert);
            Log.i(KEY, "Creating CmyGreyscaler");

            this.cMultiplier=(float)cMultiplier;
            this.mMultiplier=(float)mMultiplier;
            this.yMultiplier=(float)yMultiplier;
            if (cMultiplier==1&&mMultiplier==0&&yMultiplier==0)
            {
                this.singleChannel = 2;
            }
            else if (cMultiplier==0&&mMultiplier==1&&yMultiplier==0)
            {
                this.singleChannel = 1;
            }
            else if (cMultiplier==0&&mMultiplier==0&&yMultiplier==1)
            {
                this.singleChannel = 0;
            }
            else
            {
                this.singleChannel = -1;
            }
        }

        @Override
        protected boolean useIntensityShortcut() {
            return false;
        }

        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            if (greyscaleImage==null || greyscaleImage.rows()!=colorImage.rows() || greyscaleImage.cols()!=colorImage.cols())
            {
                Log.i(KEY, "Creating new Mat buffer (7)");
                greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
            }

            int desiredBufferSize = colorImage.rows() * colorImage.cols() * colorImage.channels();
            if (this.colorPixelBuffer==null || this.colorPixelBuffer.length<desiredBufferSize)
            {
                Log.i(KEY, "Creating new byte["+desiredBufferSize+"] buffer (8)");
                this.colorPixelBuffer = new byte[colorImage.rows() * colorImage.cols() * colorImage.channels()];
            }

            colorImage.get(0, 0, this.colorPixelBuffer);
            if (this.singleChannel==-1)
            {
                for (int c = 0, g = 0; c < desiredBufferSize; c += 3, ++g) {
                    this.colorPixelBuffer[g] = (byte) ((1-(this.colorPixelBuffer[c+2] & 0xFF)/255.0f)*this.cMultiplier*255 + (1-(this.colorPixelBuffer[c+1] & 0xFF)/255.0f)*this.mMultiplier*255 + (1-(this.colorPixelBuffer[c] & 0xFF)/255.0f)*this.yMultiplier*255);
                }
            }
            else
            {
                for (int c = 0, g = 0; c < desiredBufferSize; c += 3, ++g) {
                    this.colorPixelBuffer[g] = (byte) (255 - this.colorPixelBuffer[c+this.singleChannel]);
                }
            }
            greyscaleImage.put(0,0,this.colorPixelBuffer);
            return greyscaleImage;
        }
    }


    /**
     * This class enables a given image to be processed concurrently over a number of threads.
     */
    public static class ThreadedGreyscaler extends Greyscaler
    {

        private List<Greyscaler> greyscalers;
        private ExecutorService threadPool;

        /**
         * As Greyscaler objects are not thread safe the number given in the List is also the
         * maximum number of threads that can concurrently process an image.
         * @param greyscalers
         */
        public ThreadedGreyscaler(List<Greyscaler> greyscalers)
        {
            super(0, greyscalers == null || greyscalers.isEmpty() ? false : greyscalers.get(0).invert);
            this.greyscalers = greyscalers;

            threadPool = Executors.newFixedThreadPool(this.greyscalers.size() - 1);
        }

        @Override
        public void release()
        {
            super.release();
            threadPool.shutdown();
            for (Greyscaler greyscaler : greyscalers)
            {
                greyscaler.release();
            }
        }

        @Override
        public Mat greyscaleImage(Mat yuvImage, Mat greyscaleImage)
        {
            // create buffers if they don't already exist as the tasks will need somewhere to put the result:
            int desiredRows = (yuvImage.rows() / 3) * 2, desiredCols = yuvImage.cols();
            if (this.threeChannelBuffer == null || this.threeChannelBuffer.rows() != desiredRows || this.threeChannelBuffer.cols() != desiredCols)
            {
                Log.i(KEY, "Creating new Mat buffer (1)");
                this.threeChannelBuffer = new Mat(desiredRows, desiredCols, CvType.CV_8UC3);
            }
            Imgproc.cvtColor(yuvImage, this.threeChannelBuffer, Imgproc.COLOR_YUV2BGR_NV21);

            if (greyscaleImage == null || greyscaleImage.rows() != desiredRows || greyscaleImage.cols() != desiredCols)
            {
                greyscaleImage = new Mat(desiredRows, desiredCols, CvType.CV_8UC1);
            }

            // Create/run tasks. Each task works on a strip of the image so that the underlying data is continuous (well if the original image was in the first place).
            List<GreyscalerTask> tasks = new ArrayList<>();
            for (int i = 0; i < greyscalers.size(); ++i)
            {
                Greyscaler greyscaler = greyscalers.get(i);
                GreyscalerTask task = new GreyscalerTask(
                        greyscaler,
                        this.threeChannelBuffer.submat(0, desiredRows, (desiredCols / greyscalers.size()) * i, (desiredCols / greyscalers.size()) * (i + 1)),
                        greyscaleImage.submat(0, desiredRows, (desiredCols / greyscalers.size()) * i, (desiredCols / greyscalers.size()) * (i + 1)));
                if (i < greyscalers.size() - 1)
                {
                    tasks.add(task);
                    threadPool.execute(task);
                } else
                {
                    task.run();
                }
            }

            // Wait for the tasks to be completed.
            for (GreyscalerTask task : tasks)
            {
                task.waitForTask();
            }

            if (this.invert)
            {
                Core.bitwise_not(greyscaleImage, greyscaleImage);
            }

            return greyscaleImage;
        }


        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            return null;
        }

        @Override
        protected boolean useIntensityShortcut()
        {
            return false;
        }

        /**
         * This class takes a Greyscaler object and a sub region of an image (e.g.
         * <code>Mat topHalf = mat.submat(0,mat.rows(),0,mat.cols()/2)</code>) so that the whole
         * image can be processed over many threads. Greyscaler objects are not thread safe so
         * don't use the same Greyscaler object in a different GreyscalerTask!
         */
        protected static class GreyscalerTask implements Runnable
        {
            private Greyscaler greyscaler;
            private Mat rgbImage, greyscaleImage;

            private boolean done = false;

            public GreyscalerTask(Greyscaler greyscaler, Mat rgbImage, Mat greyscaleImage)
            {
                this.greyscaler = greyscaler;
                this.rgbImage = rgbImage;
                this.greyscaleImage = greyscaleImage;
            }

            public void run()
            {
                greyscaler.justHueShiftImage(rgbImage, rgbImage);
                greyscaleImage = greyscaler.justGreyscaleImage(rgbImage, greyscaleImage);

                synchronized (this)
                {
                    this.done = true;

                    this.notifyAll();
                }
            }

            public synchronized void waitForTask()
            {
                while (!this.done)
                {
                    try
                    {
                        this.wait();
                    } catch (InterruptedException e)
                    {
                    }
                }
            }
        }
    }
}
