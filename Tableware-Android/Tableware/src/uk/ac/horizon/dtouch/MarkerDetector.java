package uk.ac.horizon.dtouch;

import java.util.Collections;
import java.util.List;

import org.opencv.core.Mat;

import uk.ac.horizon.tableware.TWPreference;

import android.content.Context;

class MarkerLabel{
    public int ID;
    public String Code;
    public MarkerLabel(int id, String code)
    {
        ID = id;
        Code = code;
    }
}

enum BranchStatus{
	INVALID,
	EMPTY,
	VALID
}

public class MarkerDetector
{
	//indexes of leaf nodes in contour tree hierarchy.
	private static final int NEXT_NODE = 0;
	private static final int FIRST_NODE = 2;
		
	private TWPreference preference;
			
	public MarkerDetector(Context context)
	{
		preference = new TWPreference(context);
	}
	
	public Boolean verifyRoot(int rootIndex, Mat rootNode, Mat hierarchy, Mat binaryImage, List<Integer> codes){
		Boolean valid = false;
		int branchCount = 0;
		int emptyBranchCount = 0;
		int currentBranchIndex = -1;
		BranchStatus status;
				
		//get the nodes of the root node.
		double[] nodes = hierarchy.get(0, rootIndex);
		//get the first child node.
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
				if (verifyMarkerConstraint(codes)){
					Collections.sort(codes);
					valid = true;
				}else
					valid = false;
			}
		}
		return valid;
	}
	
		
	private BranchStatus verifyBranch(int branchIndex, Mat hierarchy, List<Integer> codes){
		int leafCount = 0;
		int currentLeafIndex = -1;
		BranchStatus status = BranchStatus.INVALID;
		
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
		return status;
	}

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
	
	private Boolean verifyMarkerConstraint(List<Integer> codes){
		MarkerConstraint markerConstraint = new MarkerConstraint(preference,codes);
		return markerConstraint.verifyMarkerCode();
	}
}
