/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.adapter;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.activity.ArtcodeActivity;
import uk.ac.horizon.artcodes.activity.ExperienceActivity;
import uk.ac.horizon.artcodes.databinding.ExperienceCardBinding;
import uk.ac.horizon.artcodes.databinding.GroupHeaderBinding;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.server.ArtcodeServer;
import uk.ac.horizon.artcodes.server.LoadCallback;

public class ExperienceGroupAdapter extends GridAdapter
{
	public class Group
	{
		private String name;
		private View.OnClickListener clickListener;
		private final SparseArray<Experience> experiences = new SparseArray<>();
		private List<String> ids = new ArrayList<>();

		public void setClickListener(View.OnClickListener clickListener)
		{
			this.clickListener = clickListener;
		}

		private void add(int index, Experience experience)
		{
			if (isEmpty())
			{
				experiences.append(index, experience);
				adapter.notifyItemRangeInserted(indexOf(name), 2);
			}
			else
			{
				final Experience existing = experiences.get(index);
				if (existing == null)
				{
					experiences.append(index, experience);
					adapter.notifyItemInserted(indexOf(experience));
				}
				else
				{
					experiences.append(index, experience);
					adapter.notifyItemChanged(indexOf(experience));
				}
			}
		}

		public boolean isEmpty()
		{
			return experiences.size() == 0;
		}

		public void setIds(List<String> ids)
		{
			this.ids = ids;
			update();
		}

		public boolean showMore()
		{
			return clickListener != null && ids.size() > layoutManager.getSpanCount();
		}

		private void update()
		{
			int index = 0;
			List<String> idList = ids;
			if(showMore())
			{
				idList = ids.subList(0, layoutManager.getSpanCount());
			}
			for (final String uri : idList)
			{
				if(experiences.get(index) == null)
				{
					final int experienceIndex = index;
					loadStarted();
					server.loadExperience(uri, new LoadCallback<Experience>()
					{
						@Override
						public void loaded(final Experience experience)
						{
							loadFinished();
							add(experienceIndex, experience);
						}

						@Override
						public void error(Throwable e)
						{
							showError(context.getString(R.string.connection_error));
							loadFinished();
						}
					});
				}
				index++;
			}
		}
	}

	private static final int EXPERIENCE_VIEW = 3;
	public static final int GROUP_VIEW = 9;
	private final String[] ordering = {"recent", "starred", "nearby", "featured", "new", "popular"};
	private final Map<String, Group> groups = new HashMap<>();
	private final ArtcodeServer server;

	public ExperienceGroupAdapter(Context context, ArtcodeServer server)
	{
		super(context);
		this.server = server;
	}

	@Override
	protected void columnsChanged()
	{
		for(Group group: groups.values())
		{
			if(group.clickListener != null)
			{
				group.update();
			}
		}
	}

	@Override
	public ViewDataBinding createBinding(final ViewGroup parent, final int viewType)
	{
		if (viewType == EXPERIENCE_VIEW)
		{
			return ExperienceCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		}
		return GroupHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
	}

