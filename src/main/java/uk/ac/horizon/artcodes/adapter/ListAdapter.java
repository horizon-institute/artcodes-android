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
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ViewDataBinding;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import uk.ac.horizon.artcodes.BR;
import uk.ac.horizon.artcodes.R;

public abstract class ListAdapter<T extends ViewDataBinding> extends BaseObservable
{
	private static class BindViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder
	{
		private final T binding;

		public BindViewHolder(T binding)
		{
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	private int loading = 0;
	private String emptyMessage;
	private int emptyIcon = R.drawable.ic_warning_black_144dp;
	private String errorMessage;
	private String emptyDetail = "";
	private int errorIcon = R.drawable.ic_warning_black_144dp;


	private boolean error = false;
	protected final Context context;
	protected final RecyclerView.Adapter<BindViewHolder<T>> adapter = new RecyclerView.Adapter<BindViewHolder<T>>()
	{
		@Override
		public int getItemViewType(final int position)
		{
			return getViewType(position);
		}

		@Override
		public BindViewHolder<T> onCreateViewHolder(final ViewGroup parent, final int viewType)
		{
			return new BindViewHolder<>(createBinding(parent, viewType));
		}

		@Override
		public void onBindViewHolder(final BindViewHolder<T> holder, final int position)
		{
			bind(position, holder.binding);
		}

		@Override
		public int getItemCount()
		{
			return getViewCount();
		}
	};

	protected ListAdapter(final Context context)
	{
		this.context = context;
		this.emptyMessage = context.getString(R.string.empty);
	}

	@Bindable
	public RecyclerView.Adapter getAdapter()
	{
		return adapter;
	}

	@Bindable
	public RecyclerView.LayoutManager getLayoutManager()
	{
		return new LinearLayoutManager(context);
	}

	public abstract T createBinding(ViewGroup parent, int viewType);

	public abstract void bind(int position, T binding);

	public abstract int getViewCount();

	public void setEmptyMessage(String message)
	{
		this.emptyMessage = message;
		notifyPropertyChanged(BR.errorMessage);
	}

	public void setEmptyIcon(@DrawableRes int icon)
	{
		this.emptyIcon = icon;
		notifyPropertyChanged(BR.errorIcon);
	}

	public void setEmptyDetail(String message)
	{
		this.emptyDetail = message;
		notifyPropertyChanged(BR.errorDetail);
	}

	@Bindable
	public RecyclerView.ItemDecoration getDecoration()
	{
		return null;
	}

	@Bindable
	public boolean getShowError()
	{
		return !isLoading() && getViewCount() == 0;
	}

	@Bindable
	public String getErrorMessage()
	{
		return error ? errorMessage : emptyMessage;
	}

	@DrawableRes
	@Bindable
	public int getErrorIcon()
	{
		return error ? errorIcon : emptyIcon;
	}

	@Bindable
	public String getErrorDetail()
	{
		return error ? "" : emptyDetail;
	}

	@Bindable
	public boolean isLoading()
	{
		return loading > 0;
	}

	public void loadStarted()
	{
		loading++;
		if (loading == 1)
		{
			notifyPropertyChanged(BR.loading);
			notifyPropertyChanged(BR.showError);
		}
	}

	protected int getViewType(int position)
	{
		return 0;
	}

	protected void showError(String errorMessage)
	{
		error = true;
		this.errorMessage = errorMessage;
		notifyPropertyChanged(BR.errorIcon);
		notifyPropertyChanged(BR.errorMessage);
		notifyPropertyChanged(BR.showError);
	}

	public void loadFinished()
	{
		loading--;
		if (loading <= 0)
		{
			if (loading < 0)
			{
				Log.i("a", "Attempted to finish a load that never started");
			}
			loading = 0;
			notifyPropertyChanged(BR.loading);
			notifyPropertyChanged(BR.showError);
		}
	}
}
