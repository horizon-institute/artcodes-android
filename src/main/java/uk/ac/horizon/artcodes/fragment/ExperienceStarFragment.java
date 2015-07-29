package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceStarBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.storage.ExperienceListStore;
import uk.ac.horizon.artcodes.storage.ExperienceStorage;
import uk.ac.horizon.artcodes.storage.StoreListener;

import java.util.List;

public class ExperienceStarFragment extends Fragment
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

		loadExperiences();

		return binding.getRoot();
	}

	private void loadExperiences()
	{
		binding.progress.setRefreshing(true);
		List<String> list = ExperienceListStore.with(getActivity(), "starred").get();
		for (String uri : list)
		{
			ExperienceStorage.load(Experience.class).fromUri(uri).async(new StoreListener<Experience>()
			{
				@Override
				public void onItemChanged(Experience item)
				{
					adapter.add(item);
				}
			});
		}
	}
}
