package uk.ac.horizon.artcodes.ui;

import android.text.InputFilter;
import android.text.Spanned;
import uk.ac.horizon.artcodes.model.Experience;

public class MarkerFormat implements InputFilter
{
	private final Experience experience;
	private final String code;

	public MarkerFormat(Experience experience, String code)
	{
		this.experience = experience;
		this.code = code;
	}

	@Override
	public CharSequence filter(CharSequence source, int sourceStart, int sourceEnd, Spanned destination, int destinationStart, int destinationEnd)
	{
		String sourceValue = source.subSequence(sourceStart, sourceEnd).toString();
		for (int index = 0; index < sourceValue.length(); index++)
		{
			if (!Character.isDigit(sourceValue.charAt(index)))
			{
				sourceValue = sourceValue.replace(sourceValue.charAt(index), ':');
			}
		}

		String result = destination.subSequence(0, destinationStart).toString() + sourceValue +
				destination.subSequence(destinationEnd, destination.length()).toString();
		if (result.equals(""))
		{
			return sourceValue;
		}

		boolean resultValid = isValidCode(result);
		if (!resultValid && !sourceValue.startsWith(":"))
		{
			sourceValue = ":" + sourceValue;
			resultValid = isValidCode(destination.subSequence(0, destinationStart).toString() + sourceValue +
					destination.subSequence(destinationEnd, destination.length()).toString());
		}
//		else if (!sourceValue.isEmpty() && !sourceValue.equals(result))
//		{
//			String[] segments = result.split(":");
//			if (segments.length < experience.getMinRegions())
//			{
//				if (isValidCode(destination.subSequence(0, destinationStart).toString() + sourceValue + ":" +
//						destination.subSequence(destinationEnd, destination.length()).toString()))
//				{
//					sourceValue = sourceValue + ":";
//					resultValid = true;
//				}
//			}
//		}

		if (resultValid && !source.subSequence(sourceStart, sourceEnd).toString().equals(sourceValue))
		{
			return sourceValue;
		}

		if (resultValid)
		{
			return null;
		}
		return "";
	}

	public boolean isValidCode(String code)
	{
		try
		{
			int lastValue = 0;
			String[] values = code.split(":");
			for (String string : values)
			{
				if (string.isEmpty())
				{
					return false;
				}
				int value = Integer.parseInt(string);
				if (value < lastValue)
				{
					return false;
				}
				lastValue = value;
			}

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

//	@Override
//	public String getError(Object value)
//	{
//		if (value == null)
//		{
//			return "No Code";
//		}
//		else if (value instanceof String)
//		{
//			String text = (String) value;
//			String error = experience.getMarkerError(text, false);
//			if (error != null)
//			{
//				return error;
//			}
//
//			if (experience.getMarkers().containsKey(text) && !text.equals(original))
//			{
//				return "Code Already Exists";
//			}
//		}
//		return null;
//	}

}