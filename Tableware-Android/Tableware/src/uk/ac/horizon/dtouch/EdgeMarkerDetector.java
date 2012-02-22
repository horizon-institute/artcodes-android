package uk.ac.horizon.dtouch;

import java.util.Collections;
import java.util.List;

import org.opencv.core.Mat;

import uk.ac.horizon.tableware.TWPreference;

import android.content.Context;

class EdgeMarkerDetector
{
	//indexes of leaf nodes in contour tree hierarchy.
	private static final int NEXT_NODE = 0;
	private static final int FIRST_NODE = 2;
		
	private TWPreference preference;
			
	public EdgeMarkerDetector(Context context)
	{
		preference = new TWPreference(context);
	}
	
	public Boolean verifyRoot(int rootIndex, Mat rootNode, Mat hierarchy, Mat binaryImage, List<Integer> codes){
		Boolean valid = false;
		int branchCount = 0;
		int emptyBranchCount = 0;
		int interiorRootNodeIndex = -1;
		int currentBranchIndex = -1;
		BranchStatus status;
		
		//get the nodes of the root node.
		double[] nodes = hierarchy.get(0, rootIndex);
		//get the child node.
		interiorRootNodeIndex = (int)nodes[FIRST_NODE];

		//The first child node is an interior node.
		if (interiorRootNodeIndex >= 0){
			//find the branch node.
			nodes = hierarchy.get(0, interiorRootNodeIndex);
			currentBranchIndex = (int)nodes[FIRST_NODE];
			
			//if there is a branch node then verify branches.
			if (currentBranchIndex >= 0 ){
				//loop until there is a branch node.
				while(currentBranchIndex >= 0){
					//verify current branch.
					status = verifyBranch(currentBranchIndex, hierarchy, codes);
					//if branch is valid or empty.
					if (status == BranchStatus.VALID || status == BranchStatus.EMPTY ){
						branchCount++;
						if (status == BranchStatus.EMPTY){
							emptyBranchCount++;
							if (emptyBranchCount > preference.getMaxEmptyBranches()){
								return false;
							}
						}
						//get next node. 
						nodes = hierarchy.get(0, currentBranchIndex);
						currentBranchIndex = (int)nodes[NEXT_NODE];
					}
					else if (status == BranchStatus.INVALID)
						return false;
				}
				if (emptyBranchCount > preference.getMaxEmptyBranches())
					valid = false;
				else if (branchCount >= preference.getMinBranches() && branchCount <= preference.getMaxBranches()){
					Collections.sort(codes);
					valid = true;
				}
			}
		}
		return valid;
	}
	
	private BranchStatus verifyBranch(int branchIndex, Mat hierarchy, List<Integer> codes){
		int leafCount = 0;
		int currentLeafIndex = -1;
		int interiorBranchNodeIndex = -1;
		BranchStatus status = BranchStatus.INVALID;
		
		//get interior branch node.
		double[] nodes = hierarchy.get(0, branchIndex);
		interiorBranchNodeIndex = (int)nodes[FIRST_NODE];
		
		if (interiorBranchNodeIndex >= 0)
		{
			//get first leaf node.
			nodes = hierarchy.get(0, interiorBranchNodeIndex);
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
						status = BranchStatus.INVALID;
						return status;
					}
				}
			}
			//if no leaf then the branch is empty.
			if (leafCount == 0)
				status = BranchStatus.EMPTY;
			else if (leafCount <= preference.getMaxLeaves())
				status = BranchStatus.VALID;
			//add branch code.
			codes.add(leafCount);
		}
		return status;
	}

	private Boolean verifyLeaf(int leafIndex, Mat hierarchy){
		Boolean valid = false;
		int interiorLeafIndex = -1;
		//Get interior leaf node.
		double[] nodes = hierarchy.get(0, leafIndex);
		interiorLeafIndex = (int)nodes[FIRST_NODE];
		if (interiorLeafIndex >= 0){
			//Get nodes of branch index.
			nodes = hierarchy.get(0, interiorLeafIndex);
			//check if there is no child node.
			if (nodes[FIRST_NODE] == -1){
				valid = true;
			}
		}
		return valid;
	}	
}

