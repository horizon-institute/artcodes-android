package uk.ac.horizon.artcodes.storage;

import android.os.Environment;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Experience;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExperienceFileStore extends FileStore implements ExperienceStore
{
	@Override
	public int getAccountType()
	{
		return R.string.device;
	}

	@Override
	public int getIcon()
	{
		return R.drawable.ic_smartphone_black_36dp;
	}

	@Override
	public String getId()
	{
		return "local";
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public int getSaveText()
	{
		return R.string.save_local;
	}

	@Override
	public Loader<List<String>> library()
	{
		return null;
	}

	@Override
	public Loader<Map<String, List<String>>> recommended()
	{
		return null;
	}

	@Override
	public void save(Saver saver)
	{
		Experience experience = saver.getItem();
		String originalID = experience.getId();
		File file = null;
		if (experience.getId().startsWith("file:"))
		{
			try
			{
				file = new File(URI.create(experience.getId()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		try
		{

			if (file == null)
			{
				String id = UUID.randomUUID().toString();
				final File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				final File directory = new File(parent, "experiences");
				if (!directory.exists())
				{
					boolean success = directory.mkdir();
				}

				file = new File(directory, id);
			}

			FileWriter writer = new FileWriter(file);

			experience.setId("file://" + file.getAbsolutePath());
			if (!experience.getId().equals(originalID))
			{
				experience.setOriginalID(originalID);
			}

			writer.write(saver.toJson());

			//RecentExperiences.with(context).remove(originalID);
			//RecentExperiences.with(context).add(experience.getId());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
