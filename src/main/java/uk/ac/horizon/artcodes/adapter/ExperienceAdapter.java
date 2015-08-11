package uk.ac.horizon.artcodes.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.collect.Ordering;
import uk.ac.horizon.artcodes.GoogleAnalytics;
import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.activity.ExperienceActivity;
import uk.ac.horizon.artcodes.databinding.ExperienceItemBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.request.RequestCallback;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.ui.IntentBuilder;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder> implements RequestCallback<Experience>
{
	public static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER =
			Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();

	public class ExperienceViewHolder extends RecyclerView.ViewHolder
	{
		private ExperienceItemBinding binding;

		public ExperienceViewHolder(ExperienceItemBinding binding)
		{
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	private final SortedList<Experience> experiences;
	private final ArtcodeServer server;
	private final Context context;

	public ExperienceAdapter(Context context, ArtcodeServer server)
	{
		super();
		this.context = context;
		this.server = server;
		experiences = new SortedList<>(Experience.class, new SortedListAdapterCallback<Experience>(this)
		{
			@Override
			public boolean areContentsTheSame(Experience oldItem, Experience newItem)
			{
				return TextUtils.equals(oldItem.getId(), newItem.getId());
			}

			@Override
			public boolean areItemsTheSame(Experience item1, Experience item2)
			{
				return TextUtils.equals(item1.getId(), item2.getId());
			}

			@Override
			public int compare(Experience o1, Experience o2)
			{
				int result = CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(o1.getName(), o2.getName());
				if (result != 0)
				{
					return result;
				}
				return CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(o1.getId(), o2.getId());
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return experiences.size();
	}

	@Override
	public void onBindViewHolder(ExperienceViewHolder holder, int position)
	{
		final Experience experience = experiences.get(position);
		holder.binding.setExperience(experience);
		holder.binding.getRoot().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				IntentBuilder.with(context)
						.target(ExperienceActivity.class)
						.setServer(server)
						.set("experience", experience)
						.start();
			}
		});
		holder.binding.scanButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				IntentBuilder.with(context)
						.target(ArtcodeActivity.class)
						.setServer(server)
						.set("experience", experience)
						.start();
			}
		});
	}

	@Override
	public ExperienceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ExperienceViewHolder(ExperienceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onResponse(Experience item)
	{
		experiences.add(item);
	}

	@Override
	public void onError(Exception e)
	{
		GoogleAnalytics.trackException(e);
	}
}
