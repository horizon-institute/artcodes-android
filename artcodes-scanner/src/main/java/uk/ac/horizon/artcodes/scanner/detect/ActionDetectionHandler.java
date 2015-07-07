package uk.ac.horizon.artcodes.scanner.detect;

import com.google.common.collect.Multiset;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.Experience;

public abstract class ActionDetectionHandler extends MarkerDetectionHandler
{
	private final Experience experience;
	private Action action;

	public ActionDetectionHandler(Experience experience)
	{
		this.experience = experience;
	}

	public abstract void onActionChanged(Action action);

	@Override
	public void onMarkersDetected(Multiset<String> markers)
	{
		int best = 0;
		Action selected = null;
		for (Action action : experience.getActions())
		{
			if (action.getMatch() == Action.Match.any)
			{
				for (String code : action.getCodes())
				{
					int count = markers.count(code);
					if (count > best)
					{
						selected = action;
						best = count;
					}
				}
			}
			else if (action.getMatch() == Action.Match.all)
			{
				int min = MAX;
				int total = 0;
				for (String code : action.getCodes())
				{
					int count = markers.count(code);
					min = Math.min(min, count);
					total += count;
				}

				if (min > REQUIRED && total > best)
				{
					best = total;
					selected = action;
				}
			}
		}

		if (selected == null || best < REQUIRED)
		{
			if (action != null)
			{
				action = null;
				onActionChanged(null);
			}
		}
		else if (selected != action)
		{
			action = selected;
			onActionChanged(action);
		}
	}
}
