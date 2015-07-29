package uk.ac.horizon.artcodes.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceLibraryBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.storage.ExperienceListStore;
import uk.ac.horizon.artcodes.storage.ExperienceStorage;
import uk.ac.horizon.artcodes.storage.StoreListener;

import java.util.List;

public class ExperienceLibraryFragment extends Fragment
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
		binding.fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), ExperienceEditActivity.class));
			}
		});

		loadExperiences();

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

	private void loadExperiences()
	{
		//binding.progress.setRefreshing(true);

		ExperienceStorage.loadLibrary().async(new StoreListener<List<String>>()
		{
			@Override
			public void onItemChanged(final List<String> uris)
			{
				ExperienceListStore.with(getActivity(), "library").set(uris);

				//binding.progress.setRefreshing(false);
				for (String uri : uris)
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
		});
	}
}
