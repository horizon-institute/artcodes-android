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
using System.Xml;

namespace Ceramics
{
    struct MarkerLabel
    {
        public int ID;
        public string Code;
        public MarkerLabel(int id, string code)
        {
            ID = id;
            Code = code;
        }
    }

    class MarkerDetector
    {
        // Constants
        private int minimumBranches;
        private int maximumBranches;
        private int maximumEmptyBranches;
        private int maximumLeaves;
        private byte markerRootClassification;

        public MarkerDetector()
        {
            // Defaults
            minimumBranches = 3;
            maximumBranches = 12;
            maximumEmptyBranches = 1;
            maximumLeaves = 20;
            markerRootClassification = 0;
            LoadConfig();
        }

        public void LoadConfig()
        {
            XmlDocument document = new XmlDocument();
            document.Load("./Config.xml");

            XmlNodeList nodes = document.SelectNodes("Ceramics/Config/Setting");
            foreach (XmlNode n in nodes)
            {
                switch (n.Attributes["key"].Value)
                {
                    case "MinimumBranches":
                        this.minimumBranches = Int32.Parse(n.Attributes["value"].Value);
                        break;
                    case "MaximumBranches":
                        this.maximumBranches = Int32.Parse(n.Attributes["value"].Value);
                        break;
                    case "MaximumEmptyBranches":
                        this.maximumEmptyBranches = Int32.Parse(n.Attributes["value"].Value);
                        break;
                    case "MaximumLeaves":
                        this.maximumLeaves = Int32.Parse(n.Attributes["value"].Value);
                        break;
                    case "RootLevelClass":
                        switch (n.Attributes["value"].Value)
                        {
                            case "Black":
                                markerRootClassification = 0;
                                break;
                            case "White":
                                markerRootClassification = 1;
                                break;
                        }
                        break;
                }
            }
        }

        public List<MarkerLabel> PermitMarkers(List<MarkerLabel> detectedMarkers, List<string> permittedCodes)
        {   
            if (detectedMarkers == null)
                return null;

            List<MarkerLabel> permittedMarkers = new List<MarkerLabel>();
            if (permittedCodes.Count > 0)
            {
                foreach (MarkerLabel marker in detectedMarkers)
                {
                    if (permittedCodes.IndexOf(marker.Code) >= 0)
                        permittedMarkers.Add(new MarkerLabel(marker.ID, marker.Code));
                }
                return permittedMarkers;
            }
            else
            {
                foreach (MarkerLabel marker in detectedMarkers)
                {
                    permittedMarkers.Add(new MarkerLabel(marker.ID, marker.Code));
                }
                return permittedMarkers;
            }
        }

        public List<MarkerLabel> FindMarkers(ComponentFinder componentFinder, RegionAdjacencyGraph regionAdjacencyGraph)
        {
            if (componentFinder.ObjectCount == 0)
            {
                return null;
            }

            List<MarkerLabel> markers = new List<MarkerLabel>();

            int objectCount = componentFinder.ObjectCount;
            List<int>[] adjacencyList = regionAdjacencyGraph.AdjacencyList;
            byte[] objectClassifications = componentFinder.ObjectClassifications;

            for (int i = 0; i < objectCount; i++)
            {
                // Apply root marker rules to determine if the region is a possible marker
                if (objectClassifications[i] == markerRootClassification
                    && adjacencyList[i].Count > minimumBranches
                    && adjacencyList[i].Count <= maximumBranches + 1)
                {
                    // Candidate marker
                    string markerCode = "";
                    if (VerifyRoot(adjacencyList, objectClassifications, i, out markerCode))
                    {
                        markers.Add(new MarkerLabel(i, markerCode));
                    }
                }
                
            }
            return markers;
        }

        private bool VerifyRoot(List<int>[] adjacencyList, byte[] objectClassifications, int candidate, out string markerCode)
        {
            int branchCount = adjacencyList[candidate].Count;
            int[] leafCount = new int[branchCount];
            bool[] verifiedBranches = new bool[branchCount];
            markerCode = "";

            for (int i = 0; i < branchCount; i++)
            {
                int currentLeafCount;
                int candidateBranch = adjacencyList[candidate][i];
                verifiedBranches[i] = VerifyBranch(adjacencyList, candidateBranch, candidate, out currentLeafCount); 
                leafCount[i] = currentLeafCount;
            }

            int falseBranches = 0;
            int emptyBranches = 0;
            for (int i = 0; i < branchCount; i++)
            {
                if (!verifiedBranches[i])
                    falseBranches++;
                else if (leafCount[i] == 0)
                    emptyBranches++;
            }

            if (falseBranches != 1 || emptyBranches > maximumEmptyBranches)
                return false;

            // Generate code
            List<int> code = new List<int>();
            for (int i = 0; i < branchCount; i++)
            {
                if (verifiedBranches[i])
                {
                    code.Add(leafCount[i]);
                }
            }
            code.Sort();
            
            for (int i = 0; i < code.Count - 1; i++)
            {
                markerCode += code[i].ToString() + ":";
            }
            markerCode += code[code.Count - 1].ToString();

            return true;
        }

        private bool VerifyBranch(List<int>[] adjacencyList, int candidate, int parentCandidate, out int leafCount)
        {
            leafCount = 0;
            foreach (int leafID in adjacencyList[candidate])
            {
                if (leafID != parentCandidate)
                {
                    if (VerifyLeaf(adjacencyList[leafID], candidate))
                        leafCount++;
                    else
                        return false;
                }
            }

            if (leafCount > maximumLeaves)
                return false;

            return true;
        }

        private bool VerifyLeaf(List<int> adjacencies, int parentCandidate)
        {
            return (adjacencies.Count == 1 && adjacencies[0] == parentCandidate);
        }


    }
}
