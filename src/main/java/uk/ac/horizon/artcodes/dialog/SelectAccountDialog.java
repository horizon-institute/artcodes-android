package uk.ac.horizon.artcodes.dialog;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.util.ArrayList;
import java.util.List;

public class SelectAccountDialog extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		List<String> accountList = new ArrayList<>();
		final AccountManager manager = AccountManager.get(getActivity());
		final android.accounts.Account[] accounts = manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		for(android.accounts.Account account: accounts)
		{
			try
			{
				accountList.add(account.name);
			}
			catch (Exception e)
			{
				Log.i("", e.getMessage(), e);
			}
		}

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setTitle(R.string.pick_color)
//				.setItems(R.array.colors_array, new DialogInterface.OnClickListener()
//				{
//					public void onClick(DialogInterface dialog, int which)
//					{
//						// The 'which' argument contains the index position
//						// of the selected item
//					}
//				});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
