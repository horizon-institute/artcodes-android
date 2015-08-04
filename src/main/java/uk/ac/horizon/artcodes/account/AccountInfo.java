package uk.ac.horizon.artcodes.account;

import android.graphics.drawable.Drawable;

public interface AccountInfo
{
	String getId();
	String getName();
	String getUsername();
	Drawable getIcon();

	Account create();
}
