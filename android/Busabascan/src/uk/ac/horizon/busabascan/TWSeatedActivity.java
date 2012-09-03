package uk.ac.horizon.busabascan;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class TWSeatedActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twseated);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_twseated, menu);
        return true;
    }
}
