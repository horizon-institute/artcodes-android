/*
 * Artcodes recognises a different marker scheme that allows the
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

package uk.ac.horizon.artcodes.scanner.overlay;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.artcodes.scanner.BR;
import uk.ac.horizon.artcodes.scanner.detect.Marker;

public class Overlay extends BaseObservable
{
	private static int THRESHOLD_LAYER = 0;
	private static int MARKER_LAYER = 1;
	private static int CODE_LAYER = 2;

	private Mat drawImage;
	private Layer[] layers = {new ThresholdNullLayer(), new MarkerNullLayer(), new CodeNullLayer()};
	private Bitmap bitmap;
	private int feedback;
	private int viewfinderSize;

	public void drawMarkers(List<Marker> foundMarkers, ArrayList<MatOfPoint> contours)
	{
		if (drawImage != null)
		{
			for (Layer layer : layers)
			{
				layer.drawMarkers(drawImage, foundMarkers, contours);
			}

			if (bitmap == null)
			{
				bitmap = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
			}
			Utils.matToBitmap(drawImage, bitmap);
			notifyPropertyChanged(BR.bitmap);
		} else if (bitmap != null)
		{
			bitmap = null;
			notifyPropertyChanged(BR.bitmap);
		}
	}

	public void drawThreshold(Mat image)
	{
		if (drawImage != null)
		{
			for (Layer layer : layers)
			{
				layer.drawThreshold(image, drawImage);
			}
		}
	}

	@Bindable
	public Bitmap getBitmap()
	{
		return bitmap;
	}

	@Bindable
	public int getCodeIcon()
	{
		return layers[CODE_LAYER].getIcon();
	}

	@Bindable
	public int getFeedback()
	{
		return feedback;
	}

	@Bindable
	public int getMarkerIcon()
	{
		return layers[MARKER_LAYER].getIcon();
	}

	@Bindable
	public int getThresholdIcon()
	{
		return layers[THRESHOLD_LAYER].getIcon();
	}

	@Bindable
	public int getViewfinderSize()
	{
		return viewfinderSize;
	}

	public void setViewfinderSize(int viewfinderSize)
	{
		bitmap = null;
		this.viewfinderSize = viewfinderSize;
		notifyPropertyChanged(BR.viewfinderSize);
	}

	public boolean hasOutput(Mat frame)
	{
		for (Layer drawer : layers)
		{
			if (drawer.hasOutput())
			{
				if (drawImage == null)
				{
					drawImage = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC4);
				}
				return true;
			}
		}
		return false;
	}

	public void nextCodeDrawMode()
	{
		layers[CODE_LAYER] = layers[CODE_LAYER].getNext();
		notifyPropertyChanged(BR.codeIcon);
		feedback = layers[CODE_LAYER].getFeedback();
		notifyPropertyChanged(BR.feedback);
	}

	public void nextMarkerDrawMode()
	{
		layers[MARKER_LAYER] = layers[MARKER_LAYER].getNext();
		notifyPropertyChanged(BR.markerIcon);
		feedback = layers[MARKER_LAYER].getFeedback();
		notifyPropertyChanged(BR.feedback);
	}

	public void nextThresholdDrawMode()
	{
		layers[THRESHOLD_LAYER] = layers[THRESHOLD_LAYER].getNext();
		notifyPropertyChanged(BR.thresholdIcon);
		feedback = layers[THRESHOLD_LAYER].getFeedback();
		notifyPropertyChanged(BR.feedback);
	}

	public void setThresholdLayer(Layer layer)
	{
		layers[THRESHOLD_LAYER] = layer;
	}
}
