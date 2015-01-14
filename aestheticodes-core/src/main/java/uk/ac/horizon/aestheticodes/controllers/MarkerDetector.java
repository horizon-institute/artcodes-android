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

package uk.ac.horizon.aestheticodes.controllers;

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
import uk.ac.horizon.aestheticodes.model.Experience;

import java.util.ArrayList;
import java.util.List;

public class MarkerDetector
{
	public static enum Mode
	{
		detect, outline, threshold
	}

	public static interface Listener
	{
		void markersDetected(boolean detected);
		void markerFound(String markerCode);
	}

	private static final String TAG = MarkerDetector.class.getName();
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private static final Scalar outlineColour = new Scalar(0, 0, 0, 255);

	private class DetectionThread extends Thread
	{
		private int framesSinceLastMarker = 0;
		private int cumulativeFramesWithoutMarker = 0;
		private long timeOfLastAutoFocus = 0;

		@Override
		public void run()
		{
			try
			{
				timeOfLastAutoFocus = System.currentTimeMillis();
				Camera.Size size = camera.getSize();
				Mat image = new Mat(size.height, size.width, CvType.CV_8UC1);
				Mat drawImage = null;
				while (running)
				{
					try
					{
						byte[] data = camera.getData();
						if (data != null)
						{
							image.put(0, 0, data);

							// Cut down region for detection
							Mat croppedImage = MatTranform.crop(image);

							// apply threshold.
							thresholdImage(croppedImage);

							if (mode != Mode.detect)
							{
								MatTranform.rotate(croppedImage, croppedImage, 360 + 90 - camera.getRotation(), camera.isFront());

								if(resetDrawImage && drawImage != null)
								{
									drawImage.release();
									drawImage = null;
								}

								if (drawImage == null)
								{
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
								else
								{
									drawImage.setTo(new Scalar(0, 0, 0));
								}
							}

							// find markers.
							List<MarkerCode> markers = findMarkers(croppedImage, drawImage);
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
								result = null;
							}
							else
							{
								if (result == null)
								{
									result = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
								}
								Utils.matToBitmap(drawImage, result);
							}

							if(listener != null)
							{
								markerSelection.addMarkers(markers);
								String code = markerSelection.getFoundMarker();
								if (code != null && mode == Mode.detect)
								{
									listener.markerFound(code);
								}
								else
								{
									listener.markersDetected(!markers.isEmpty());
								}
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
					if (CameraController.deviceNeedsManualAutoFocus && framesSinceLastMarker > 2 && System.currentTimeMillis() - timeOfLastAutoFocus >= 5000)
					{
						timeOfLastAutoFocus = System.currentTimeMillis();
						camera.performManualAutoFocus(new Camera.AutoFocusCallback()
						{
							@Override
							public void onAutoFocus(boolean b, Camera camera)
							{
							}
						});
					}
				}

				image.release();
				if (drawImage != null)
				{
					drawImage.release();
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}

			Log.i(TAG, "Finishing processing thread");
		}

		private List<MarkerCode> findMarkers(Mat inputImage, Mat drawImage)
		{
			final ArrayList<MatOfPoint> contours = new ArrayList<>();
			final Mat hierarchy = new Mat();
			try
			{
				// holds all the markers identified in the camera.
				List<MarkerCode> foundMarkers = new ArrayList<>();
				// Find blobs using connect component.
				Imgproc.findContours(inputImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

				for (int i = 0; i < contours.size(); i++)
				{
					final MarkerCode code = MarkerCode.findMarker(hierarchy, i, experience.get());
					if (code != null)
					{
						// if marker found then add in the list.
						foundMarkers.add(code);

						if ((mode == Mode.outline || mode == Mode.threshold) && drawImage != null)
						{
							Imgproc.drawContours(drawImage, contours, i, outlineColour, 7);
							Imgproc.drawContours(drawImage, contours, i, detectedColour, 5);
						}
					}
				}

				if ((mode != Mode.detect || mode == Mode.threshold) && drawImage != null)
				{
					for (MarkerCode marker : foundMarkers)
					{
						Rect bounds = Imgproc.boundingRect(contours.get(marker.getComponentIndex()));
						String markerCode = marker.getCodeKey();

						Core.putText(drawImage, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
						Core.putText(drawImage, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
					}
				}

				return foundMarkers;
			}
			finally
			{
				contours.clear();
				hierarchy.release();
			}
		}

		private void thresholdImage(Mat image)
		{
			Experience.Threshold threshold = experience.get().getThreshold();

			if (framesSinceLastMarker > 2)
			{
				++cumulativeFramesWithoutMarker;
			}

			if (threshold == Experience.Threshold.temporalTile)
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
			else if (threshold == Experience.Threshold.resize)
			{
				Imgproc.resize(image, image, new Size(540, 540));

				Imgproc.GaussianBlur(image, image, new Size(5, 5), 0);

				int neighbourhood = (((cumulativeFramesWithoutMarker % 50) + 1) * 4) + 1;
				//Log.i(TAG, "Neighbourhood = " + neighbourhood);
				Imgproc.adaptiveThreshold(image, image, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, neighbourhood, 2);
			}
		}
	}

	private final MarkerSelection markerSelection = new MarkerSelection();
	private final CameraController camera;
	private final Listener listener;
	private final ExperienceController experience;
	private DetectionThread thread = null;
	private boolean running = true;
	private boolean resetDrawImage = false;
	private Mode mode = Mode.detect;
	private Bitmap result;

	public MarkerDetector(CameraController camera, Listener listener, ExperienceController experience)
	{
		this.camera = camera;
		this.listener = listener;
		this.experience = experience;
	}

	public Mode getMode()
	{
		return mode;
	}

	public void setMode(Mode mode)
	{
		Log.i(TAG, "Set mode " + mode);
		this.mode = mode;
		resetDrawImage = true;
		markerSelection.reset();
	}

	public Bitmap getResult()
	{
		return result;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new DetectionThread();
			running = true;
			thread.start();
		}
	}

	public void stop()
	{
		running = false;
		thread = null;
	}
}
