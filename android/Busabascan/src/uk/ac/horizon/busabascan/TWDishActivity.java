package uk.ac.horizon.busabascan;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class TWDishActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String dish = intent.getStringExtra(TWCameraMainActivity.DISH);
        
        //TextView textView = (TextView) findViewById(R.id.dishtext1);
        //textView.setText(dish);

        setContentView(R.layout.activity_twdish);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_twdish, menu);
        return true;
    }
}
