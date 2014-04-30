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
import uk.ac.horizon.dtouchMobile.DtouchMarker;
import uk.ac.horizon.dtouchMobile.MarkerDetector;

import java.util.ArrayList;
import java.util.List;

public class MarkerDetectionThread extends Thread
{
	private static final String TAG = MarkerDetectionThread.class.getName();
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private final MarkerDetector markerDetector;
	private final CameraManager cameraManager;
	private final MarkerPreferences markerPreferences;
	MarkerDetectionListener listener;
	private boolean running = true;
	private DrawMode drawMode = DrawMode.none;

	public MarkerDetectionThread(CameraManager cameraManager, MarkerDetectionListener listener)
	{
		this.cameraManager = cameraManager;
		this.listener = listener;
		this.markerPreferences = new MarkerPreferences(cameraManager.getContext());
		markerDetector = new MarkerDetector(cameraManager.getContext(), markerPreferences);
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}

	private Mat getDetectionSegment(Mat imgMat)
	{
		final int size = Math.min(imgMat.rows(), imgMat.cols());

		final int colStart = (imgMat.cols() - size) / 2;
		final int rowStart = (imgMat.rows() - size) / 2;

		return imgMat.submat(rowStart, rowStart + size, colStart, colStart + size);
	}

	private void thresholdImage(Mat image)
	{
		int startRow;
		int endRow;
		int startCol;
		int endCol;

		int numberOfTiles = markerPreferences.getNumberOfTiles();
		int tileWidth = (int) image.size().height / numberOfTiles;
		int tileHeight = (int) image.size().width / numberOfTiles;

		// Split image into tiles and apply threshold on each image tile
		// separately.

		// process image tiles other than the last one.
		for (int tileRowCount = 0; tileRowCount < numberOfTiles; tileRowCount++)
		{
			startRow = tileRowCount * tileWidth;
			if (tileRowCount < numberOfTiles - 1)
			{
				endRow = (tileRowCount + 1) * tileWidth;
			}
			else
			{
				endRow = (int) image.size().height;
			}

			Mat tileThreshold = new Mat();
			for (int tileColCount = 0; tileColCount < numberOfTiles; tileColCount++)
			{
				startCol = tileColCount * tileHeight;
				if (tileColCount < numberOfTiles - 1)
				{
					endCol = (tileColCount + 1) * tileHeight;
				}
				else
				{
					endCol = (int) image.size().width;
				}

				Mat tileMat = image.submat(startRow, endRow, startCol, endCol);
				// localThreshold = Imgproc.threshold(tileMat, tileThreshold, 0,
				// 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
				// RNM: Adaptive threshold rules!
				//localThreshold = 0x80;
				// ADAPTIVE_THRESH_GAUSSIAN_C
				// Imgproc.adaptiveThreshold(tileMat, tileThreshold, 255,
				// Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 91,
				// 2);
				Imgproc.threshold(tileMat, tileThreshold, 127, 255, Imgproc.THRESH_OTSU);
				tileThreshold.copyTo(tileMat);
				tileMat.release();
			}
			tileThreshold.release();
		}
	}

	private List<DtouchMarker> findMarkers(Mat inputImage, Mat drawImage)
	{
		final ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		final Mat hierarchy = new Mat();
		try
		{
			// holds all the markers identified in the camera.
			List<DtouchMarker> markersDetected = new ArrayList<DtouchMarker>();
			// Find blobs using connect component.
			Imgproc.findContours(inputImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);


			List<Integer> code = new ArrayList<Integer>();

			for (int i = 0; i < contours.size(); i++)
			{
				// clean this list.
				code.clear();
				if (markerDetector.verifyRoot(i, hierarchy, code))
				{
					// if marker found then add in the list.
					DtouchMarker marker = new DtouchMarker();
					marker.setCode(code);
					marker.setComponentIndex(i);
					markersDetected.add(marker);

					if (drawMode == DrawMode.outline && drawImage != null)
					{
						Imgproc.drawContours(drawImage, contours, i, detectedColour, 5);
						Rect bounds = Imgproc.boundingRect(contours.get(i));

						Core.putText(drawImage, marker.getCodeKey(), bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
					}
				}
				//else
				//{
				//if (!detecting && drawImage != null)
				//{
				//Imgproc.drawContours(drawImage, contours, i, new Scalar(0, 0, 255), 3);
				//}
				//}
			}

			return markersDetected;
		}
		finally
		{
			contours.clear();
			hierarchy.release();
		}
	}

	void rotate(Mat src, Mat dst, int angle)
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
			Core.flip(dst, dst, flip_horizontal_or_vertical);
		}
	}

	@Override
	public void run()
	{
		try
		{
			Camera.Size size = cameraManager.getSize();
			Mat image = new Mat(size.height, size.width, CvType.CV_8UC1);
			Bitmap bmp = null;
			while (running)
			{
				try
				{
					byte[] data = cameraManager.getData();
					if (data != null)
					{
						image.put(0, 0, data);

						// Cut down region for detection
						Mat detectionImage = getDetectionSegment(image);

						// apply threshold.
						thresholdImage(detectionImage);

						Mat drawImage = null;

						if (drawMode != DrawMode.none)
						{
							rotate(detectionImage, detectionImage, 360 + 90 - cameraManager.getRotation());
							drawImage = new Mat(detectionImage.rows(), detectionImage.cols(), CvType.CV_8UC4);
						}

						// find markers.
						List<DtouchMarker> markers = findMarkers(detectionImage, drawImage);

						// if markers are found then decide which marker code occurred most.
						if (!markers.isEmpty() && listener != null)
						{
							listener.markerDetected(markerDetector.compareDetectedMarkers(markers));
						}

						if (drawMode == DrawMode.none)
						{
							cameraManager.setResult(null);
						}
						else if (drawImage != null)
						{
							if (bmp == null)
							{
								bmp = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
							}
							Utils.matToBitmap(drawImage, bmp);

							cameraManager.setResult(bmp);

							listener.tracking(markers);

							drawImage.release();
						}

						if (drawImage != null)
						{
							drawImage.release();
						}
					}
					else
					{
						Log.i(TAG, "No data");
						synchronized (this)
						{
							wait(200);
						}
					}
				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}

		Log.i(TAG, "Finishing processing thread");
	}

	public DrawMode getDrawMode()
	{
		return drawMode;
	}

	public void setDrawMode(DrawMode drawMode)
	{
		Log.i(TAG, "Set draw mode " + drawMode);
		this.drawMode = drawMode;
	}

	public enum DrawMode
	{
		none, outline
	}
}
