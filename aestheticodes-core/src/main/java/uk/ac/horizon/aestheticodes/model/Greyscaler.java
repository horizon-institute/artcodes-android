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

/**
 * Class containing the method to greyscale an image.
 * This is a base abstract class, use a subclass like Greyscaler.RgbGreyscaler or Greyscaler.CmykGreyscaler.
 */
public abstract class Greyscaler
{
    private final int hueShift;
    private final boolean invert;
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
     */
    public Mat greyscaleImage(Mat yuvImage)
    {
        Mat greyscaleImage = null;
        if (this.useIntensityShortcut())
        {
            Log.i("GREY", "using Intensity Shortcut");
            // cut off the UV components
            greyscaleImage = yuvImage.submat(0, (yuvImage.rows()/3)*2, 0, yuvImage.cols());
        }
        else
        {
            Log.i("GREY", "Not using Intensity Shortcut");
            Mat hueShiftedImage = new Mat((yuvImage.rows()/3)*2, yuvImage.cols(), CvType.CV_8UC3);
            Imgproc.cvtColor(yuvImage, hueShiftedImage, Imgproc.COLOR_YUV2BGR_NV21);
            this.justHueShiftImage(hueShiftedImage, hueShiftedImage);
            greyscaleImage = this.justGreyscaleImage(hueShiftedImage, greyscaleImage);
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

            byte[] pixelData = new byte[colorImage.rows()*colorImage.cols()*colorImage.channels()];
            colorImage.get(0,0,pixelData);
            for (int i=0; i<pixelData.length; i+=colorImage.channels())
            {
                pixelData[i] = (byte) ((pixelData[i] + this.hueShift) % 181);
            }
            colorImage.put(0,0,pixelData);

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


    public static class RgbGreyscaler extends Greyscaler
    {
        private final Mat weight;
        private final boolean useIntensityShortcut;
        private final int singleChannel;
        public RgbGreyscaler(double hueShift, double redMultiplier, double greenMultiplier, double blueMultiplier, boolean invert)
        {
            super(hueShift, invert);
            this.useIntensityShortcut = redMultiplier == 0.299 &&
                    greenMultiplier == 0.587 &&
                    blueMultiplier == 0.114;
            if (redMultiplier==1&&greenMultiplier==0&&blueMultiplier==0)
            {
                this.weight = null;
                this.singleChannel = 2;
            }
            else if (redMultiplier==0&&greenMultiplier==1&&blueMultiplier==0)
            {
                this.weight = null;
                this.singleChannel = 1;
            }
            else if (redMultiplier==0&&greenMultiplier==0&&blueMultiplier==1)
            {
                this.weight = null;
                this.singleChannel = 0;
            }
            else if (!useIntensityShortcut)
            {
                this.singleChannel = -1;
                this.weight = new Mat(1, 3,  CvType.CV_32FC1, new Scalar(0));
                this.weight.put(0, 0, blueMultiplier);
                this.weight.put(0, 1, greenMultiplier);
                this.weight.put(0, 2, redMultiplier);
            }
            else
            {
                this.singleChannel = -1;
                this.weight = null;
            }
        }

        @Override
        protected boolean useIntensityShortcut() {
            return this.useIntensityShortcut;
        }

        @Override
        protected Mat justGreyscaleImage(Mat colorImage, Mat greyscaleImage)
        {
            if (this.singleChannel>=0)
            {
                Log.i("GREY", "Single channel=" + this.singleChannel);
                List<Mat> channels = new ArrayList<>(3);
                Core.split(colorImage, channels);
                return channels.get(this.singleChannel);
            }
            else {
                Log.i("GREY", "Mixed channel");
                if (greyscaleImage==null)
                {
                    greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
                }
                Core.transform(colorImage, greyscaleImage, this.weight);
                return greyscaleImage;
            }
        }
    }


    public static class CmykGreyscaler extends Greyscaler
    {
        private final float cMultiplier, mMultiplier, yMultiplier, kMultiplier;
        private final int singleChannel;
        public CmykGreyscaler(double hueShift, double cMultiplier, double mMultiplier, double yMultiplier, double kMultiplier, boolean invert)
        {
            super(hueShift, invert);
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
            if (greyscaleImage==null)
            {
                greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
            }

            /*
            It seems faster to get/set the whole image as an array.
            Here the same array is used as both input and output.
             */

            byte[] pixelData = new byte[colorImage.rows() * colorImage.cols() * colorImage.channels()];
            colorImage.get(0, 0, pixelData);
            float k, r, g, b;
            if (this.singleChannel==-1) {
                Log.i("GREY", "justGreyscaleImage CMYK mixed channel");
                for (int i = 0, j = 0; i < pixelData.length; i += 3, ++j) {
                    b = pixelData[i] / 255.0f;
                    g = pixelData[i + 1] / 255.0f;
                    r = pixelData[i + 2] / 255.0f;
                    k = Math.min(1 - r, Math.min(1 - g, 1 - b));
                    pixelData[j] = (byte) (k * kMultiplier * 255);
                    pixelData[j] += (byte) (255 * cMultiplier * ((1 - r - k) / (1 - k)));
                    pixelData[j] += (byte) (255 * mMultiplier * ((1 - g - k) / (1 - k)));
                    pixelData[j] += (byte) (255 * yMultiplier * ((1 - b - k) / (1 - k)));
                }
            }
            else if (this.singleChannel==3) // k only
            {
                Log.i("GREY", "justGreyscaleImage CMYK k channel");
                for (int i = 0, j = 0; i < pixelData.length; i += 3, ++j) {
                    pixelData[j] = (byte) Math.min(255 - pixelData[i], Math.min(255 - pixelData[i+1], 255 - pixelData[i+2]));
                }
            }
            else // c, y or m only
            {
                Log.i("GREY", "justGreyscaleImage CMYK single channel");
                float[] bgr = new float[3];
                for (int i = 0, j = 0; i < pixelData.length; i += 3, ++j) {
                    bgr[0] = pixelData[i] / 255.0f;
                    bgr[1] = pixelData[i + 1] / 255.0f;
                    bgr[2] = pixelData[i + 2] / 255.0f;
                    k = Math.min(1 - bgr[0], Math.min(1 - bgr[1], 1 - bgr[2]));
                    pixelData[j] = (byte) (255 * ((1 - bgr[this.singleChannel] - k) / (1 - k)));
                }
            }
            greyscaleImage.put(0, 0, pixelData);

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
            if (greyscaleImage==null)
            {
                greyscaleImage = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
            }

            byte[] pixelData = new byte[colorImage.rows() * colorImage.cols() * colorImage.channels()];
            colorImage.get(0, 0, pixelData);
            if (this.singleChannel==-1)
            {
                Log.i("GREY", "justGreyscaleImage CMY mixed channel");
                for (int i = 0, j = 0; i < pixelData.length; i += 3, ++j) {
                    pixelData[i] = (byte) ((1-pixelData[i+2]/255.0f)*this.cMultiplier*255 + (1-pixelData[i+1]/255.0f)*this.mMultiplier*255 + (1-pixelData[i]/255.0f)*this.yMultiplier*255);
                }
            }
            else
            {
                Log.i("GREY", "justGreyscaleImage CMY single channel");
                for (int i = 0, j = 0; i < pixelData.length; i += 3, ++j) {
                    pixelData[i] = (byte) (255 - pixelData[i+this.singleChannel]);
                }
            }

            return greyscaleImage;
        }
    }
}
