package uk.ac.horizon.artcodes.fragment;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.model.Experience;

public abstract class ExperienceEditFragment extends ArtcodeFragmentBase
{
	public Experience getExperience()
	{
		return ((ExperienceEditActivity) getActivity()).getExperience();
	}

	public Account getAccount()
	{
		return ((ExperienceEditActivity) getActivity()).getAccount();
	}

	public void setAccount(Account account)
	{
		((ExperienceEditActivity) getActivity()).setAccount(account);
	}


	@StringRes
	public abstract int getTitleResource();
}
