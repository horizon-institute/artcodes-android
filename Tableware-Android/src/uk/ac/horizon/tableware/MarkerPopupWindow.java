package uk.ac.horizon.tableware;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MarkerPopupWindow{
	private View anchor;
	private PopupWindow window;
	private DtouchMarker dtouchMarker;
	private OnMarkerPopupWindowListener listener;
	
	public interface OnMarkerPopupWindowListener{
		public void onDismissedSelected(DtouchMarker marker);
		public void onBrowseMarkerSelected(DtouchMarker marker);
	}
	
	MarkerPopupWindow(View anchor, DtouchMarker marker){
		this.dtouchMarker = marker;
		this.anchor = anchor;
		initPopupWindow(anchor.getContext());
		initContentView();		
	}
	
	private void initContentView(){
		LayoutInflater inflater = (LayoutInflater)this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.markerpopup, null);
    	int rotation = WindowRotation.getCameraRotation(this.anchor.getContext());
    	if (rotation == 0)
    		WindowRotation.rotateViewGroup(this.anchor.getContext(),layout);
    	this.window.setContentView(layout);
    	TextView markerDesc = (TextView) layout.findViewById(R.id.markerDesc);
    	markerDesc.setText(dtouchMarker.getDescription());
    }
	
	private void initPopupWindow(Context context){
		this.window = new PopupWindow(context);
		this.window.setTouchInterceptor(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event){
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
					MarkerPopupWindow.this.dismissPopupWindow();
					return true;
				}else if (event.getAction() == MotionEvent.ACTION_DOWN){
					MarkerPopupWindow.this.browseMarker();
					return true;
				}
				return false;
			}
		});
	}
	
	public void show(Point location, Point size){
		onPreShow(size);
		this.window.setAnimationStyle(R.style.Animations_GrowFromCentre);
		this.window.showAtLocation(anchor, Gravity.NO_GRAVITY, location.x, location.y);
	}
	
	private void onPreShow(Point size){
		this.window.setWidth(size.x);
		this.window.setHeight(size.y);
		//this.window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		//this.window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		this.window.setTouchable(true);
		//this.window.setFocusable(true);
		this.window.setOutsideTouchable(true);
	}
	
	public void setOnMarkerPopupWindowListener(OnMarkerPopupWindowListener listener){
		this.listener = listener;
	}
	
	private void dismissPopupWindow(){
		this.window.dismiss();
		if (listener != null){
			listener.onDismissedSelected(dtouchMarker);
		}
	}
	
	private void browseMarker(){
		this.window.dismiss();
		if (listener != null){
			listener.onBrowseMarkerSelected(dtouchMarker);
		}
	}
}
