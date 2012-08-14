package uk.ac.horizon.busabascan;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;



public class RestaurantListAdapter extends BaseExpandableListAdapter {
	//The number of list children added programatically
	private final static int QUEUE_IDX = 0;
	private final static int SPECIALS_IDX = 3;

	
// Sample data set. children[i] contains the children (String[]) for
// groups[i].
private String[] locations = { "Wardour St", "Store St",
    "Bird St", "Panton St", "Old St", "Westfield Sheperd's Bush", "Bicester Village", "Floral St", "King's Road", "Westfield Stratford City" };
private String[][] detail = { 
		{ "20", "106Ğ110 Wardour St. London W1F 0TR", "tel: 020 7255 8686","Today's Specials" },
		{ "15", "22 Store Street, London WC1E 7DF", "tel: 020 7299 7900","Today's Specials" },
		{ "0", "8Ğ13 Bird Street, London W1U 1BU", "tel: 020 7518 8080","Today's Specials" },
		{ "0", "35 Panton Street, London SW1Y 4EA", "tel: 020 7930 0088","Today's Specials" },
		{ "40", "319 Old Street, London EC1V 9LE", "tel: 020 7729 0808","Today's Specials" },
		{ "0", "Westfield Shepherd's Bush, London W12 7GA", "tel: 020 3249 1919","Today's Specials" },
		{ "10", "Bicester Village, Oxfordshire OX266WD", "tel: 01869 362 700","Today's Specials" },
		{ "25", "44 Floral Street, London WC2E 9DA", "tel: 020 7759 0088","Today's Specials" },
		{ "0", "358 King's Road, London SW3 5UZ", "tel: 020 7349 5488","Today's Specials" },
		{ "0", "Westfield Stratford City, London E20 1GL", "tel: 020 8221 8989","Today's Specials" } };

private Activity activity;

RestaurantListAdapter(Activity act) {
	    activity = act;
}


public Object getChild(int groupPosition, int childPosition) {
	Object ret;
	if (childPosition == QUEUE_IDX)
	{
		int wait = Integer.parseInt(detail[groupPosition][QUEUE_IDX]);
		if (wait == 0)
		{
	        ret = activity.getResources().getString(R.string.no_queue);
		}
		else
		{
			ret =  activity.getResources().getString(R.string.queue_time) +
			detail[groupPosition][QUEUE_IDX] + 
			activity.getResources().getString(R.string.minutes);
		}
	}
	else
	{
	    ret = detail[groupPosition][childPosition];
	}
	return ret;
}

public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
}

public int getChildrenCount(int groupPosition) {
    int i = 0;
    try {
    i = detail[groupPosition].length;

    } catch (Exception e) {
    }

    return i;
}

public TextView getGenericView() {
    // Layout parameters for the ExpandableListView
    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
        ViewGroup.LayoutParams.FILL_PARENT, 64);

    TextView textView = new TextView(activity);
    textView.setLayoutParams(lp);
    // Center the text vertically
    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    //textView.setTextColor(Color.parseColor("darkblue"));
	textView.setBackgroundColor(activity.getResources().getColor(R.color.base_brown));
    // Set the text starting position
    textView.setPadding(36, 0, 0, 0);
    return textView;
}

public Button getButton(String text) {
    // Layout parameters for the ExpandableListView
    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, 64);

    Button button = new Button(activity);
    button.setLayoutParams(lp);
    // Center the text vertically
    button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    // Set the text starting position
 
    //button.setPadding(36, 0, 0, 0);
    button.setText(text);
    return button;

}

public View getChildView(int groupPosition, int childPosition,
    boolean isLastChild, View convertView, ViewGroup parent) {
	if (childPosition == SPECIALS_IDX)
	{
		Button button = getButton(getChild(groupPosition, childPosition).toString());
		button.setEnabled(false);
		return button;
	}
	else
	{
        TextView textView = getGenericView();
        textView.setText(getChild(groupPosition, childPosition).toString());
        return textView;
	}
}

public Object getGroup(int groupPosition) {
    return locations[groupPosition];
}

public int getGroupCount() {
    return locations.length;
}

public long getGroupId(int groupPosition) {
    return groupPosition;
}

public View getGroupView(int groupPosition, boolean isExpanded,
    View convertView, ViewGroup parent) {
    
//    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT, 64);
    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
            320, 64);

    TextView textView = new TextView(activity);
    textView.setLayoutParams(lp);
    // Center the text vertically
    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    //textView.setTextColor(Color.parseColor("darkblue"));
    // Set the text starting position
    textView.setPadding(60, 0, 0, 0);
    textView.setText(getGroup(groupPosition).toString());
    
    int progress = 100 - getNormalisedQueueTime(groupPosition);
    
    ProgressBar queueLengthIndicator;
    queueLengthIndicator = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
    queueLengthIndicator.setProgress(progress);
    queueLengthIndicator.setMax(100);
    LinearLayout.LayoutParams progsize = new LinearLayout.LayoutParams(140,
    		LinearLayout.LayoutParams.WRAP_CONTENT);
    queueLengthIndicator.setLayoutParams(progsize);
    
//    ViewGroup.LayoutParams wrap_content = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
//    		LayoutParams.WRAP_CONTENT);
    //create horizontal section
    LinearLayout horizontalLayout = new LinearLayout(activity); 
    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL); 
//    horizontalLayout.setLayoutParams(wrap_content);
 
    horizontalLayout.addView(textView);
    horizontalLayout.addView(queueLengthIndicator);
    
    return horizontalLayout;
}

public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
}

public boolean hasStableIds() {
    return true;
}

private int getNormalisedQueueTime(int position)
{
	//Get the wait in minutes
	int qt = Integer.parseInt(detail[position][QUEUE_IDX]);
	//Maximum normalised queue time is one hour
	if (qt > 60) {qt = 60;}
	qt = (qt*100)/60;
	return qt;
}	

}