	@Override
	public void bind(final int position, final ViewDataBinding binding)
	{
		Object item = getItemAt(position);
		if (binding instanceof ExperienceCardBinding && item instanceof Experience)
		{
			final Experience experience = (Experience) item;
			final ExperienceCardBinding experienceCardBinding = (ExperienceCardBinding) binding;
			experienceCardBinding.setExperience((Experience) item);
			experienceCardBinding.getRoot().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					ExperienceActivity.start(context, experience);
				}
			});
			experienceCardBinding.scanButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					ArtcodeActivity.start(context, experience);
				}
			});
		}
		else if (binding instanceof GroupHeaderBinding && item instanceof Group)
		{
			Group group = ((Group) item);
			((GroupHeaderBinding) binding).title.setText(getStringResourceByName(context, group.name));
			if (group.showMore())
			{
				binding.getRoot().setOnClickListener(group.clickListener);
				((GroupHeaderBinding) binding).moreButton.setOnClickListener(group.clickListener);
				((GroupHeaderBinding) binding).moreButton.setVisibility(View.VISIBLE);
			}
			else
			{
				binding.getRoot().setOnClickListener(null);
				((GroupHeaderBinding) binding).moreButton.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	protected int getSpan(final int position)
	{
		int viewType = adapter.getItemViewType(position);
		if (viewType == EXPERIENCE_VIEW)
		{
			return 1;
		}
		else
		{
			return layoutManager.getSpanCount();
		}
	}

	@Override
	public int getViewCount()
	{
		int size = 0;
		for (String groupName : ordering)
		{
			Group group = groups.get(groupName);
			if (group != null && !group.isEmpty())
			{
				size += 1;
				size += group.experiences.size();
			}
		}

		return size;
	}

	public Object getItemAt(int position)
	{
		int currentPosition = position;
		for (String groupName : ordering)
		{
			Group group = groups.get(groupName);
			if (group != null && !group.isEmpty())
			{
				if (currentPosition == 0)
				{
					return group;
				}

				currentPosition -= 1;

				if (currentPosition < group.experiences.size())
				{
					return group.experiences.get(group.experiences.keyAt(currentPosition));
				}

				currentPosition -= group.experiences.size();
			}
		}

		return null;
	}

	@Override
	public int getViewType(int position)
	{
		Object item = getItemAt(position);
		if (item instanceof Experience)
		{
			return EXPERIENCE_VIEW;
		}
		return GROUP_VIEW;
	}

	public Group getGroup(String name)
	{
		Group group = groups.get(name);
		if (group == null)
		{
			group = new Group();
			group.name = name;
			groups.put(name, group);
		}
		return group;
	}

	private int indexOf(String groupName)
	{
		int index = 0;
		for (String aGroup : ordering)
		{
			if (aGroup.equals(groupName))
			{
				return index;
			}
			Group group = groups.get(aGroup);
			if (group != null && !group.isEmpty())
			{
				index += 1;
				index += group.experiences.size();
			}
		}

		return -1;
	}

	private int indexOf(Experience experience)
	{
		int index = 0;
		for (String groupName : ordering)
		{
			Group group = groups.get(groupName);
			if (group != null && !group.isEmpty())
			{
				index += 1;
				int experienceIndex = group.experiences.indexOfValue(experience);
				if (experienceIndex > -1)
				{
					return index + experienceIndex;
				}
				index += group.experiences.size();
			}
		}

		return -1;
	}

	public LoadCallback<List<String>> getCallback(final String name, final View.OnClickListener clickListener)
	{
		return new LoadCallback<List<String>>()
		{
			@Override
			public void loaded(List<String> item)
			{
				Group group = getGroup(name);
				group.setClickListener(clickListener);
				group.setIds(item);
				loadFinished();
			}

			@Override
			public void error(Throwable e)
			{
				showError(context.getString(R.string.connection_error));
				loadFinished();
			}
		};
	}

	public LoadCallback<Map<String, List<String>>> getCallback()
	{
		return new LoadCallback<Map<String, List<String>>>()
		{
			@Override
			public void loaded(Map<String, List<String>> item)
			{
				for (String name : item.keySet())
				{
					Group group = getGroup(name);
					group.setIds(item.get(name));
				}
				loadFinished();
			}

			@Override
			public void error(Throwable e)
			{
				showError(context.getString(R.string.connection_error));
				loadFinished();
			}
		};
	}

	private String getStringResourceByName(Context context, String aString)
	{
		String packageName = context.getPackageName();
		int resId = context.getResources().getIdentifier(aString, "string", packageName);
		if (resId == 0)
		{
			return aString.substring(0, 1).toUpperCase() + aString.substring(1);
		}
		return context.getString(resId);
	}
}
