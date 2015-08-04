package uk.ac.horizon.artcodes.activity;

import android.support.v7.app.AppCompatActivity;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.account.Account;

public abstract class ArtcodeActivityBase extends AppCompatActivity
{
	protected Artcodes getArtcodes()
	{
		return (Artcodes)getApplication();
	}

	protected Account getAccount()
	{
		return getArtcodes().getAccount();
	}
}
