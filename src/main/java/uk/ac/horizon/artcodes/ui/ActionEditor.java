package uk.ac.horizon.artcodes.ui;

import android.net.Uri;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.scanner.BR;

public class ActionEditor
{
	private final Action action;

	public ActionEditor(Action action)
	{
		this.action = action;
	}

	public SimpleTextWatcher getNameWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return action.getName();
			}

			@Override
			public void onTextChanged(String value)
			{
				if(value.trim().isEmpty())
				{
					if(action.getName() != null)
					{
						action.setName(null);
						action.notifyPropertyChanged(BR.name);
					}
				}
				else if (!value.equals(action.getName()))
				{
					action.setName(value);
					action.notifyPropertyChanged(BR.name);
				}
			}
		};
	}

	public SimpleTextWatcher getUrlWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return action.getDisplayUrl();
			}

			@Override
			public void onTextChanged(String value)
			{
				if(value.trim().isEmpty())
				{
					if(action.getUrl() != null)
					{
						action.setUrl(null);
						action.notifyPropertyChanged(BR.url);
						action.notifyPropertyChanged(BR.displayUrl);
					}
				}
				else
				{
					Uri uri = Uri.parse(value);
					String urlValue = value;
					if (uri.getScheme() == null)
					{
						urlValue = Action.HTTP_PREFIX + value;
					}
					if (!urlValue.equals(action.getUrl()))
					{
						action.setUrl(urlValue);
						action.notifyPropertyChanged(BR.url);
						action.notifyPropertyChanged(BR.displayUrl);
					}
				}
			}
		};
	}
}
