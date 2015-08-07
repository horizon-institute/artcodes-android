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

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

import uk.ac.horizon.aestheticodes.controllers.MatTranform;

public class Thumbnail {

    private final Mat cvThumbnail;
    private Bitmap bitmap = null;

    /**
     * Create a thumbnail image of an artcode marker that can be used in the UI.
     * @param markerIndex The index of the marker.
     * @param scene The scene data.
     * @param width The width of the thumbnail (does not need to match the marker) or -1 to get the width from the markers aspect ratio.
     * @param height The height of the thumbnail (does not need to match the marker) or -1 to get the height from the markers aspect ratio (if both width & height are -1 then the markers resolution is used).
     * @param color The colour to draw the marker in, the background will be transparent.
     */
    public Thumbnail(int markerIndex, Scene scene, int width, int height, Scalar color)
    {
        if (scene.isRotated())
        {
            int temp = height;
            height = width;
            width = temp;
        }

        // work put how big our image needs to be to draw the marker while keeping the aspect ratio of the thumbnail
        Rect boundingBox = Imgproc.boundingRect(scene.getContours().get(markerIndex));
        double ratio = (double)width / (double)height;
        int tmpWidth=0, tmpHeight=0;
        if (height==-1 && width==-1)
        {
            width  = tmpWidth  = boundingBox.width;
            height = tmpHeight = boundingBox.height;
        }
        else if (height==-1)
        {
            tmpWidth = boundingBox.width;
            tmpHeight = boundingBox.height;
            height = (int) (width * ((double)boundingBox.height/(double)boundingBox.width));
        }
        else if (width==-1)
        {
            tmpWidth = boundingBox.width;
            tmpHeight = boundingBox.height;
            width = (int) (height * ((double)boundingBox.width/(double)boundingBox.height));
        }
        else if (boundingBox.width/ratio >= boundingBox.height)
        {
            tmpWidth = boundingBox.width;
            tmpHeight = (int) ((double)boundingBox.width / ratio);
        }
        else
        {
            tmpWidth = (int) ((double)boundingBox.height * ratio);
            tmpHeight = boundingBox.height;
        }

        // draw the marker
        Mat image = new Mat(tmpHeight, tmpWidth, CvType.CV_8UC4);
        int verticalPadding = (tmpHeight-boundingBox.height)/2;
        int horizontalPadding = (tmpWidth-boundingBox.width)/2;
        Imgproc.drawContours(image, scene.getContours(), markerIndex, color, -1/*thinkness*/, 8/*line type*/, scene.getHierarchy(), 3/*level*/, new Point(horizontalPadding - boundingBox.tl().x, verticalPadding - boundingBox.tl().y)/*offset*/);

        // resize/rotate the image to the thumbnail size
        cvThumbnail = new Mat(height, width, CvType.CV_8UC4);
        Imgproc.resize(image, cvThumbnail, cvThumbnail.size());

        if (scene.isRotated())
        {
            Core.transpose(cvThumbnail, cvThumbnail);
            Core.flip(cvThumbnail, cvThumbnail, 1);
        }
    }

    public Bitmap getBitmap()
    {
        if (this.bitmap==null) {
            this.bitmap = Bitmap.createBitmap(this.cvThumbnail.cols(), this.cvThumbnail.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(this.cvThumbnail, this.bitmap);
        }
        return this.bitmap;
    }
}
