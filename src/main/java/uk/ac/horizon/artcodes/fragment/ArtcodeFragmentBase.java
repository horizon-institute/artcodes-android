/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.fragment;

import android.support.v4.app.Fragment;

import uk.ac.horizon.artcodes.Artcodes;
import uk.ac.horizon.artcodes.server.ArtcodeServer;

public class ArtcodeFragmentBase extends Fragment
{
	ArtcodeServer getServer()
	{
		return getArtcodes().getServer();
	}

	private Artcodes getArtcodes()
	{
		return (Artcodes) getActivity().getApplication();
	}
}
