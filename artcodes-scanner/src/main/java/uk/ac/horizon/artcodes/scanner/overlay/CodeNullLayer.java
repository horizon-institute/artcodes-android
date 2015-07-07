package uk.ac.horizon.artcodes.scanner.overlay;

import uk.ac.horizon.artcodes.scanner.R;

public class CodeNullLayer extends Layer
{
	@Override
	public int getIcon()
	{
		return 0;
	}

	@Override
	public Layer getNext()
	{
		return new CodeLayer();
	}

	@Override
	int getFeedback()
	{
		return R.string.draw_code_off;
	}
}
