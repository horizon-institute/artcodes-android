package uk.ac.horizon.tableware;

import java.util.ArrayList;

import org.opencv.core.Mat;

class MarkerLabel{
    public int ID;
    public String Code;
    public MarkerLabel(int id, String code)
    {
        ID = id;
        Code = code;
    }
}

class MarkerDetector
{
	//indexes of leaf nodes in contour tree hierarchy.
	public static final int NEXT_NODE = 0;
	public static final int PREV_NODE = 1;
	public static final int FIRST_NODE = 2;
	public static final int PARENT_NODE = 3;
	
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
	}
	
	public Boolean verifyRoot(int rootIndex, Mat hierarchy){
		Boolean valid = false;
		int branchCount = 0;
		int currentBranchIndex = -1;
		//get the nodes of the root node.
		double[] nodes = hierarchy.get(0, rootIndex);
		//get the first child node.
		currentBranchIndex = (int)nodes[FIRST_NODE];
		
		//if there is a branch node then verify branches.
		if (currentBranchIndex >= 0 ){
			//loop until there is a branch node.
			while(currentBranchIndex >= 0){
				//verify current branch.
				if (verifyBranch(currentBranchIndex, hierarchy)){
					branchCount++;
					//get next node. 
					nodes = hierarchy.get(0, currentBranchIndex);
					currentBranchIndex = (int)nodes[NEXT_NODE];
				}else
					return false;
			}
			
			if (branchCount >= minimumBranches && branchCount <= maximumBranches){
				valid = true;
			}
		}
		return valid;
	}

	private Boolean verifyBranch(int branchIndex, Mat hierarchy){
		Boolean valid = false;
		int leafCount = 0;
		int currentLeafIndex = -1;
		
		//get first leaf node.
		double[] nodes = hierarchy.get(0, branchIndex);
		currentLeafIndex = (int)nodes[FIRST_NODE];
		if (currentLeafIndex >= 0){
			//loop until there is a leaf node.
			while(currentLeafIndex >= 0){
				if (verifyLeaf(currentLeafIndex, hierarchy)){
					leafCount++;
					//get next leaf node.
					nodes = hierarchy.get(0, currentLeafIndex);
					currentLeafIndex = (int)nodes[NEXT_NODE];
				}else{
					return false;
				}
			}
			if (leafCount <= maximumLeaves){
				valid = true;
			}
		}
		return valid;
	}

	/*
	private Boolean verifyBranch(int branchIndex, Mat hierarchy){
		Boolean valid = false;
		int leafCount = 0;
		int currentLeafIndex = -1;
		
		//get first leaf node.
		double[] nodes = hierarchy.get(0, branchIndex);
		currentLeafIndex = (int)nodes[FIRST_NODE];
		if (currentLeafIndex >= 0){
			//loop until there is a leaf node.
			while(currentLeafIndex >= 0){
				if (verifyLeaf(currentLeafIndex, hierarchy)){
					leafCount++;
					//get next leaf node.
					nodes = hierarchy.get(0, currentLeafIndex);
					currentLeafIndex = (int)nodes[NEXT_NODE];
				}else{
					//branch is not valid.
					return false;
				}
			}
			if (leafCount > 0 && leafCount <= maximumLeaves){
				valid = true;
			}
		}
		return valid;
	}*/
	
	private Boolean verifyLeaf(int leafIndex, Mat hierarchy){
		Boolean valid = true;
		//Get nodes of branch index.
		double[] nodes = hierarchy.get(0, leafIndex);
		//check if there is no child node.
		if (nodes[FIRST_NODE] >= 0){
			valid = false;
		}
		return valid;
	}
	
	/*
	public Boolean FindMarkers(ArrayList<Mat> contours, Mat hierachy){
		VerifyRoot();
		return true;
	}*/

	/*
	public List<MarkerLabel> PermitMarkers(List<MarkerLabel> detectedMarkers, List<String> permittedCodes)
	{   
		if (detectedMarkers == null)
			return null;

		List<MarkerLabel> permittedMarkers = new ArrayList<MarkerLabel>();
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

		List<MarkerLabel> markers = new ArrayList<MarkerLabel>();

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
*/

}
	
