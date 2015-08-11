package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.account.Account;
import uk.ac.horizon.artcodes.activity.ExperienceEditActivity;
import uk.ac.horizon.artcodes.adapter.ExperienceAdapter;
import uk.ac.horizon.artcodes.databinding.ExperienceLibraryBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallbackBase;
import uk.ac.horizon.artcodes.ui.IntentBuilder;

public class ExperienceLibraryFragment extends ArtcodeFragmentBase
{
	private ExperienceLibraryBinding binding;
	private ExperienceAdapter adapter;
	private Account account;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = ExperienceLibraryBinding.inflate(inflater, container, false);
		//binding.list.setHasFixedSize(true);
		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
		adapter = new ExperienceAdapter(getActivity(), getServer());
		binding.list.setAdapter(adapter);
		binding.progress.setEnabled(false);
		binding.fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				IntentBuilder.with(getActivity())
						.target(ExperienceEditActivity.class)
						.setServer(getServer())
						.set("experience", new Experience())
						.set("account", getAccount().getId())
						.start();
			}
		});

		return binding.getRoot();
	}

	public Account getAccount()
	{
		if (account == null)
		{
			if (getArguments() != null)
			{
				String accountID = getArguments().getString("account");
				if (accountID != null)
				{
					Account selected = getServer().getAccount(accountID);
					if (selected != null)
					{
						account = selected;
					}
				}
			}
		}
		return account;
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
			} else
			{
				binding.fab.hide();
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		GoogleAnalytics.trackScreen("View Library");
		binding.progress.addPending();
		getAccount().loadLibrary(new RequestCallbackBase<List<String>>()
		{
			@Override
			public void onResponse(List<String> item)
			{
				for (String uri : item)
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
