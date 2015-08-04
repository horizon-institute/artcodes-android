package uk.ac.horizon.artcodes.account;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import uk.ac.horizon.artcodes.ExperienceParser;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;
import uk.ac.horizon.artcodes.scanner.camera.CameraAdapter;
import uk.ac.horizon.artcodes.source.Source;
import uk.ac.horizon.artcodes.source.Target;

import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

public class LocalAccount extends AccountBase
{
	public static class Info implements AccountInfo
	{
		private final Context context;

		public Info(Context context)
		{
			this.context = context;
		}

		@Override
		public String getId()
		{
			return "local";
		}

		@Override
		public String getName()
		{
			return context.getString(R.string.device);
		}

		@Override
		public String getUsername()
		{
			return null;
		}

		@Override
		public Drawable getIcon()
		{
			return context.getResources().getDrawable(R.drawable.ic_smartphone_black_36dp);
		}

		@Override
		public Account create()
		{
			return new LocalAccount(context);
		}
	}

	public AccountInfo getInfo()
	{
		return new Info(context);
	}

	public LocalAccount(Context context)
	{
		super(context, ExperienceParser.createGson(context));
	}

	private File getDirectory()
	{
		return context.getDir("experiences", Context.MODE_PRIVATE);
	}

	@Override
	public void saveExperience(final Experience experience)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					File directory = getDirectory();
					if (experience.getId() == null || !experience.getId().startsWith("file:"))
					{
						String id = UUID.randomUUID().toString();
						File file = new File(directory, id);
						experience.setId(file.toURI().toString());

						FileWriter writer = new FileWriter(file);
						getGson().toJson(experience, writer);
						writer.flush();
						writer.close();
					}
				}
				catch (Exception e)
				{
					Log.e("", e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public Source<Experience> getLibrary()
	{
		return new Source<Experience>()
		{
			@Override
			public void loadInto(final Target<Experience> target)
			{
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						File directory = getDirectory();
						for (File file : directory.listFiles())
						{
							getSource(file.toURI().toString(), Experience.class).loadInto(target);
						}
					}
				});
			}
		};
	}

	@Override
	public void scanned(String uri, String marker, CameraAdapter adapter)
	{

	}
}
