package uk.ac.horizon.aestheticodes;

import android.content.Context;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import uk.ac.horizon.aestheticodes.R;

public class WindowRotation {
	
	private static int getDisplayRotation(Context context){
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = windowManager.getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation){
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270:degrees = 270; break;
		}
		return degrees;
	}
	
	/***
	 * This is temporary function and needs to be changed. It currently assumes that the natural orientation is
	 * portrait. Which is not true in all mobile devices.
	 * @return
	 */
	public static int getCameraRotation(Context context){
		int rotation = 0;
		int degrees = getDisplayRotation(context);
		//as camera is always in landscape mode in this scenario so add some degrees to
		switch(degrees){
			case 0: rotation = -90; break;
			case 90: rotation = 0; break;
			case 180: rotation = -90; break;
			case 270: rotation = 0;break;
			default: rotation = 0; break;
		}
		return rotation;
	}
	
	public static void rotateViewGroup(Context context, ViewGroup layout){
		Animation rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotation);
		LayoutAnimationController animController = new LayoutAnimationController(rotateAnim, 0);
		layout.setLayoutAnimation(animController);
	}
}
