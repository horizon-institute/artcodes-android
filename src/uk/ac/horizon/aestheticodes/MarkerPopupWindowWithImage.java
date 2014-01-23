package uk.ac.horizon.aestheticodes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.ac.horizon.data.DataMarker;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

class MarkerPopupWindowWithImage
{
	private View anchor;
	private PopupWindow window;
	private DataMarker dataMarker;
	private OnMarkerPopupWindowWithImageListener listener;
	private Context context;

	public interface OnMarkerPopupWindowWithImageListener
	{
		public void onImageDismissedSelected(DataMarker marker);

		public void onImageBrowseMarkerSelected(DataMarker marker);
	}

	MarkerPopupWindowWithImage(View anchor, DataMarker marker, Context context)
	{
		this.dataMarker = marker;
		this.anchor = anchor;
		this.context = context;
		initPopupWindow(anchor.getContext());
		initContentView();
	}

	private void initContentView()
	{
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.markerpopupwithimage, null);
		int rotation = WindowRotation.getCameraRotation(this.anchor.getContext());
		if (rotation == 0)
			WindowRotation.rotateViewGroup(this.anchor.getContext(), layout);
		this.window.setContentView(layout);
		setPicture(layout);
	}

	private void initPopupWindow(Context context)
	{
		this.window = new PopupWindow(context);
		this.window.setTouchInterceptor(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
				{
					MarkerPopupWindowWithImage.this.dismissPopupWindow();
					return true;
				}
				else if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					MarkerPopupWindowWithImage.this.browseMarker();
					return true;
				}
				return false;
			}
		});
	}

	public void show(Point location, Point size)
	{
		onPreShow(size);
		this.window.setAnimationStyle(R.style.Animations_GrowFromCentre);
		this.window.showAtLocation(anchor, Gravity.NO_GRAVITY, location.x, location.y);
	}

	private void onPreShow(Point size)
	{
		this.window.setWidth(size.x);
		this.window.setHeight(size.y);
		this.window.setTouchable(true);
		this.window.setOutsideTouchable(true);
	}

	public void setOnMarkerPopupWindowListener(OnMarkerPopupWindowWithImageListener listener)
	{
		this.listener = listener;
	}

	private void dismissPopupWindow()
	{
		this.window.dismiss();
		if (listener != null)
		{
			listener.onImageDismissedSelected(dataMarker);
		}
	}

	private void browseMarker()
	{
		this.window.dismiss();
		if (listener != null)
		{
			listener.onImageBrowseMarkerSelected(dataMarker);
		}
	}

	private void setPicture(ViewGroup layout)
	{
		if (dataMarker.getUri() != null)
		{
			ImageView imageView = (ImageView) layout.findViewById(R.id.markerImageViewResult);
			Bitmap bmp = readImageFromAsset(dataMarker.getUri());
			if (bmp != null)
			{
				imageView.setImageBitmap(bmp);
			}
		}
	}

	private Bitmap readImageFromAsset(String uri)
	{
		Bitmap bitmap = null;
		AssetManager am = context.getResources().getAssets();
		try
		{
			InputStream is = am.open(uri);
			BufferedInputStream bis = new BufferedInputStream(is);
			bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		}
		catch (IOException e)
		{

		}
		return bitmap;
	}
}
