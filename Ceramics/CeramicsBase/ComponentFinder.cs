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
using System.Text;
using System.Drawing;
using System.Drawing.Imaging;

using AForge;
using AForge.Imaging;

namespace Ceramics
{
    struct BoundingBox
    {
        public int x1, y1, x2, y2;
        public BoundingBox(int x1, int x2, int y1, int y2)
        {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }
    }
    class ComponentFinder
    {
        private int[] objectLabels;
        public int[] ObjectLabels
        {
            get
            {
                return objectLabels;
            }
        }

        private int[] map;
        private int[] reMap;

        private int objectCount = 0;
        public int ObjectCount
        {
            get
            {
                return objectCount;
            }
        }

        private BoundingBox[] objectBoundingBoxes;
        public BoundingBox[] ObjectBoundingBoxes
        {
            get
            {
                return objectBoundingBoxes;
            }
        }

        private byte[] objectClassifications;
        public byte[] ObjectClassifications
        {
            get
            {
                return objectClassifications;
            }
        }

        public ComponentFinder()
        {
        }

        public unsafe bool FindBlobs(UnmanagedImage grayBufferUMI)
        {
                if (grayBufferUMI.PixelFormat != PixelFormat.Format8bppIndexed)
                    return false;

                // Apply a connected components algorithm to the thresholded image
                int imageWidth = grayBufferUMI.Width, imageHeight = grayBufferUMI.Height;
                int stride = grayBufferUMI.Stride;

                // objectLabels will eventually contain the component labels for every pixel
                if (objectLabels == null || objectLabels.Length != imageWidth * imageHeight)
                    objectLabels = new int[imageWidth * imageHeight];
                else
                    Array.Clear(objectLabels, 0, objectLabels.Length);

                int labelsCount = 0;

                // Map is used to merge regions during the second pass.
                int maxObjects = ((imageWidth / 2) + 1) * ((imageHeight / 2) + 1) + 1;
                if (map == null || map.Length != maxObjects)
                    map = new int[maxObjects];

                // Self map all regions at the start
                for (int i = 0; i < maxObjects; i++)
                {
                    map[i] = i;
                }

                // Bounding boxes
                if (objectBoundingBoxes == null || objectBoundingBoxes.Length != maxObjects)
                {
                    objectBoundingBoxes = new BoundingBox[maxObjects];
                }

                // Initialise bounding boxes to extremities
                int maxValue = Int32.MaxValue, minValue = Int32.MinValue;
                for (int i = 0; i < maxObjects; i++)
                {
                    objectBoundingBoxes[i] = new BoundingBox(maxValue, minValue, maxValue, minValue);
                }

                // Classifications
                if (objectClassifications == null || objectClassifications.Length != maxObjects)
                {
                    objectClassifications = new byte[maxObjects];
                }

                // Pointer to image data
                byte* src = (byte*)grayBufferUMI.ImageData.ToPointer();
                int p = 0;
                int offset = stride - imageWidth;

                // 1 - for pixels of the first row
                // First pixel at 0,0
                objectLabels[p] = ++labelsCount;
                ++src;
                ++p;

                // Process the rest of the first row
                for (int x = 1; x < imageWidth; x++, src++, p++)
                {
                    // check if the previous pixel is the same binary value
                    if (src[-1] == src[0])
                    {
                        // label current pixel, as the previous
                        objectLabels[p] = objectLabels[p - 1];
                    }
                    else
                    {
                        // create new label
                        objectLabels[p] = ++labelsCount;
                    }
                }
                src += offset;

                // 2 - for other rows
                // for each row
                for (int y = 1; y < imageHeight; y++)
                {
                    // Efficiency escape

                    // for the first pixel of the row, we need to check
                    // only upper and upper-right pixels
                    // check surrounding pixels
                    if (src[-stride] == src[0])
                    {
                        // label current pixel, as the above
                        objectLabels[p] = objectLabels[p - imageWidth];
                    }
                    else if (src[1 - stride] == src[0])
                    {
                        // label current pixel, as the above right
                        objectLabels[p] = objectLabels[p + 1 - imageWidth];
                    }
                    else
                    {
                        // create new label
                        objectLabels[p] = ++labelsCount;
                    }
                    ++src;
                    ++p;

                    // check left pixel and three upper pixels for the rest of pixels
                    for (int x = 1; x < imageWidth - 1; x++, src++, p++)
                    {
                        // check surrounding pixels
                        if (src[-1] == src[0])
                        {
                            // label current pixel, as the left
                            objectLabels[p] = objectLabels[p - 1];
                        }
                        else if (src[-1 - stride] == src[0])
                        {
                            // label current pixel, as the above left
                            objectLabels[p] = objectLabels[p - 1 - imageWidth];
                        }
                        else if (src[-stride] == src[0])
                        {
                            // label current pixel, as the above
                            objectLabels[p] = objectLabels[p - imageWidth];
                        }

                        if (src[1 - stride] == src[0])
                        {
                            if (objectLabels[p] == 0)
                            {
                                // label current pixel, as the above right
                                objectLabels[p] = objectLabels[p + 1 - imageWidth];
                            }
                            else
                            {
                                int l1 = objectLabels[p];
                                int l2 = objectLabels[p + 1 - imageWidth];

                                if ((l1 != l2) && (map[l1] != map[l2]))
                                {
                                    // merge
                                    if (map[l1] == l1)
                                    {
                                        // map left value to the right
                                        map[l1] = map[l2];
                                    }
                                    else if (map[l2] == l2)
                                    {
                                        // map right value to the left
                                        map[l2] = map[l1];
                                    }
                                    else
                                    {
                                        // both values already mapped
                                        map[map[l1]] = map[l2];
                                        map[l1] = map[l2];
                                    }

                                    // reindex
                                    for (int i = 1; i <= labelsCount; i++)
                                    {
                                        if (map[i] != i)
                                        {
                                            // reindex
                                            int j = map[i];
                                            while (j != map[j])
                                            {
                                                j = map[j];
                                            }
                                            map[i] = j;
                                        }
                                    }
                                }
                            }
                        }

                        // label the object if it is not yet
                        if (objectLabels[p] == 0)
                        {
                            // create new label
                            objectLabels[p] = ++labelsCount;
                        }
                    }

                    // for the last pixel of the row, we need to check
                    // only upper and upper-left pixels
                    // check surrounding pixels
                    if (src[-1] == src[0])
                    {
                        // label current pixel, as the left
                        objectLabels[p] = objectLabels[p - 1];
                    }
                    else if (src[-1 - stride] == src[0])
                    {
                        // label current pixel, as the above left
                        objectLabels[p] = objectLabels[p - 1 - imageWidth];
                    }
                    else if (src[-stride] == src[0])
                    {
                        // label current pixel, as the above
                        objectLabels[p] = objectLabels[p - imageWidth];
                    }
                    else
                    {
                        // create new label
                        objectLabels[p] = ++labelsCount;
                    }
                    ++src;
                    ++p;

                    src += offset;
                }

                // allocate remapping array
                if (reMap == null || reMap.Length != map.Length)
                    reMap = new int[map.Length];

                // count objects and prepare remapping array
                int objectsCount = 0;
                for (int i = 1; i <= labelsCount; i++)
                {
                    if (map[i] == i)
                    {
                        // increase objects count
                        reMap[i] = ++objectsCount;
                    }
                }
                // second pass to complete remapping
                for (int i = 1; i <= labelsCount; i++)
                {
                    if (map[i] != i)
                    {
                        reMap[i] = reMap[map[i]];
                    }
                }

                // repair object labels
                int currentLabel = -1;
                src = (byte*)grayBufferUMI.ImageData.ToPointer();
                for (int y = 0; y < imageHeight; y++)
                {
                    int rowIndex = y * imageWidth;
                    for (int x = 0; x < imageWidth; x++)
                    {
                        int positionIndex = rowIndex + x;
                        int regionID = reMap[objectLabels[positionIndex]];
                        objectLabels[positionIndex] = regionID;

                        // Check classification
                        if (regionID != currentLabel)
                        {
                            objectClassifications[regionID] = src[y * stride + x] > 0 ? (byte)1 : (byte)0;
                            currentLabel = regionID;
                        }

                        // Check and update bounding box 
                        // x
                        if (x < objectBoundingBoxes[regionID].x1)
                            objectBoundingBoxes[regionID].x1 = x;
                        if (x > objectBoundingBoxes[regionID].x2)
                            objectBoundingBoxes[regionID].x2 = x;
                        // y
                        if (y < objectBoundingBoxes[regionID].y1)
                            objectBoundingBoxes[regionID].y1 = y;
                        if (y > objectBoundingBoxes[regionID].y2)
                            objectBoundingBoxes[regionID].y2 = y;
                    }
                }
                // Labels from 0 - max inclusive
                objectCount = objectsCount + 1;
                return true;
        }
        
    }
}
