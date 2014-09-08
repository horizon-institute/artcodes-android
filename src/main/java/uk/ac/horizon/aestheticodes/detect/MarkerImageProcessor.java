/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
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

package uk.ac.horizon.aestheticodes.detect;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.MarkerDetector;
import uk.ac.horizon.aestheticodes.model.Mode;

import java.util.ArrayList;
import java.util.List;

public class MarkerImageProcessor implements ImageProcessor
{
	private static final String TAG = MarkerImageProcessor.class.getName();
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private static final Scalar outlineColour = new Scalar(0, 0, 0, 255);
	private final MarkerDetector markerDetector;
	private final CameraManager cameraManager;
	private final MarkerDetectionListener listener;
	private Mode mode = Mode.detect;

	public MarkerImageProcessor(CameraManager cameraManager, MarkerDetectionListener listener, Experience settings)
	{
		this.cameraManager = cameraManager;
		this.listener = listener;
		markerDetector = new MarkerDetector(settings);
	}

	private Mat cropImage(Mat imgMat)
	{
		final int size = Math.min(imgMat.rows(), imgMat.cols());

		final int colStart = (imgMat.cols() - size) / 2;
		final int rowStart = (imgMat.rows() - size) / 2;

		return imgMat.submat(rowStart, rowStart + size, colStart, colStart + size);
	}

	private void thresholdImage(Mat image)
	{
		final int numberOfTiles = 3;
		final int tileHeight = (int) image.size().height / numberOfTiles;
		final int tileWidth = (int) image.size().width / numberOfTiles;

		// Split image into tiles and apply threshold on each image tile separately.
		for (int tileRowCount = 0; tileRowCount < numberOfTiles; tileRowCount++)
		{
			final int startRow = tileRowCount * tileHeight;
			int endRow;
			if (tileRowCount < numberOfTiles - 1)
			{
				endRow = (tileRowCount + 1) * tileHeight;
			}
			else
			{
				endRow = (int) image.size().height;
			}

			for (int tileColCount = 0; tileColCount < numberOfTiles; tileColCount++)
			{
				final int startCol = tileColCount * tileWidth;
				int endCol;
				if (tileColCount < numberOfTiles - 1)
				{
					endCol = (tileColCount + 1) * tileWidth;
				}
				else
				{
					endCol = (int) image.size().width;
				}

				final Mat tileMat = image.submat(startRow, endRow, startCol, endCol);
				Imgproc.threshold(tileMat, tileMat, 127, 255, Imgproc.THRESH_OTSU);
				tileMat.release();
			}
		}
	}

	private List<Marker> findMarkers(Mat inputImage, Mat drawImage)
	{
		final ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		final Mat hierarchy = new Mat();
		try
		{
			// holds all the markers identified in the camera.
			List<Marker> markersDetected = new ArrayList<Marker>();
			// Find blobs using connect component.
			Imgproc.findContours(inputImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

			for (int i = 0; i < contours.size(); i++)
			{
				final List<Integer> code = markerDetector.verifyRoot(i, hierarchy);
				if (code != null)
				{
					// if marker found then add in the list.
					Marker marker = new Marker();
					marker.setCode(code);
					marker.setComponentIndex(i);
					markersDetected.add(marker);

					if ((mode == Mode.outline) && drawImage != null)
					{
						Imgproc.drawContours(drawImage, contours, i, outlineColour, 7);
						Imgproc.drawContours(drawImage, contours, i, detectedColour, 5);
					}
				}
			}

			if (mode != Mode.detect && drawImage != null)
			{
				for (Marker marker : markersDetected)
				{
					Rect bounds = Imgproc.boundingRect(contours.get(marker.getComponentIndex()));
					String markerCode = marker.getCodeKey();

					Core.putText(drawImage, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
					Core.putText(drawImage, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
				}
			}

			return markersDetected;
		}
		finally
		{
			contours.clear();
			hierarchy.release();
		}
	}

	void rotate(Mat src, Mat dst, int angle, boolean flip)
	{
		if (src != dst)
		{
			src.copyTo(dst);
		}

		angle = ((angle / 90) % 4) * 90;

		//0 : flip vertical; 1 flip horizontal
		int flip_horizontal_or_vertical = angle > 0 ? 1 : 0;
		int number = Math.abs(angle / 90);

		for (int i = 0; i != number; ++i)
		{
			Core.transpose(dst, dst);
			if (!flip)
			{
				Core.flip(dst, dst, flip_horizontal_or_vertical);
			}
		}
	}

	private Mat image;
	private Bitmap bmp = null;

	@Override
	public void processImage(byte[] data)
	{
		image.put(0, 0, data);

		// Cut down region for detection
		Mat croppedImage = cropImage(image);

		// apply threshold.
		thresholdImage(croppedImage);

		Mat drawImage = null;
		if (mode != Mode.detect)
		{
			rotate(croppedImage, croppedImage, 360 + 90 - cameraManager.getRotation(), cameraManager.isFront());

			if (mode == Mode.threshold)
			{
				drawImage = croppedImage.clone();
			}
			else
			{
				drawImage = new Mat(croppedImage.rows(), croppedImage.cols(), CvType.CV_8UC4);
			}
		}

		// find markers.
		List<Marker> markers = findMarkers(croppedImage, drawImage);

		if (mode == Mode.detect || drawImage == null)
		{
			cameraManager.setResult(null);
		}
		else
		{
			if (bmp == null)
			{
				bmp = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
			}
			Utils.matToBitmap(drawImage, bmp);

			cameraManager.setResult(bmp);

			drawImage.release();
		}

		if (listener != null)
		{
			listener.markersDetected(markers);
		}

		if (drawImage != null)
		{
			drawImage.release();
		}
	}

	@Override
	public void initCamera(Camera.Size size)
	{
		if(image != null)
		{
			image.release();
		}
		image = new Mat(size.height, size.width, CvType.CV_8UC1);
	}

	public Mode getMode()
	{
		return mode;
	}

	public void setMode(Mode mode)
	{
		Log.i(TAG, "Set mode " + mode);
		this.mode = mode;
	}
}
