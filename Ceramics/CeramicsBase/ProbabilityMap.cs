/*
    Copyright (c) 2011 University of Nottingham.
    Contact <richard.mortier@nottingham.ac.uk> for more info.
    Original code by Michael Pound <mpp@cs.nott.ac.uk>.

    This file is part of Ceramics.

    Ceramics is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Ceramics is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public
    License along with Ceramics.  If not, see
    <http://www.gnu.org/licenses/>.
*/
﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

namespace Ceramics
{
    class ProbabilityMap
    {
        public int[,] foregroundHistogram;
        public int[,] backgroundHistogram;
        public double[,] foregroundProbabilityMap;
        public double[,] backgroundProbabilityMap;

        int totalF, totalB;

        public ProbabilityMap()
        {
            InitialiseHistograms();
        }

        public ProbabilityMap(String path)
        {
            this.LoadHistograms(path);
        }

        private void InitialiseHistograms()
        {
            this.foregroundHistogram = new int[360, 256];
            this.backgroundHistogram = new int[360, 256];
        }

        unsafe public void TrainHistograms(Bitmap source, Bitmap mask)
        {
            // Validate bitmap sizes
            if (source.Width != mask.Width || source.Height != mask.Height)
                throw new ArgumentException("Training image and mask are of different sizes.");

            BitmapData sourceBD = source.LockBits(new Rectangle(new Point(0, 0), source.Size), ImageLockMode.ReadOnly, PixelFormat.Format24bppRgb);
            BitmapData maskBD = mask.LockBits(new Rectangle(new Point(0, 0), mask.Size), ImageLockMode.ReadOnly, PixelFormat.Format24bppRgb);

            try
            {
                int width = sourceBD.Width;
                int height = sourceBD.Height;
                int stride = sourceBD.Stride;

                double hue, saturation, value;

                byte* src = (byte*)sourceBD.Scan0.ToPointer();
                byte* msk = (byte*)maskBD.Scan0.ToPointer();
                for (int y = 0; y < height; y++)
                {
                    for (int x = 0; x < width; x++)
                    {
                        int pixel = y * stride + x * 3;

                        // Red mask
                        if (msk[pixel + 2] == 255 && msk[pixel + 1] == 0 && msk[pixel] == 0)
                        {
                            RGBToHSV(src[pixel+2], src[pixel + 1], src[pixel], out hue, out saturation, out value);
                            this.foregroundHistogram[(int)hue, (int)(saturation * 255)]++;
                            totalF++;
                        }
                        // Blue mask
                        else if (msk[pixel + 2] == 0 && msk[pixel + 1] == 0 && msk[pixel] == 255)
                        {
                            RGBToHSV(src[pixel+2], src[pixel + 1], src[pixel], out hue, out saturation, out value);
                            this.backgroundHistogram[(int)hue, (int)(saturation * 255)]++;
                            totalB++;
                        }
                    }
                }
            }
            finally
            {
                source.UnlockBits(sourceBD);
                mask.UnlockBits(maskBD);
            }

        }

        public void CreateMaps()
        {
            // Initialise
            this.foregroundProbabilityMap = new double[360, 256];
            this.backgroundProbabilityMap = new double[360, 256];

            // Normalise
            for (int h = 0; h < 360; h++)
            {
                for (int s = 0; s < 256; s++)
                {
                    this.foregroundProbabilityMap[h, s] = this.foregroundHistogram[h, s] / (double)this.totalF;
                    this.backgroundProbabilityMap[h, s] = this.backgroundHistogram[h, s] / (double)this.totalB;
                }
            }

            // Apply Gaussian Blur
            GaussianBlur(30.0f, this.foregroundProbabilityMap);
            GaussianBlur(30.0f, this.backgroundProbabilityMap);
        }
        
        private void GaussianBlur(float sd, double[,] probabilityMap)
        {
            const double pi = 3.14159;
            const double e = 2.71828;

            double[,] intermediateMap = new double[360, 256];

            // Determine appropriate window size for mask
            int wSize = (int)((5 * sd) + 0.5);
            if (wSize < 3)
                wSize = 3;
            else if (!isOdd(wSize))
            {
                if ((5 * sd) < wSize) wSize--; else wSize++;
            }

            // Calculate static part of gaussian distribution formula
            double f1 = 1.0 / (sd * Math.Sqrt(2.0 * pi));

            // Initialise and calculate the gaussian distribution mask
            int ex = (int)((float)wSize / 2.0);
            double[] gMask = new double[wSize];

            for (int r = -ex; r <= ex; r++)
                gMask[r + ex] = f1 * Math.Pow(e, -Math.Pow(r, 2.0) / (2.0 * Math.Pow(sd, 2.0)));

            // Apply mask to map horizontally
            double totalWeight = 0.0;
            double totalProbability = 0.0;
            for (int y = 0; y < 256; y++)
            {
                for (int x = 0; x < 360; x++)
                {
                    totalWeight = 0.0;
                    totalProbability = 0.0;

                    // Apply the gaussian filter horizontally
                    for (int u = -ex; u <= ex; u++)
                    {
                        if (x + u >= 0 && x + u < 360)
                        {
                            totalWeight = totalWeight + gMask[u + ex];
                            totalProbability = totalProbability + (gMask[u + ex] * probabilityMap[x + u, y]);
                        }
                        else if (x + u < 0)
                        {
                            totalWeight = totalWeight + gMask[u + ex];
                            totalProbability = totalProbability + (gMask[u + ex] * probabilityMap[x + u + 360, y]);
                        }
                        else
                        {
                            totalWeight = totalWeight + gMask[u + ex];
                            totalProbability = totalProbability + (gMask[u + ex] * probabilityMap[x + u - 360, y]);
                        }
                    }
                    intermediateMap[x, y] = totalProbability / totalWeight;
                }
            }

            // Apply mask to intermediate map vertically
            for (int y = 0; y < 256; y++)
            {
                for (int x = 0; x < 360; x++)
                {
                    totalWeight = 0.0;
                    totalProbability = 0.0;

                    // Apply the gaussian filter horizontally
                    for (int v = -ex; v <= ex; v++)
                    {
                        if (y + v >= 0 && y + v < 256)
                        {
                            totalWeight = totalWeight + gMask[v + ex];
                            totalProbability = totalProbability + (gMask[v + ex] * probabilityMap[x, y + v]);
                        }
                    }
                    probabilityMap[x, y] = totalProbability / totalWeight;
                }
            }
        }

