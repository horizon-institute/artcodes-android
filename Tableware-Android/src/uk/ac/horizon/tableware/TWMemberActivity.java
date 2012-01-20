package uk.ac.horizon.tableware;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TWMemberActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memberinfo);
        initMemberInfo();
    }
	
	private void initMemberInfo(){
		TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
		String memberName = new TWPreference(this).getMemberName();
		nameTextView.setText(memberName);
	}
}
