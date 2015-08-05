package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceLibraryBinding;
import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceLibraryFragment extends ArtcodeFragmentBase
{
	private ExperienceLibraryBinding binding;
	private ExperienceAdapter adapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceLibraryBinding.inflate(inflater, container, false);
		//binding.list.setHasFixedSize(true);
		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
		adapter = new ExperienceAdapter(getActivity());
		binding.list.setAdapter(adapter);
		binding.progress.setEnabled(false);
		binding.fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ExperienceEditActivity.start(getActivity(), new Experience());
			}
		});

		return binding.getRoot();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isInLayout())
		{
			if (isVisibleToUser)
			{
				binding.fab.show();
			}
			else
			{
				binding.fab.hide();
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getAccount().getLibrary().loadInto(adapter);
	}
}
