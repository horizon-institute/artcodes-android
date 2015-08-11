package uk.ac.horizon.artcodes.fragment;

import android.support.v4.app.Fragment;
import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

public class ArtcodeFragmentBase extends Fragment
{
	protected Artcodes getArtcodes()
	{
		return (Artcodes)getActivity().getApplication();
	}

	protected ArtcodeServer getServer()
	{
		return getArtcodes().getServer();
	}
}