        private bool isOdd(int i)
        {
            return ((float)i / 2) != (int)((float)i / 2);
        }

        // Save and Load functions
        public void SaveHistograms(String path)
        {
            FileStream fileStream = null;
            BinaryWriter streamWriter = null;
            try
            {

                fileStream = new FileStream(path, FileMode.OpenOrCreate, FileAccess.Write);
                streamWriter = new BinaryWriter(fileStream);

                for (int h = 0; h < 360; h++)
                {
                    for (int s = 0; s < 256; s++)
                    {
                        streamWriter.Write(this.foregroundHistogram[h, s]);
                    }
                }
                streamWriter.Flush();
                for (int h = 0; h < 360; h++)
                {
                    for (int s = 0; s < 256; s++)
                    {
                        streamWriter.Write(this.backgroundHistogram[h, s]);
                    }
                }
                streamWriter.Flush();
            }
            finally
            {
                // Close streams
                if (streamWriter != null)
                    streamWriter.Close();
            }
        }

        public void LoadHistograms(String path)
        {
            FileStream fileStream = null;
            BinaryReader streamReader = null;
            try
            {

                fileStream = new FileStream(path, FileMode.Open, FileAccess.Read);
                streamReader = new BinaryReader(fileStream);
                this.foregroundHistogram = new int[360, 256];
                this.backgroundHistogram = new int[360, 256];

                this.totalF = 0;
                this.totalB = 0;

                for (int h = 0; h < 360; h++)
                {
                    for (int s = 0; s < 256; s++)
                    {
                        Int32 value = streamReader.ReadInt32();
                        this.foregroundHistogram[h, s] = value;
                        totalF += value;
                    }
                }
                
                for (int h = 0; h < 360; h++)
                {
                    for (int s = 0; s < 256; s++)
                    {
                        Int32 value = streamReader.ReadInt32();
                        this.backgroundHistogram[h, s] = value;
                        totalB += value;
                    }
                }
                
            }
            finally
            {
                // Close streams
                if (streamReader != null)
                    streamReader.Close();
            }
        }

        // Miscellaneous Functions for HSV <-> RGB
        public static void RGBToHSV(int red, int green, int blue, out double hue, out double saturation, out double value)
        {
            int max = Math.Max(red, Math.Max(blue, green));
            int min = Math.Min(red, Math.Min(blue, green));

            hue = Color.FromArgb(red,green,blue).GetHue();
            saturation = (max == 0) ? 0 : 1d - (1d * min / max);
            value = max / 255d;
        }

        public static Color ColorFromHSV(double hue, double saturation, double value)
        {
            int hi = Convert.ToInt32(Math.Floor(hue / 60)) % 6;
            double f = hue / 60 - Math.Floor(hue / 60);

            value = value * 255;
            int v = Convert.ToInt32(value);
            int p = Convert.ToInt32(value * (1 - saturation));
            int q = Convert.ToInt32(value * (1 - f * saturation));
            int t = Convert.ToInt32(value * (1 - (1 - f) * saturation));

            if (hi == 0)
                return Color.FromArgb(255, v, t, p);
            else if (hi == 1)
                return Color.FromArgb(255, q, v, p);
            else if (hi == 2)
                return Color.FromArgb(255, p, v, t);
            else if (hi == 3)
                return Color.FromArgb(255, p, q, v);
            else if (hi == 4)
                return Color.FromArgb(255, t, p, v);
            else
                return Color.FromArgb(255, v, p, q);
        }

        unsafe public void TresholdImage(AForge.Imaging.UnmanagedImage source, AForge.Imaging.UnmanagedImage destination)
        {
            if (source.Width != destination.Width || source.Height != destination.Height)
                return;


            // Pointer to image data
            byte* src = (byte*)source.ImageData.ToPointer();
            byte* dst = (byte*)destination.ImageData.ToPointer();
            int srcStride = source.Stride;
            int dstStride = destination.Stride;

            int width = source.Width;
            int height = source.Height;
            double hue, sat, val;
            int HueIndex = 0, SatIndex = 0;

            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    int srcPixel = y * srcStride + x * 4;
                    int dstPixel = y * dstStride + x;

                    RGBToHSV(src[srcPixel+2], src[srcPixel+1], src[srcPixel], out hue, out sat, out val);
                    HueIndex = (int)hue;
                    SatIndex = (int)sat *255;

                    if (this.foregroundProbabilityMap[HueIndex, SatIndex] > this.backgroundProbabilityMap[HueIndex, SatIndex])
                        dst[dstPixel] = 0;
                    else
                        dst[dstPixel] = 255;

                }
            }

        }
    }
}
