/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.account;

import com.google.android.gms.auth.UserRecoverableAuthException;

import java.util.List;

import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.LoadCallback;
import uk.ac.horizon.artcodes.server.URILoaderCallback;

public interface Account
{
	void loadLibrary(LoadCallback<List<String>> callback);

	void saveExperience(Experience experience);

	void deleteExperience(Experience experience);

	String getId();

	String getName();

	boolean canEdit(String uri);

	boolean isSaving(String uri);

	boolean logScan(String uri);

	boolean validates() throws UserRecoverableAuthException;

	boolean load(String uri, URILoaderCallback callback);
}
