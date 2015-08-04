package uk.ac.horizon.artcodes.ui;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.BR;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExperienceEditor
{
	private final Experience experience;

	public ExperienceEditor(Experience experience)
	{
		this.experience = experience;
	}

	public SimpleTextWatcher getDescWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return experience.getDescription();
			}

			@Override
			public void onTextChanged(String value)
			{
				if (!value.equals(experience.getDescription()))
				{
					experience.setDescription(value);
					experience.notifyPropertyChanged(BR.description);
				}
			}
		};
	}


	public CompoundButton.OnCheckedChangeListener getPublishedListener()
	{
		return new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					experience.setVisibility(Experience.Visibility.published);
				}
				else
				{
					experience.setVisibility(Experience.Visibility.secret);
				}
				experience.notifyPropertyChanged(BR.published);
			}
		};
	}

	public SimpleTextWatcher getNameWatcher()
	{
		return new SimpleTextWatcher()
		{
			@Override
			public String getText()
			{
				return experience.getName();
			}

			@Override
			public void onTextChanged(String value)
			{
				if (!value.equals(experience.getName()))
				{
					experience.setName(value);
					experience.notifyPropertyChanged(BR.name);
				}
			}
		};
	}

}
