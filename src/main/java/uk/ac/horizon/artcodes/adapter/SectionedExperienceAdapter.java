package uk.ac.horizon.artcodes.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.activity.ExperienceActivity;
import uk.ac.horizon.artcodes.databinding.SectionExperienceItemBinding;
import uk.ac.horizon.artcodes.databinding.SectionHeaderBinding;
import uk.ac.horizon.artcodes.databinding.WelcomeBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.ui.IntentBuilder;

import java.util.HashMap;
import java.util.Map;

public class SectionedExperienceAdapter extends RecyclerView.Adapter<SectionedExperienceAdapter.ViewHolderBase>
{
	private static final int EXPERIENCE_VIEW = 0;
	private static final int SECTION_VIEW = 1;
	private static final int HEADER_VIEW = 2;

	private String[] ordering = {"recent", "nearby", "featured", "new", "popular"};
	private Map<String, SparseArray<Experience>> experienceMap = new HashMap<>();

	public abstract class ViewHolderBase extends RecyclerView.ViewHolder
	{
		public ViewHolderBase(View root)
		{
			super(root);
		}

		public abstract void setItem(Object item);
	}

	private class ExperienceViewHolder extends ViewHolderBase
	{
		private SectionExperienceItemBinding binding;

		public ExperienceViewHolder(SectionExperienceItemBinding binding)
		{
			super(binding.getRoot());
			this.binding = binding;
		}

		@Override
		public void setItem(Object item)
		{
			final Experience experience = (Experience) item;
			binding.setExperience(experience);
			binding.getRoot().setOnClickListener(new View.OnClickListener()
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
			binding.scanButton.setOnClickListener(new View.OnClickListener()
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
	}

	private class WelcomeViewHolder extends ViewHolderBase
	{
		private WelcomeBinding binding;

		public WelcomeViewHolder(WelcomeBinding welcomeBinding)
		{
			super(welcomeBinding.getRoot());
			this.binding = welcomeBinding;
			binding.dismissButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					setShowHeaderItem(false);
					Feature.get(server.getContext(), R.bool.feature_show_welcome).setEnabled(false);
				}
			});
			binding.moreButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					IntentBuilder.with(context)
							.setAction(Intent.ACTION_VIEW)
							.setURI("http://aestheticodes.com/info/")
							.start();
				}
			});
		}

		@Override
		public void setItem(Object item)
		{

		}
	}

	private class SectionViewHolder extends ViewHolderBase
	{
		private SectionHeaderBinding binding;

		public SectionViewHolder(SectionHeaderBinding binding)
		{
			super(binding.getRoot());
			this.binding = binding;
		}

		@Override
		public void setItem(Object item)
		{
			String group = (String) item;
			binding.title.setText(getStringResourceByName(group));
		}
	}

	private boolean hasHeaderItem = false;

	private final ArtcodeServer server;
	private final Context context;

	public SectionedExperienceAdapter(Context context, ArtcodeServer server)
	{
		super();
		this.context = context;
		this.server = server;
	}

	public void setShowHeaderItem(boolean showHeader)
	{
		this.hasHeaderItem = showHeader;
		if (showHeader)
		{
			notifyItemInserted(0);
		}
		else
		{
			notifyItemRemoved(0);
		}
	}

	@Override
	public int getItemCount()
	{
		int size = 0;
		if (hasHeaderItem)
		{
			size += 1;
		}

		for (String group : ordering)
		{
			SparseArray<Experience> experiences = experienceMap.get(group);
			if (experiences != null && experiences.size() != 0)
			{
				size += 1;
				size += experiences.size();
			}
		}

		return size;
	}

	public Object getItemAt(int position)
	{
		int currentPosition = position;
		if (hasHeaderItem)
		{
			if (currentPosition == 0)
			{
				// TODO
				return null;
			}
			currentPosition -= 1;
		}

		for (String group : ordering)
		{
			SparseArray<Experience> experiences = experienceMap.get(group);
			if (experiences != null && experiences.size() != 0)
			{
				if (currentPosition == 0)
				{
					return group;
				}

				currentPosition -= 1;

				if (currentPosition < experiences.size())
				{
					return experiences.get(experiences.keyAt(currentPosition));
				}

				currentPosition -= experiences.size();
			}
		}

		return null;
	}

	@Override
	public int getItemViewType(int position)
	{
		if (position == 0 && hasHeaderItem)
		{
			return HEADER_VIEW;
		}
		else
		{
			Object item = getItemAt(position);
			if (item instanceof Experience)
			{
				return EXPERIENCE_VIEW;
			}
		}
		return SECTION_VIEW;
	}

	public void addExperience(Experience experience, String group, int index)
	{
		if (Looper.getMainLooper().getThread() != Thread.currentThread())
		{
			throw new RuntimeException("Not on UI thread");
		}
		SparseArray<Experience> experiences = experienceMap.get(group);
		if (experiences == null)
		{
			experiences = new SparseArray<>();
			experienceMap.put(group, experiences);
			experiences.append(index, experience);
			notifyItemRangeInserted(indexOf(group), 2);
		}
		else
		{
			experiences.append(index, experience);
			notifyItemInserted(indexOf(experience));
		}
	}

	private int indexOf(String group)
	{
		int index = 0;
		if (hasHeaderItem)
		{
			index += 1;
		}

		for (String aGroup : ordering)
		{
			if (aGroup.equals(group))
			{
				return index;
			}
			SparseArray<Experience> experiences = experienceMap.get(aGroup);
			if (experiences != null && experiences.size() != 0)
			{
				index += 1;
				index += experiences.size();
			}
		}

		return -1;
	}

	private int indexOf(Experience experience)
	{
		int index = 0;
		if (hasHeaderItem)
		{
			index += 1;
		}

		for (String group : ordering)
		{
			SparseArray<Experience> experiences = experienceMap.get(group);
			if (experiences != null && experiences.size() != 0)
			{
				index += 1;
				int experienceIndex = experiences.indexOfValue(experience);
				if (experienceIndex > -1)
				{
					return index + experienceIndex;
				}
				index += experiences.size();
			}
		}

		return -1;
	}

	private String getStringResourceByName(String aString)
	{
		String packageName = server.getContext().getPackageName();
		int resId = server.getContext().getResources().getIdentifier(aString, "string", packageName);
		if (resId == 0)
		{
			return aString.substring(0, 1).toUpperCase() + aString.substring(1);
		}
		return server.getContext().getString(resId);
	}

	@Override
	public void onBindViewHolder(ViewHolderBase holder, int position)
	{
		holder.setItem(getItemAt(position));
	}

	@Override
	public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == EXPERIENCE_VIEW)
		{
			return new ExperienceViewHolder(SectionExperienceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}
		else if (viewType == HEADER_VIEW)
		{
			return new WelcomeViewHolder(WelcomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}
		else if (viewType == SECTION_VIEW)
		{
			return new SectionViewHolder(SectionHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}

		return null;
	}
}
