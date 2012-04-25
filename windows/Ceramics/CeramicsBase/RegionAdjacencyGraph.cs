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

namespace Ceramics
{
    class RegionAdjacencyGraph
    {
        private byte[,] adjacencyMatrix;
        public byte[,] AdjacencyMatrix
        {
            get
            {
                return adjacencyMatrix;
            }
        }

        private List<int>[] adjacencyList;
        public List<int>[] AdjacencyList
        {
            get
            {
                return adjacencyList;
            }
        }

        private int regionCount = 0;
        private int regionCountMax = 0;

        public RegionAdjacencyGraph(int imageWidth, int imageHeight)
        {
            // Limit the size of the graph to w*h/100 possible components.
            regionCountMax = (imageWidth * imageHeight) / 200;
            adjacencyList = new List<int>[regionCountMax];
            for (int i = 0; i < regionCountMax; i++)
                adjacencyList[i] = new List<int>();

            adjacencyMatrix = new byte[regionCountMax, regionCountMax];
        }

        public bool ConstructGraph(int[] objectLabels, int objectCount, int imageWidth, int imageHeight)
        {
            // Begins by populating an adjacency matrix for speed. Then converts to an adjacency list for fast subtree isomorphism later.
            if (objectCount > regionCountMax)
                return false;
            
            regionCount = objectCount;

            // Wipe matrix and list from previous frame
            Array.Clear(adjacencyMatrix, 0, regionCountMax * regionCountMax);
            for (int i = 0; i < regionCount; i++)
                adjacencyList[i].Clear();

            // Construct matrix:
            for (int y = 1; y < imageHeight; y++)
            {
                int rowIndex = y * imageWidth;
                for (int x = 1; x < imageWidth; x++)
                {
                    int pixelIndex = rowIndex + x;
                    int pixelRegion = objectLabels[pixelIndex];
                    // Check neighbour regions
                    // Top
                    if (objectLabels[pixelIndex - imageWidth] != pixelRegion)
                    {
                        adjacencyMatrix[pixelRegion, objectLabels[pixelIndex - imageWidth]] = 1;
                        adjacencyMatrix[objectLabels[pixelIndex - imageWidth], pixelRegion] = 1;
                    }
                    /*
                    // Right
                    if (objectLabels[pixelIndex + 1] != pixelRegion)
                    {
                        adjacencyMatrix[pixelRegion, objectLabels[pixelIndex + 1]] = 1;
                        adjacencyMatrix[objectLabels[pixelIndex + 1], pixelRegion] = 1;
                    }
                    // Bottom
                    if (objectLabels[pixelIndex + imageWidth] != pixelRegion)
                    {
                        adjacencyMatrix[pixelRegion, objectLabels[pixelIndex + imageWidth]] = 1;
                        adjacencyMatrix[objectLabels[pixelIndex + imageWidth], pixelRegion] = 1;
                    }
                    */
                    // Left
                    if (objectLabels[pixelIndex - 1] != pixelRegion)
                    {
                        adjacencyMatrix[pixelRegion, objectLabels[pixelIndex - 1]] = 1;
                        adjacencyMatrix[objectLabels[pixelIndex - 1], pixelRegion] = 1;
                    }
                }
            }
            
            for (int row = 0; row < regionCount; row++)
            {
                for (int col = 0; col < regionCount; col++)
                {
                    if (adjacencyMatrix[col, row] == 1)
                    {
                        adjacencyList[col].Add(row);
                    }
                }
            }
            return true;
        }




    }
}
