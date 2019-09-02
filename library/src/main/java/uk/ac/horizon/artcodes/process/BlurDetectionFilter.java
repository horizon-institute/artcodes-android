/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.process;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import uk.ac.horizon.artcodes.camera.CameraFocusControl;
import uk.ac.horizon.artcodes.detect.DetectorSetting;
import uk.ac.horizon.artcodes.detect.ImageBuffers;

/**
 * This ImageProcessor evaluates how blurry the image is, and if required tells the CameraFocusControl to re-focus the camera.
 * Based on Pech-Pacheco et al. “Diatom autofocusing in brightfield microscopy: a comparative study” ( https://doi.org/10.1109/ICPR.2000.903548 ) and blog post https://www.pyimagesearch.com/2015/09/07/blur-detection-with-opencv/
 * <p>
 * For efficiency this only opperates on a square (half the size of the image) in the centre of the image.
 */
public class BlurDetectionFilter implements ImageProcessor
{
	private CameraFocusControl cameraFocusControl;

	public BlurDetectionFilter(CameraFocusControl cameraFocusControl)
	{
		this.cameraFocusControl = cameraFocusControl;
	}

	@Override
	public void process(ImageBuffers buffers)
	{
		Mat greyImage = buffers.getImageInGrey();

		Mat dst = new Mat();

		//long start = System.currentTimeMillis();

		int roiSize = Math.min(greyImage.rows(), greyImage.cols()) / 2;
		Imgproc.Laplacian(greyImage.submat(new Rect((greyImage.cols() - roiSize) / 2, (greyImage.rows() - roiSize) / 2, roiSize, roiSize)), dst, CvType.CV_16S);
		MatOfDouble mean = new MatOfDouble();
		MatOfDouble stdDev = new MatOfDouble();
		Core.meanStdDev(dst, mean, stdDev);

		//long end = System.currentTimeMillis();

		//Log.i("STDDEV", "StdDev: "+Math.pow(stdDev.get(0,0)[0],2)+ " (took: " + (end-start) + "ms)");

		double blurScore = Math.pow(stdDev.get(0, 0)[0], 2);

        /*
        Mat overlay = buffers.getOverlay();
        String text = "b.score: " + (int)blurScore + " ("+(end-start)+"ms)";
        int y = overlay.rows()-50;
        int x = 50;
        Imgproc.putText(overlay, text, new Point(x,y), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,0,0,0), 5);
        Imgproc.putText(overlay, text, new Point(x,y), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255,255,255,255), 3);
*/

		// if image is blurry
		if (blurScore <= 100)
		{
			// tell camera to focus
			Log.i("FOCUS", "Blur detector requesting auto focus with b.score of " + (int) blurScore);
			this.cameraFocusControl.focus(new Runnable()
			{
				@Override
				public void run()
				{

				}
			});
		}

	}

	@Override
	public void getSettings(List<DetectorSetting> settings)
	{
	}
}
