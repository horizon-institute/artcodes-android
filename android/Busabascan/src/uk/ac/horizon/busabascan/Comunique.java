package uk.ac.horizon.busabascan;

import java.io.FileNotFoundException;

import uk.ac.horizon.data.DataMarker;
import uk.ac.horizon.data.DataMarkerWebServices;
import uk.ac.horizon.data.DataMarkerWebServices.MarkerDownloadRequestListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

// This class handles both the user interface aspects of sending a communique to
// the waiting staff and the api to the server.
public class Comunique {

	private View sourceView;
	private Context context;
	private String   activeMessage;  //The current message to the server
	
	Comunique(View v)
	{
		super();
		sourceView = v;
		context = sourceView.getContext();
	}
	
	//initiate the sending of communication to the server.
	//message is sent via the API
	//title is the message of the confirmation dialog.
	void send(String message, String title)
	{
		activeMessage = message;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(title)
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                sendToServer(activeMessage);
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void sendToServer(String message)
	{
    	DataMarkerWebServices dtouchMarkerWebServices = new DataMarkerWebServices(new MarkerDownloadRequestListener(){
    		@Override
			public void onMarkerDownloadError() {
    	    	MessageDialog.showMessage(R.string.marker_download_error, context);
			}

			@Override
			public void onMarkerDownloaded(DataMarker marker) {
				//markerDownloaded(marker);
    	    	MessageDialog.showMessage(R.string.comunique_success, context);
			}
    	});
    	
    	String originator=null;
    	TWFacebookUser fbUser = new TWFacebookUser();
    	try {
			fbUser.restoreMember(context);
			originator = fbUser.name;
		} catch (FileNotFoundException e){};
    	dtouchMarkerWebServices.executeComuniqueRequest(message, originator);
 	}

}
