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

package uk.ac.horizon.artcodes.server;

import android.content.Context;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;

public interface ArtcodeServer
{
	void add(Account account);

	Account getAccount(String id);

	List<Account> getAccounts();

	void loadStarred(RequestCallback<List<String>> callback);

	void loadRecommended(RequestCallback<Map<String, List<String>>> callback);

	void loadRecent(RequestCallback<List<String>> callback);

	void logScan(String uri, Action action, CameraAdapter adapter);

	void loadExperience(String id, RequestCallback<Experience> callback);

	<T> void load(String uri, Type type, RequestCallback<T> callback);

	Context getContext();

	Gson getGson();
}
