package uk.ac.horizon.artcodes.fragment;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.model.Experience;

public abstract class ExperienceEditFragment extends Fragment
{
	public Experience getExperience()
	{
		return ((ExperienceEditActivity) getActivity()).getExperience();
	}

	@StringRes
	public abstract int getTitleResource();
}
