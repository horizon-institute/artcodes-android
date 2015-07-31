package uk.ac.horizon.artcodes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.artcodes.Feature;
import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.adapter.FeatureAdapter;
import uk.ac.horizon.artcodes.databinding.FeatureListBinding;

import java.lang.reflect.Field;

public class FeatureListFragment extends Fragment
{
	private void addFeature(FeatureAdapter adapter, int featureID)
	{
		Feature feature = Feature.get(getActivity(), featureID);
		if(feature.getName().startsWith("feature_"))
		{
			adapter.add(feature);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		FeatureListBinding binding = FeatureListBinding.inflate(inflater, container, false);

		binding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
		final FeatureAdapter adapter = new FeatureAdapter(getActivity());
		binding.list.setAdapter(adapter);

		final R.bool features = new R.bool();
		final Field[] fields = features.getClass().getDeclaredFields();
		for (Field field: fields)
		{
			try
			{
				addFeature(adapter, field.getInt(features));
			}
			catch (Exception e)
			{
				Log.i("", e.getMessage(), e);
			}
		}


		final uk.ac.horizon.artcodes.scanner.R.bool scannerFeatures = new uk.ac.horizon.artcodes.scanner.R.bool();
		final Field[] scannerFields = scannerFeatures.getClass().getDeclaredFields();
		for (Field field: scannerFields)
		{
			try
			{
				addFeature(adapter, field.getInt(features));
			}
			catch (Exception e)
			{
				Log.i("", e.getMessage(), e);
			}
		}

		return binding.getRoot();
	}
}
