package uk.ac.horizon.busabascan;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TWFacebookUser {
	private static final String PREF_KEY = "membership";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String MEMBER_PIC_FILE = "memberpic";
	
	public String id;
	public String name;
	public String bitmapType;
	private Bitmap pic;
	
	public void setPicture(Bitmap photo){
		if (this.pic != photo){
			if (this.pic != null)
				pic.recycle();
			pic = photo.copy(photo.getConfig(), false);
			
		}
			
	}
	
	public Bitmap getPicture(){
		Bitmap bitmap = null;
		if (this.pic != null){
			bitmap = Bitmap.createBitmap(this.pic);
		}
		return bitmap;
	}
	
	public void saveMember(Context context) throws IOException{
		saveMemberInPreferences(context);
		saveMemberPhoto(context);
	}
	
	public void removeMember(Context context){
		removeMemberFromPreferences(context);
		removeMemberPhoto(context);
	}
	
	public void saveMemberInPreferences(Context context){
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(ID, this.id);
		editor.putString(NAME, this.name);
		editor.commit();
	}
	
	private void removeMemberFromPreferences(Context context){
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.remove(ID);
		editor.remove(NAME);
		editor.commit();
	}
	
	public void saveMemberPhoto(Context context) throws IOException{
		Bitmap srcBitmap = this.pic;
		FileOutputStream fos = null;
		BufferedOutputStream buf = null;
		ByteArrayOutputStream baos = null;
		
		try{
			boolean compressed = false;
			baos = new ByteArrayOutputStream();
			if (bitmapType.equals("image/png")) {
	            compressed = srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
	        } else if (bitmapType.equals("image/jpg") || bitmapType.equals("image/jpeg")) {
	            compressed = srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	        }
			if (compressed){
				fos = context.openFileOutput(MEMBER_PIC_FILE, Context.MODE_PRIVATE);
				buf = new BufferedOutputStream(fos);
				buf.write(baos.toByteArray());
			}
		}finally{
			try{
				if (buf != null){
					buf.flush();
					buf.close();
				}
				if (baos != null){
					baos.flush();
					baos.close();
				}
				if (fos != null){
					fos.flush();
					fos.close();
				}				
			}catch(Exception e){
				fos = null;
				buf = null;
				baos = null;
			}
		}
	}
	
	private void removeMemberPhoto(Context context){
		context.deleteFile(MEMBER_PIC_FILE);
	}

	public boolean restoreMember(Context context) throws FileNotFoundException{
		//try to restore member data.
		restoreMemberFromPreferences(context);
		// if data exist.
		if (this.id.compareTo("") != 0){
			//Member data is already retrieved.Now restore photo.
			restoreMemberPhoto(context);
			return true;
		}else
			return false;
	}
	
	public void restoreMemberFromPreferences(Context context){
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		this.id = pref.getString(ID, "");
		this.name = pref.getString(NAME, "");
	}
	
	private void restoreMemberPhoto(Context context) throws FileNotFoundException{
		FileInputStream fis = null;
		try{
			fis = context.openFileInput(MEMBER_PIC_FILE);
			Bitmap bmp = BitmapFactory.decodeStream(fis);
			if (bmp != null){
				this.setPicture(bmp);
				bmp.recycle();
			}
		}finally{
			try{
				if (fis != null){
					fis.close();
				}
			}catch(Exception e){
				fis = null;
			}
		}
	}
}

