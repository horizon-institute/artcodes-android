package uk.ac.horizon.artcodes.scanner.overlay;

import uk.ac.horizon.artcodes.scanner.R;

public class MarkerNullLayer extends Layer
{
	@Override
	public int getIcon()
	{
		return R.drawable.ic_border_clear_white_24dp;
	}

	@Override
	public Layer getNext()
	{
		return new MarkerOutlineLayer();
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_marker_off;
	}
}
