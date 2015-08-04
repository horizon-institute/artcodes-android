package uk.ac.horizon.artcodes.fragment;

import android.support.v4.app.Fragment;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.account.Account;

public class ArtcodeFragmentBase extends Fragment
{
	protected Artcodes getArtcodes()
	{
		return (Artcodes)getActivity().getApplication();
	}

	protected Account getAccount()
	{
		return getArtcodes().getAccount();
	}
}
