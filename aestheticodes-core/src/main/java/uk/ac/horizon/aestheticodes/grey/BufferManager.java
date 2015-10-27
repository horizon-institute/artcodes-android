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

package uk.ac.horizon.aestheticodes.grey;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an image that can exist in multiple buffers while image processing tasks
 * are executed that may use any of the buffers provided by this class. For tasks that run
 * concurrently on different segments of the image use <code>getSegments()</code> and
 * <code>collapseSegmentMetadata()</code>.
 *
 * <ul>
 * <li>To get the most recent data in a buffer type/color space use the getMostRecentDataInX()
 * methods.</li>
 * <li>If the buffer type is not important use getMostRecentSetData() (e.g. for when you want to
 * invert all bits).</li>
 * <li>To just get a buffer to write to, without coping the most recent data into it, use
 * getXBuffer() methods (these may contain the most recent data or old data but are never
 * null).</li>
 * <li>After writing to a buffer use setXBuffer() to mark it as the most recent data, you can also
 * use these methods to replace the buffer with a different buffer object.</li>
 * </ul>
 */
public class BufferManager
{
    Mat yuvBuffer;
    Mat bgrBuffer;
    Mat greyBuffer;

    boolean ownYuvBuffer = false;
    boolean ownBgrBuffer = false;
    boolean ownGreyBuffer = false;

    Mat mostRecentSetData;

    List<byte[]> byteArrays = new ArrayList<>();

    public void setupWithYuvSourceAndGreyResultBuffer(Mat yuvSource, Mat greyResultBuffer)
    {
        this.yuvBuffer = this.mostRecentSetData = yuvSource;
        this.greyBuffer = greyResultBuffer;

        if (this.greyBuffer!=null)
        {
            int desiredCols = this.yuvBuffer.cols();
            int desiredRows = (this.yuvBuffer.rows() / 3) * 2;
            if (desiredRows != this.greyBuffer.rows() || desiredCols != this.greyBuffer.cols())
            {
                if (desiredCols == this.greyBuffer.rows() && desiredRows == this.greyBuffer.cols())
                {
                    Core.transpose(this.greyBuffer, this.greyBuffer);
                }
                else
                {
                    this.greyBuffer.release();
                    this.greyBuffer = new Mat(this.bgrBuffer.size(), CvType.CV_8UC1);
                }
            }
        }
    }

    public void setupWithBgrSourceAndGreyResultBuffer(Mat bgrSource, Mat greyResultBuffer)
    {
        this.bgrBuffer = this.mostRecentSetData = bgrSource;
        this.greyBuffer = greyResultBuffer;

        if (this.greyBuffer!=null)
        {
            if (this.bgrBuffer.rows() != this.greyBuffer.rows() || this.bgrBuffer.cols() != this.greyBuffer.cols())
            {
                if (this.bgrBuffer.cols() == this.greyBuffer.rows() && this.bgrBuffer.rows() == this.greyBuffer.cols())
                {
                    Core.transpose(this.greyBuffer, this.greyBuffer);
                }
                else
                {
                    this.greyBuffer.release();
                    this.greyBuffer = new Mat(this.bgrBuffer.size(), CvType.CV_8UC1);
                }
            }
        }
    }

    public void release()
    {
        if (ownYuvBuffer)
        {
            yuvBuffer.release();
        }
        if (ownBgrBuffer)
        {
            bgrBuffer.release();
        }
        if (ownGreyBuffer)
        {
            greyBuffer.release();
        }

        yuvBuffer = null;
        bgrBuffer = null;
        greyBuffer = null;
    }

    /**
     * Get the first available byte buffer large enough. Return it with returnByteArray.
     * @param size
     * @return
     */
    public byte[] getByteArray(int size)
    {
        byte[] array = null;
        for (byte[] possibleArray : byteArrays)
        {
            if (possibleArray.length >= size)
            {
                array = possibleArray;
                break;
            }
        }
        if (array!=null)
        {
            return array;
        }
        else
        {
            return new byte[size];
        }
    }
    public void returnByteArray(byte[] array)
    {
        this.byteArrays.add(array);
    }

    public void setYuvBuffer(Mat yuvBuffer)
    {
        this.yuvBuffer = yuvBuffer;
        mostRecentSetData = this.yuvBuffer;
    }

    public void setBgrBuffer(Mat bgrBuffer)
    {
        this.bgrBuffer = bgrBuffer;
        mostRecentSetData = this.bgrBuffer;
    }

    public void setGreyBuffer(Mat greyBuffer)
    {
        this.greyBuffer = greyBuffer;
        mostRecentSetData = this.greyBuffer;
    }

    public Mat getYuvBuffer()
    {
        if (bgrBuffer==null)
        {
            if (greyBuffer!=null)
            {
                yuvBuffer = new Mat((int) ((greyBuffer.rows()/2.0)*3.0), greyBuffer.cols(), CvType.CV_8UC1);
                ownYuvBuffer = true;
            }
            else if (bgrBuffer!=null)
            {
                yuvBuffer = new Mat((int) ((bgrBuffer.rows()/2.0)*3.0), bgrBuffer.cols(), CvType.CV_8UC1);
                ownYuvBuffer = true;
            }
        }
        return yuvBuffer;
    }

