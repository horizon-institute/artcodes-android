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

package uk.ac.horizon.artcodes.server;

import android.location.Location;

import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;

public interface ArtcodeServer
{
	void add(Account account);

	Account getAccount(String id);

	Account createAccount(String id);

	List<Account> getAccounts();

	void saveRecent(List<String> recent);

	void saveStarred(List<String> starred);

	void loadExperience(String id, LoadCallback<Experience> callback);

	void loadRecent(LoadCallback<List<String>> callback);

	void loadRecommended(LoadCallback<Map<String, List<String>>> callback, Location location);

	void loadStarred(LoadCallback<List<String>> callback);

	void logScan(String uri, Action action);

	void search(String query, LoadCallback<List<String>> callback);
}
