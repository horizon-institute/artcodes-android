package uk.ac.horizon.artcodes.scanner.overlay;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import uk.ac.horizon.artcodes.scanner.BR;
import uk.ac.horizon.artcodes.scanner.detect.Marker;

import java.util.ArrayList;
import java.util.List;

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
		}
		else if (bitmap != null)
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
