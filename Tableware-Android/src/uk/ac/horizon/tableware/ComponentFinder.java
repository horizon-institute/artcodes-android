package uk.ac.horizon.tableware;

import java.util.Arrays;
import org.opencv.core.Mat;

class BoundingBox
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
    private int[] map;
    private int[] reMap;
    private int objectCount = 0;
    private BoundingBox[] objectBoundingBoxes;
    private byte[] objectClassifications;
    
    public int[] getObjectLabels(){
    	return objectLabels;
    }
     
    public int getObjectCount(){
    	return objectCount;
    }

    public BoundingBox[] getObjectBoundingBoxes(){
        return objectBoundingBoxes;
    }

    public byte[] getObjectClassifications(){
        return objectClassifications;
    }

    // Apply a connected components algorithm to the thresholded image
    public Boolean FindBlobs(Mat imgMat)
    {
        //size of matrix in terms of numbers of cols and rows.
    	int imageWidth = imgMat.width(), imageHeight = imgMat.height();
        
        // objectLabels will eventually contain the component labels for every pixel
        if (objectLabels == null || objectLabels.length != imageWidth * imageHeight)
            objectLabels = new int[imageWidth * imageHeight];
        else
        	//initialise array from previous iteration. 
            Arrays.fill(objectLabels, 0);

        int labelsCount = 0;

        // Map is used to merge regions during the second pass.
        int maxObjects = ((imageWidth / 2) + 1) * ((imageHeight / 2) + 1) + 1;
        if (map == null || map.length != maxObjects)
            map = new int[maxObjects];

        // Self map all regions at the start
        for (int i = 0; i < maxObjects; i++)
        {
            map[i] = i;
        }

        // Bounding boxes
        if (objectBoundingBoxes == null || objectBoundingBoxes.length != maxObjects)
        {
            objectBoundingBoxes = new BoundingBox[maxObjects];
        }

        // Initialise bounding boxes to extremities
        int maxValue = Integer.MAX_VALUE, minValue = Integer.MIN_VALUE;
        for (int i = 0; i < maxObjects; i++)
        {
            objectBoundingBoxes[i] = new BoundingBox(maxValue, minValue, maxValue, minValue);
        }

        // Classifications
        if (objectClassifications == null || objectClassifications.length != maxObjects)
        {
            objectClassifications = new byte[maxObjects];
        }

        int pixIndex = 0;
        int row = 0;
        int col = 0;
        
        // 1 - for pixels of the first row
        // First pixel at 0,0
        objectLabels[pixIndex] = ++labelsCount;
        ++pixIndex;

        // Process the rest of the first row. In first row, it starts scanning from the second column.
        for (col = 1; col < imageWidth; col++, pixIndex++)
        {
            // check if the previous pixel has the same binary value
            if (imgMat.get(row, col - 1)[0] == imgMat.get(row, col)[0])
            {
                // label current pixel, as the previous
                objectLabels[pixIndex] = objectLabels[pixIndex - 1];
            }
            else
            {
                // create new label
                objectLabels[pixIndex] = ++labelsCount;
            }
        }
        
        // 2 - for other rows
        // for each row
        //for (row = 1; row < imageHeight ; row++)
        long timebefore = System.currentTimeMillis();
        
        for (row = 1; row < imageHeight ; row++)
        {
            
        	// Efficiency escape
        	// for the first pixel of the row, we need to check only upper and upper-right pixels
        	//set to first column
        	col = 0;
            //First check with upper pixel.
        	if (imgMat.get(row -1, col)[0] == imgMat.get(row, col)[0])
            {
                // label current pixel, as the above
                objectLabels[pixIndex] = objectLabels[pixIndex - imageWidth];
            }
            //Otherwise check with upper right pixel.
        	else if (imgMat.get(row -1 , col)[0] == imgMat.get(row, col)[0])
            {
                // label current pixel, as the above right
                objectLabels[pixIndex] = objectLabels[pixIndex + 1 - imageWidth];
            }
        	//if none of them are equal then assign a new label. 
            else
            {
                // create new label
                objectLabels[pixIndex] = ++labelsCount;
            }
            
        	++pixIndex;

            // check left, upper left, upper and upper right pixels for the rest of the pixels.
        	//Iterate from second to second last column.
            for (col = 1; col < imageWidth - 1; col++, pixIndex++)
            {
                // check surrounding pixels
            	// check if the left pixel has the same binary value
                if (imgMat.get(row, col - 1)[0] == imgMat.get(row, col)[0]){
                    // label current pixel, as the left pixel.
                    objectLabels[pixIndex] = objectLabels[pixIndex - 1];
                }
                //otherwise check if the upper left pixel has same binary value.  
                else if (imgMat.get(row -1 , col - 1)[0] == imgMat.get(row, col)[0]){
                	objectLabels[pixIndex] = objectLabels[pixIndex - 1 - imageWidth];
                }
                //otherwise check if the upper pixel has same binary value.  
                else if (imgMat.get(row -1 , col)[0] == imgMat.get(row, col)[0]){
                	objectLabels[pixIndex] = objectLabels[pixIndex - imageWidth];
                }
                //check upper right pixel. 
                if (imgMat.get(row -1, col + 1)[0] == imgMat.get(row, col)[0]) 
                {
                    //if current pixel does not have a label.
                	if (objectLabels[pixIndex] == 0)
                    {
                        // label current pixel, as the upper right
                        objectLabels[pixIndex] = objectLabels[pixIndex + 1 - imageWidth];
                    }
                    //if current pixel has already a label.
                	else
                    {
                        int currentPixLabel = objectLabels[pixIndex];
                        int upperPixLabel = objectLabels[pixIndex + 1 - imageWidth];

                        if ((currentPixLabel != upperPixLabel) && (map[currentPixLabel] != map[upperPixLabel]))
                        {
                            // merge
                            if (map[currentPixLabel] == currentPixLabel)
                            {
                                // map current value to the upper right
                                map[currentPixLabel] = map[upperPixLabel];
                            }
                            else if (map[upperPixLabel] == upperPixLabel)
                            {
                                // map right value to the current
                                map[upperPixLabel] = map[currentPixLabel];
                            }
                            else
                            {
                                // both values are already mapped
                                map[map[currentPixLabel]] = map[upperPixLabel];
                                map[currentPixLabel] = map[upperPixLabel];
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
                if (objectLabels[pixIndex] == 0)
                {
                    // create new label
                    objectLabels[pixIndex] = ++labelsCount;
                }
            }

            // for the last pixel of the row, we need to check left, upper and upper-left pixels.
            //check left pixel.
            if (imgMat.get(row, col - 1)[0] == imgMat.get(row, col)[0])
            {
                // label current pixel, as the left
                objectLabels[pixIndex] = objectLabels[pixIndex - 1];
            }
            //check upper left pixel.
            else if (imgMat.get(row - 1, col - 1)[0] == imgMat.get(row, col)[0])
            {
                // label current pixel, as the above left
                objectLabels[pixIndex] = objectLabels[pixIndex - 1 - imageWidth];
            }
            //check upper pixel.
            else if (imgMat.get(row - 1, col)[0] == imgMat.get(row, col)[0])
            {
                // label current pixel, as the above
                objectLabels[pixIndex] = objectLabels[pixIndex - imageWidth];
            }
            else
            {
                // create new label
                objectLabels[pixIndex] = ++labelsCount;
            }
            
            ++pixIndex;
        }
        
        long timeafter = System.currentTimeMillis();
        
        long totaltime = timeafter - timebefore;
        System.out.println(totaltime);
        // allocate remapping array
        if (reMap == null || reMap.length != map.length)
            reMap = new int[map.length];

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
        for (row = 0; row < imageHeight; row++)
        {
            int rowIndex = row * imageWidth;
            for (col = 0; col < imageWidth; col++)
            {
                int positionIndex = rowIndex + col;
                int regionID = reMap[objectLabels[positionIndex]];
                objectLabels[positionIndex] = regionID;

                // Check classification
                if (regionID != currentLabel)
                {
                    objectClassifications[regionID] = imgMat.get(row, col)[0] > 0 ? (byte)1 : (byte)0;
                    currentLabel = regionID;
                }

                // Check and update bounding box 
                // x
                if (col < objectBoundingBoxes[regionID].x1)
                    objectBoundingBoxes[regionID].x1 = col;
                if (col > objectBoundingBoxes[regionID].x2)
                    objectBoundingBoxes[regionID].x2 = col;
                // y
                if (row < objectBoundingBoxes[regionID].y1)
                    objectBoundingBoxes[regionID].y1 = row;
                if (row > objectBoundingBoxes[regionID].y2)
                    objectBoundingBoxes[regionID].y2 = row;
            }
        }
        
        // Labels from 0 - max inclusive
        objectCount = objectsCount + 1;
        return true;
    }
}	
