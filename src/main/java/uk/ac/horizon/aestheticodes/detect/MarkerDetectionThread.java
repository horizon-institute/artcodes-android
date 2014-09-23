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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.aestheticodes.model.ExperienceManager;
import uk.ac.horizon.aestheticodes.model.Marker;
import uk.ac.horizon.aestheticodes.model.MarkerDetector;
import uk.ac.horizon.aestheticodes.model.Mode;
import uk.ac.horizon.aestheticodes.settings.ThresholdBehaviour;

import java.util.ArrayList;
import java.util.List;

public class MarkerDetectionThread extends Thread
{
	private static final String TAG = MarkerDetectionThread.class.getName();
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private static final Scalar outlineColour = new Scalar(0, 0, 0, 255);

	private static Mat cropImage(Mat imgMat)
	{
		final int size = Math.min(imgMat.rows(), imgMat.cols());

		final int colStart = (imgMat.cols() - size) / 2;
		final int rowStart = (imgMat.rows() - size) / 2;

		return imgMat.submat(rowStart, rowStart + size, colStart, colStart + size);
	}

	private static void rotate(Mat src, Mat dst, int angle, boolean flip)
	{
		if (src != dst)
		{
			src.copyTo(dst);
		}

		angle = ((angle / 90) % 4) * 90;

		//0 : flip vertical; 1 flip horizontal

		int flip_horizontal_or_vertical = angle > 0 ? 1 : 0;
		if (flip)
		{
			flip_horizontal_or_vertical = -1;
		}
		int number = Math.abs(angle / 90);

		for (int i = 0; i != number; ++i)
		{
			Core.transpose(dst, dst);
			Core.flip(dst, dst, flip_horizontal_or_vertical);
		}
	}

	private final MarkerDetector markerDetector;
	private final CameraManager cameraManager;
	private final ExperienceEventListener listener;
	private final ExperienceManager experienceManager;
	private int framesSinceLastMarker = 0, cumulativeFramesWithoutMarker = 0;
	private boolean running = true;
	private Mode mode = Mode.detect;
	private long timeOfLastAutoFocus;

	public MarkerDetectionThread(CameraManager cameraManager, ExperienceEventListener listener, ExperienceManager experienceManager)
	{
		this.cameraManager = cameraManager;
		this.listener = listener;
		this.experienceManager = experienceManager;
		markerDetector = new MarkerDetector(experienceManager);
		timeOfLastAutoFocus = System.currentTimeMillis();
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}

	private void thresholdImage(Mat image)
	{
		ThresholdBehaviour thresholdBehaviour = experienceManager.getSelected().getThresholdBehaviour();

		if (framesSinceLastMarker > 2)
		{
			++cumulativeFramesWithoutMarker;
		}

		if (thresholdBehaviour == ThresholdBehaviour.temporalTile)
		{
			Imgproc.GaussianBlur(image, image, new Size(5, 5), 0);

			final int numberOfTiles = (cumulativeFramesWithoutMarker % 9) + 1;
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

			Imgproc.threshold(image, image, 127, 255, Imgproc.THRESH_OTSU);
		}
		else if (thresholdBehaviour == ThresholdBehaviour.resize)
		{
			Imgproc.resize(image, image, new Size(540, 540));

			Imgproc.GaussianBlur(image, image, new Size(5, 5), 0);

			int neighbourhood = (((cumulativeFramesWithoutMarker % 50) + 1) * 4) + 1;
			//Log.i(TAG, "Neighbourhood = " + neighbourhood);
			Imgproc.adaptiveThreshold(image, image, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, neighbourhood, 2);
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

					if ((mode == Mode.outline || mode == Mode.threshold) && drawImage != null)
					{
						Imgproc.drawContours(drawImage, contours, i, outlineColour, 7);
						Imgproc.drawContours(drawImage, contours, i, detectedColour, 5);
					}
				}
			}

			if ((mode != Mode.detect || mode == Mode.threshold) && drawImage != null)
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
						Mat croppedImage = cropImage(image);

						// apply threshold.
						thresholdImage(croppedImage);

						Mat drawImage = null;
						if (mode != Mode.detect)
						{
							rotate(croppedImage, croppedImage, 360 + 90 - cameraManager.getRotation(), cameraManager.isFront());

							if (mode == Mode.threshold)
							{
								drawImage = new Mat(croppedImage.rows(), croppedImage.cols(), CvType.CV_8UC3);
								Imgproc.cvtColor(croppedImage, drawImage, Imgproc.COLOR_GRAY2BGR);
							}
							else
							{
								drawImage = new Mat(croppedImage.rows(), croppedImage.cols(), CvType.CV_8UC4);
							}
						}

						// find markers.
						List<Marker> markers = findMarkers(croppedImage, drawImage);
						if (markers.size() == 0)
						{
							++framesSinceLastMarker;
						}
						else
						{
							framesSinceLastMarker = 0;
						}

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
							listener.markersFound(markers);
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

				// Test if camera needs to be focused
				if (CameraManager.deviceNeedsManualAutoFocus && this.framesSinceLastMarker > 2 && System.currentTimeMillis() - this.timeOfLastAutoFocus >= 5000)
				{
					this.timeOfLastAutoFocus = System.currentTimeMillis();
					this.cameraManager.performManualAutoFocus(new Camera.AutoFocusCallback()
					{
						@Override
						public void onAutoFocus(boolean b, Camera camera)
						{
						}
					});
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}

		Log.i(TAG, "Finishing processing thread");
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
