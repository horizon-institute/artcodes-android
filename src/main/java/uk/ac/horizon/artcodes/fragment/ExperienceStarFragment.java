package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceStarBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallbackBase;

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
		adapter = new ExperienceAdapter(getActivity(), getServer());
		binding.list.setAdapter(adapter);

		return binding.getRoot();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		GoogleAnalytics.trackScreen("View Starred");
		binding.progress.addPending();
		getServer().loadStarred(new RequestCallbackBase<List<String>>()
		{
			@Override
			public void onResponse(List<String> item)
			{
				for(String uri: item)
				{
					binding.progress.addPending();
					getServer().loadExperience(uri, new RequestCallbackBase<Experience>()
					{
						@Override
						public void onResponse(Experience item)
						{
							binding.progress.removePending();
							adapter.onResponse(item);
						}
					});
				}
				binding.progress.removePending();
			}
		});
	}
}
