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

import android.support.annotation.StringRes;

import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.model.Experience;

public abstract class ExperienceEditFragment extends ArtcodeFragmentBase
{
	public Account getAccount()
	{
		return ((ExperienceEditActivity) getActivity()).getAccount();
	}

	public void setAccount(Account account)
	{
		((ExperienceEditActivity) getActivity()).setAccount(account);
	}

	public boolean displayAddFAB()
	{
		return false;
	}

	public void add()
	{

	}

	public void update()
	{

	}

	@StringRes
	public abstract int getTitleResource();

	Experience getExperience()
	{
		return ((ExperienceEditActivity) getActivity()).getExperience();
	}
}