    public Mat getBgrBuffer()
    {
        if (bgrBuffer==null)
        {
            if (greyBuffer!=null)
            {
                bgrBuffer = new Mat(greyBuffer.size(), CvType.CV_8UC3);
                ownBgrBuffer = true;
            }
            else if (yuvBuffer!=null)
            {
                bgrBuffer = new Mat((int) ((yuvBuffer.rows()/3.0)*2.0), yuvBuffer.cols(), CvType.CV_8UC3);
                ownBgrBuffer = true;
            }
        }
        return bgrBuffer;
    }

    public Mat getGreyBuffer()
    {
        if (greyBuffer==null)
        {
            if (bgrBuffer!=null)
            {
                greyBuffer = new Mat(bgrBuffer.size(), CvType.CV_8UC1);
                ownGreyBuffer = true;
            }
            else if (yuvBuffer!=null)
            {
                greyBuffer = new Mat((int) ((yuvBuffer.rows()/3.0)*2.0), yuvBuffer.cols(), CvType.CV_8UC1);
                ownGreyBuffer = true;
            }
        }
        return greyBuffer;
    }

    public Mat getMostRecentSetData()
    {
        return this.mostRecentSetData;
    }

    public Mat getMostRecentDataInBgr()
    {
        if (bgrBuffer==null)
        {
            getBgrBuffer();
        }

        if (mostRecentSetData==bgrBuffer)
        {
            return bgrBuffer;
        }
        else if (mostRecentSetData==greyBuffer)
        {
            Imgproc.cvtColor(greyBuffer, bgrBuffer, Imgproc.COLOR_GRAY2BGR);
            return bgrBuffer;
        }
        else if (mostRecentSetData==yuvBuffer)
        {
            Imgproc.cvtColor(yuvBuffer, bgrBuffer, Imgproc.COLOR_YUV2BGR_NV21);
            return bgrBuffer;
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    public Mat getMostRecentDataInGrey()
    {
        if (greyBuffer==null)
        {
            getGreyBuffer();
        }

        if (mostRecentSetData==bgrBuffer)
        {
            Imgproc.cvtColor(bgrBuffer, greyBuffer, Imgproc.COLOR_BGR2GRAY);
            return greyBuffer;
        }
        else if (mostRecentSetData==greyBuffer)
        {
            return greyBuffer;
        }
        else if (mostRecentSetData==yuvBuffer)
        {
            if (greyBuffer != null)
            {
                greyBuffer.release();
            }
            // cut off the UV components
            greyBuffer = yuvBuffer.submat(0, (yuvBuffer.rows()/3)*2, 0, yuvBuffer.cols());
            return greyBuffer;
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    public Mat getMostRecentDataInYuv()
    {
        if (yuvBuffer==null)
        {
            getYuvBuffer();
        }

        if (mostRecentSetData==bgrBuffer)
        {
            Imgproc.cvtColor(bgrBuffer, yuvBuffer, Imgproc.COLOR_BGR2YUV);
            return yuvBuffer;
        }
        else if (mostRecentSetData==greyBuffer)
        {
            // TODO: Copy grey into yuv followed by zeros
            greyBuffer.copyTo(yuvBuffer);
            return yuvBuffer;
        }
        else if (mostRecentSetData==yuvBuffer)
        {
            return yuvBuffer;
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    private List<BufferManager> cachedSegments = null;
    public List<BufferManager> getSegments(int n)
    {
        if (this.cachedSegments == null)
        {
            cachedSegments = new ArrayList<>(n);
            for (int i=0; i<n; ++i)
            {
                cachedSegments.add(new BufferManager());
            }
        }

        this.getMostRecentDataInBgr();
        this.getGreyBuffer();
        int desiredRows = this.bgrBuffer.rows();
        int desiredCols = this.bgrBuffer.cols();

        n = cachedSegments.size();
        for (int i=0; i<n; ++i)
        {
            BufferManager segment = cachedSegments.get(i);
            segment.setupWithBgrSourceAndGreyResultBuffer(
                    this.bgrBuffer.submat(0, desiredRows, (desiredCols / n) * i, (desiredCols / n) * (i + 1)),
                    this.greyBuffer.submat(0, desiredRows, (desiredCols / n) * i, (desiredCols / n) * (i + 1))
            );
            segment.ownBgrBuffer = true;
            segment.ownBgrBuffer = true;
        }

        return cachedSegments;
    }

    public void collapseSegmentMetadata()
    {
        if (this.cachedSegments!=null && !this.cachedSegments.isEmpty())
        {
            BufferManager segment0 = this.cachedSegments.get(0);
            if (segment0.getMostRecentSetData()==segment0.getGreyBuffer())
            {
                this.mostRecentSetData = this.greyBuffer;
            }
            else if (segment0.getMostRecentSetData()==segment0.getBgrBuffer())
            {
                this.mostRecentSetData = this.bgrBuffer;
            }
            else if (segment0.getMostRecentSetData()==segment0.getYuvBuffer())
            {
                this.mostRecentSetData = this.yuvBuffer;
            }

            for (BufferManager buffer : this.cachedSegments)
            {
                buffer.release();
            }
        }
    }
}
