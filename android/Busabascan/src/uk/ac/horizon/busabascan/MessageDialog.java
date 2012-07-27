package uk.ac.horizon.busabascan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MessageDialog {
	
	public static void showMessage(int msgId, Context context){
		new AlertDialog.Builder(context)
		.setTitle(R.string.app_name)
		.setMessage(R.string.facebookErrMsg)
		.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		})
		.create();
	}
}
