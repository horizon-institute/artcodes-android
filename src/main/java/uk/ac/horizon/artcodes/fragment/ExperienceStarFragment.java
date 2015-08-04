package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceStarBinding;

public class ExperienceStarFragment extends ArtcodeFragmentBase
{
	private ExperienceStarBinding binding;
	private ExperienceAdapter adapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceStarBinding.inflate(inflater, container, false);
		//binding.list.setHasFixedSize(true);
		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
		adapter = new ExperienceAdapter(getActivity());
		binding.list.setAdapter(adapter);

		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		binding.progress.setRefreshing(true);
		getAccount().getStarred().loadInto(adapter);
	}
}
