package com.example.coverflow;

import java.util.List;

import uk.ac.horizon.tableware.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class CoverFlowImageAdapter extends BaseAdapter {
	private Context mContext;
	private List<Bitmap> mThumbnails;
	
	public CoverFlowImageAdapter(List<Bitmap> thumbnails, Context context){
		mThumbnails = thumbnails;
		mContext = context;
	}
	
	@Override
	public int getCount() {
		return mThumbnails.size();
	}

	@Override
	public Object getItem(int position) {
		return mThumbnails.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ImageView imageView = new ImageView(mContext);
		imageView.setImageBitmap(mThumbnails.get(position));
		imageView.setLayoutParams(new Gallery.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.coverFlowitemwidth)
				, mContext.getResources().getDimensionPixelSize(R.dimen.coverFlowitemheight)));
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		
		//Make sure we set anti-aliasing otherwise we get jaggies
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        drawable.setAntiAlias(true);
        return imageView;
	}

}
