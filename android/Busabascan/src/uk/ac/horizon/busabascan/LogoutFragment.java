package uk.ac.horizon.busabascan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LogoutFragment extends DialogFragment {

	private static final int LOGOUT_ITEM = 0;
	private static final int CANCEL_ITEM = 1;

	static LogoutFragment newInstance(){
		LogoutFragment frag = new LogoutFragment();
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.settings_title)
		.setItems(R.array.settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case LOGOUT_ITEM:
					((TWMainActivity)getActivity()).onSettingsLogoutItemClick(LogoutFragment.this);
					break;
				case CANCEL_ITEM:
					((TWMainActivity)getActivity()).onSettingsCancelItemClick(LogoutFragment.this);
					break;
				default:
					break;
				}
			}

		})
		.create();
	}
}
