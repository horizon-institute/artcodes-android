package uk.ac.horizon.busabascan;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class TWDishActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String dish_name = intent.getStringExtra(TWCameraMainActivity.DISH);
        
        setContentView(R.layout.activity_twdish);
        
        Dish dish = Dish.dishFactory(dish_name);
        
        TextView textView = (TextView) findViewById(R.id.textView1);
        textView.setText(dish_name);
        ImageView image = (ImageView) findViewById(R.id.imageView1);
        int img_id = dish.getImageId();
        image.setImageDrawable(this.getResources().getDrawable(img_id));
        RatingBar rating = (RatingBar) findViewById(R.id.ratingBar1);
        rating.setRating(dish.getStars());
        TextView raters = (TextView) findViewById(R.id.textViewCount);
        raters.setText(dish.getRaters().toString());
        TextView desc = (TextView) findViewById(R.id.dishdetail);
        desc.setText(dish.getDescription());
        TextView review = (TextView) findViewById(R.id.dishreview);
        review.setText(dish.getReview());
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_twdish, menu);
        return true;
    }
}
