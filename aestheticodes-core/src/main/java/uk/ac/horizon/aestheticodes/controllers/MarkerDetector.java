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

package uk.ac.horizon.aestheticodes.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Greyscaler;
import uk.ac.horizon.aestheticodes.model.Scene;

import java.util.ArrayList;
import java.util.List;

public class MarkerDetector
{
	public static int frames = 0;
	public static long fpsStartTime = 0;

	public enum MarkerDrawMode
	{
		off, outline, regions, debug
	}
	public enum CameraDrawMode
	{
		normal, grey, threshold, depth
	}

	public static interface Listener
	{
		void markerChanged(String markerCode, List<Integer> newMarkerContourIndexes, int historySize, Scene scene);

		void resultUpdated(boolean detected, Bitmap image);
	}

	private static final String TAG = MarkerDetector.class.getName();
	private static final Scalar detectedColour = new Scalar(255, 255, 0, 255);
	private static final Scalar regionColour = new Scalar(255, 128, 0, 255);
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
				//Mat image = new Mat(size.height, size.width, CvType.CV_8UC1);
				Mat drawImage = null;
				result = null;

				Mat croppedImage = null;

				while (running)
				{
					try
					{
						byte[] data = camera.getData();
						if (data != null)
						{
							// data is in NV21 (or YCrCb or YUV) format, with all the Y values first followed by interleaved U and V values e.g. data=YYYYUVUV
							// 'image' is only big enough to fit the Y values which is a hack for an easy greyscale image.
							//image.put(0, 0, data);


							SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
							boolean fullscreen = sharedPreferences.getBoolean("fullscreen", false);
							Mat yuvImage;
							if (fullscreen)
							{
								yuvImage = new Mat(size.height+size.height/2, size.width, CvType.CV_8UC1);
								yuvImage.put(0,0, data);
							}
							else
							{
								yuvImage = MatTranform.cropNV12Data(data, size.height, size.width);
							}

							synchronized (greyscalerLock)
							{
								croppedImage = greyscaler.greyscaleImage(yuvImage, croppedImage);
							}

							Scene scene = new Scene();
							scene.setRotated(true);
							if (cameraDrawMode!=CameraDrawMode.normal || markerDrawMode != MarkerDrawMode.off)
							{
								scene.setRotated(false);
								MatTranform.rotate(croppedImage, croppedImage, 360 + 90 - camera.getRotation(), camera.isFront());
								if (drawImage == null || resetDrawImage)
								{
									if(drawImage != null)
									{
										drawImage.release();
									}
									drawImage = new Mat(croppedImage.rows(), croppedImage.cols(), CvType.CV_8UC4);
									resetDrawImage = false;
								}
							}
							if (cameraDrawMode==CameraDrawMode.grey)
							{
								Imgproc.cvtColor(croppedImage, drawImage, Imgproc.COLOR_GRAY2BGRA);
							}

							// apply threshold.
							thresholdImage(croppedImage);

							if (markerDrawMode != MarkerDrawMode.off || cameraDrawMode!=CameraDrawMode.normal)
							{
								if(cameraDrawMode==CameraDrawMode.threshold)
								{
									Imgproc.cvtColor(croppedImage, drawImage, Imgproc.COLOR_GRAY2BGRA);
									printFps();
								}
								else if (cameraDrawMode==CameraDrawMode.normal)
								{
									drawImage.setTo(new Scalar(0, 0, 0));
								}
							}
							else if(drawImage != null)
							{
								drawImage.release();
								drawImage = null;
								result = null;
								resetDrawImage = false;
							}

							// find markers.
							List<MarkerCode> markers = findMarkers(croppedImage, drawImage, scene);
							if (markers.size() == 0)
							{
								++framesSinceLastMarker;
							}
							else
							{
								framesSinceLastMarker = 0;
							}

							if (drawImage == null)
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

							listener.resultUpdated(!markers.isEmpty(), result);
							markerSelection.addMarkers(markers, listener, experience.get(), scene);

							yuvImage.release();
							scene.release();
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
					frames++;
				}


				if (croppedImage!=null)
				{
					croppedImage.release();
				}

				//image.release();
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

		private List<MarkerCode> findMarkers(Mat inputImage, Mat drawImage, Scene scene)
		{
			final ArrayList<MatOfPoint> contours = new ArrayList<>();
			final Mat hierarchy = new Mat();

			// holds all the markers identified in the camera.
			List<MarkerCode> foundMarkers = new ArrayList<>();
			// Find blobs using connect component.
			Imgproc.findContours(inputImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
			if (scene!=null)
			{
				scene.setContours(contours).setHierarchy(hierarchy);
			}
			if (contours.size()==0)
			{
				return foundMarkers;
			}

			if (cameraDrawMode==CameraDrawMode.depth)
			{
				drawDepth(drawImage, contours, hierarchy);
			}

			MarkerCodeFactory.DetectionStatus[] errors = new MarkerCodeFactory.DetectionStatus[contours.size()];

			if (markerCodeFactory != null)
			{
				for (int i = 0; i < contours.size(); i++)
				{
					//final MarkerCode code = MarkerCode.findMarker(hierarchy, i, experience.get());
					errors[i] = MarkerCodeFactory.DetectionStatus.unknown;
					final MarkerCode code = markerCodeFactory.createMarkerForNode(i, contours, hierarchy, experience.get(), errors, i);
					if (code != null)
					{
						// if marker found then add in the list.
						foundMarkers.add(code);

						if (markerDrawMode != MarkerDrawMode.off && drawImage != null)
						{
							code.draw(drawImage, contours, hierarchy, detectedColour, outlineColour, (markerDrawMode == MarkerDrawMode.regions ? regionColour : null));

						}
					}
				}
			}

			if (markerDrawMode == MarkerDrawMode.debug)
			{
				drawDebug(drawImage, contours, hierarchy, errors);
			}

			if (markerDrawMode != MarkerDrawMode.off && drawImage != null)
			{
				for (MarkerCode marker : foundMarkers)
				{
					Rect bounds = Imgproc.boundingRect(contours.get(marker.getComponentIndexs().get(0)));
					String markerCode = marker.getCodeKey();

					Core.putText(drawImage, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, outlineColour, 5);
					Core.putText(drawImage, markerCode, bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, detectedColour, 3);
				}
			}

			return foundMarkers;

		}

		private void labelDepthOfContourHierarchy(Mat hierarchy, int[] depthArray, int rootIndex, int rootValue)
		{
			int CV_NEXT=0, CV_CHILD=2, CV_PARENT=3;

			for (int i=rootIndex; i>-1 && i<depthArray.length; i=(int)hierarchy.get(0, i)[CV_NEXT])
			{
				// label given node
				depthArray[i] = rootValue;
				// label children
				labelDepthOfContourHierarchy(hierarchy, depthArray, (int)hierarchy.get(0, i)[CV_CHILD], rootValue+1);
			}
		}

		private void  drawDepth(Mat drawImage, List<MatOfPoint> contours, Mat hierarchy)
		{
			drawImage.setTo(new Scalar(0,0,0,255));
			if (contours.size()==0)
			{
				return;
			}

			// get the depth of the contours, we can't use the hierarchy because we need to know
			// the maximum depth
			int numOfContours = (int) contours.size();
			int[] depth = new int[numOfContours];
			labelDepthOfContourHierarchy(hierarchy, depth, 0, 1);

			// contours are grouped by depth so them can be drawn over each other in order of depth
			// without holes
			List<List<Integer>> buckets = new ArrayList<>();
			for (int i=0; i<numOfContours; ++i)
			{
				while (depth[i]>buckets.size())
				{
					buckets.add(new ArrayList<Integer>());
				}
				buckets.get(depth[i]-1).add(i);
			}

			Point offsetPoint = new Point(0, 0);
			for (int bucketIndex=0; bucketIndex<buckets.size(); ++bucketIndex)
			{
				List<Integer> bucket = buckets.get(bucketIndex);
				int step = (int) ((255.0 / (buckets.size())) * (bucketIndex+1));
				Scalar depthColour = new Scalar(step, step, step, 255);
				for (int i : bucket)
				{
					// drawing many contours with drawContours (set contour index to -1) does not
					// seem to work, so we draw them individually. Drawing with/without holes does
					// not seem to affect draw time.
					Imgproc.drawContours(drawImage, contours, i, depthColour, -1, 8, hierarchy, 0, offsetPoint);
				}
			}
		}

		private void drawDebug(Mat drawImage, ArrayList<MatOfPoint> contours, Mat hierarchy, MarkerCodeFactory.DetectionStatus[] errors)
		{
			if (contours.isEmpty())
			{
				return;
			}

			// setup buckets to place contour indexes in depending on their status
			// ATM only the highest level error is drawn, but this might change so keep track of all of them.
			int numOfBuckets = 9;
			List<List<Integer>> buckets = new ArrayList<>();
			for (int i=0; i<numOfBuckets; ++i)
			{
				buckets.add(new ArrayList<Integer>());
			}

			// label contours by depth in the hierarchy, white contours are even black contours are odd
			// this is so we can only process black contours as the display can look confusing if you have the same error on nested contours (and officially an Artcodes hierarchy should be black-white-black).
			int[] depth = new int[contours.size()];
			labelDepthOfContourHierarchy(hierarchy, depth, 0, 0);

			// Place (black/odd) contours in buckets
			// if you use black & white contours the display can look very confusing.
			MarkerCodeFactory.DetectionStatus[] errorTypes = new MarkerCodeFactory.DetectionStatus[] {MarkerCodeFactory.DetectionStatus.noSubContours, MarkerCodeFactory.DetectionStatus.tooManyEmptyRegions, MarkerCodeFactory.DetectionStatus.nestedRegions, MarkerCodeFactory.DetectionStatus.numberOfRegions, MarkerCodeFactory.DetectionStatus.numberOfDots, MarkerCodeFactory.DetectionStatus.checksum, MarkerCodeFactory.DetectionStatus.validationRegions, MarkerCodeFactory.DetectionStatus.extensionSpecificError, MarkerCodeFactory.DetectionStatus.OK};
			for (int i=0; i<errors.length; ++i)
			{
				if (depth[i]%2==0)
				{
					continue;
				}

				for (int j=0; j<numOfBuckets; ++j)
				{
					if (errors[i] == errorTypes[j])
					{
						buckets.get(j).add(i);
						break;
					}
				}
			}

			// draw contours with highest level error status
			for (int bucket=numOfBuckets-1; bucket>=1; bucket--)
			{
				MarkerCodeFactory.DetectionStatus errorType = errorTypes[bucket];

				if (!buckets.get(bucket).isEmpty())
				{
					for (Integer contour : buckets.get(bucket))
					{
						markerCodeFactory.drawErrorDebug(contour, errorType, drawImage, contours, hierarchy, experience.get());
					}

					// the rest of the code below just prints a message to the screen to let the
					// user know what is wrong with the (non-)detected marker. ATM this draws the
					// message to the OpenCV image. In the future this should change the UI label
					// at the bottom of the screen.

					// print text message:
					String[] debugMessages = markerCodeFactory.getDebugMessagesForErrorType(errorType, experience.get());
					String str = debugMessages[0];

					double fontScaleStep = 0.1;
					int[] baseLine = new int[1];
					if (str!=null && !str.equals(""))
					{
						double fontScale = 1 + fontScaleStep;
						int fontThickness = 2, textWidth = drawImage.cols() + 1;
						while (textWidth > drawImage.cols())
						{ // decrease font scale until the text fits in the image
							fontScale -= fontScaleStep;
							Size size = Core.getTextSize(str, Core.FONT_HERSHEY_SIMPLEX, fontScale, fontThickness, baseLine);
							textWidth = (int) size.width;
						}
						int xPositionOfCentredText = (drawImage.cols() - textWidth) / 2;
						int yPositionOfCentredText = drawImage.rows() - 40;

						Core.putText(drawImage, str, new Point(xPositionOfCentredText, yPositionOfCentredText), Core.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(0, 0, 0, 255), fontThickness + 5);
						Core.putText(drawImage, str, new Point(xPositionOfCentredText, yPositionOfCentredText), Core.FONT_HERSHEY_SIMPLEX, fontScale, errorType.getColor(), fontThickness);
					}

					// print secondary message
					str = debugMessages[1];

					if (str!=null && !str.equals(""))
					{
						double fontScale = 0.7 + fontScaleStep;
						int fontThickness = 2, textWidth = drawImage.cols()+1;
						while (textWidth > drawImage.cols()) { // decrease font scale until the text fits in the image
							fontScale -= fontScaleStep;
							Size size = Core.getTextSize(str, Core.FONT_HERSHEY_SIMPLEX, fontScale, fontThickness, baseLine);
							textWidth = (int) size.width;
						}
						int xPositionOfCentredText = (drawImage.cols()-textWidth)/2;
						int yPositionOfCentredText = drawImage.rows() - 10;

						Core.putText(drawImage, str, new Point(xPositionOfCentredText, yPositionOfCentredText), Core.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(0,0,0,255), fontThickness+5);
						Core.putText(drawImage, str, new Point(xPositionOfCentredText, yPositionOfCentredText), Core.FONT_HERSHEY_SIMPLEX, fontScale, errorType.getColor(), fontThickness);
					}

					break;
				}
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

	private final MarkerSelection markerSelection = new CombinedMarkerSelection();
	private final CameraController camera;
	private final Listener listener;
	private final ExperienceController experience;
	private Context context;
	private DetectionThread thread = null;
	private boolean running = true;
	private boolean resetDrawImage = false;
	private Bitmap result;

	private MarkerDrawMode markerDrawMode = MarkerDrawMode.off;
	private CameraDrawMode cameraDrawMode = CameraDrawMode.normal;

	private Greyscaler greyscaler = new Greyscaler.IntensityGreyscaler(0, false);
	private Object greyscalerLock = new Object();

	private MarkerCodeFactory markerCodeFactory = null;


	public MarkerDetector(CameraController camera, Listener listener, ExperienceController experience, Context context)
	{
		this.camera = camera;
		this.listener = listener;
		this.experience = experience;
		this.context = context;
	}

	public MarkerDrawMode getMarkerDrawMode()
	{
		return markerDrawMode;
	}

	public void setMarkerDrawMode(MarkerDrawMode mode)
	{
		this.markerDrawMode = mode;
	}

	public void setGreyscaler(Greyscaler newGreyscaler)
	{
		synchronized (greyscalerLock)
		{
			// it should not be possible to set a null object here as it will cause problems if (for some reason) no experience is selected
			if (newGreyscaler==null)
			{
				newGreyscaler = new Greyscaler.IntensityGreyscaler(0, false);
			}

			Greyscaler oldGreyscaler = greyscaler;
			greyscaler = newGreyscaler;

			if (oldGreyscaler!=null)
			{
				oldGreyscaler.release();
			}
		}
		resetFps();
	}

	public static void resetFps()
	{
		frames = 0;
		fpsStartTime = System.currentTimeMillis();
	}
	public static void printFps()
	{
		Log.i("FPS", "Avg. FPS = " + 1000*((double)frames / ((double)System.currentTimeMillis()-(double)fpsStartTime)));
	}

	public void setMarkerCodeFactory(MarkerCodeFactory markerCodeFactory)
	{
		this.markerCodeFactory = markerCodeFactory;
	}

	public Bitmap getResult()
	{
		return result;
	}

	public void toggleCameraDrawMode()
	{
		switch (this.cameraDrawMode)
		{
			case normal:
				this.setCameraDrawMode(CameraDrawMode.grey);
				break;
			case grey:
				this.setCameraDrawMode(CameraDrawMode.threshold);
				break;
			case threshold:
				//this.setCameraDrawMode(CameraDrawMode.depth);
				//break;
			//case depth:
				this.setCameraDrawMode(CameraDrawMode.normal);
				break;
		}
	}

	public void setCameraDrawMode(CameraDrawMode cameraDrawMode)
	{
		resetDrawImage = this.cameraDrawMode==CameraDrawMode.normal || cameraDrawMode==CameraDrawMode.normal;
		this.cameraDrawMode = cameraDrawMode;
	}

	public CameraDrawMode shouldDrawThreshold()
	{
		return this.cameraDrawMode;
	}

	public void start()
	{
		if (thread == null)
		{
			markerSelection.reset(listener);
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
