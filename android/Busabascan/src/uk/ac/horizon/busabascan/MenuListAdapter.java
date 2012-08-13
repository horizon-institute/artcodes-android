package uk.ac.horizon.busabascan;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuListAdapter extends BaseExpandableListAdapter {

    private String[][] items = { 
            {"h1. Salad"},
            {"Som tam", "green papaya salad with dried shrimp and cherry tomato (s)","£6.90"},
            {"Som yam", "pomelo salad, mango, grapefruit, coconut and miang sauce","£6.70"},
            {"Salmon and green mango salad","lime leaf, peanut, chilli and nam pla dee sauce (s)","£7.90"},
            {"Grilled aubergine", "Thai red shallot, white fungus, chilli and mint (s,v)", "£6.60"},
            {"h1. Soup noodle"},
            {"Tom kha chicken", "lemongrass chicken, glass noodle, coconut and galangal soup", "£6.70"},
            {"Tom yam talay", "prawn, squid and baby clam, vermicelli noodle, spicy sour soup (s)", "£6.70"},
            {"Tom yam goong", "prawn, vermicelli noodle, spicy sour soup (s)", "£6.70"},
            {"Tom sen chicken", "chargrilled chicken, rice noodle, clear soup", "£6.20"},
            {"h1. Wok noodle"}, 
            {"Pad Thai", "rice noodle, prawn, dried shrimp, tofu, egg, peanut, beansprout and lime", "£7.40"},
            {"Sen chan pad Thai", "rice noodle, prawn, peanut, egg, green mango and crabmeat (s)", "£9.10"},
            {"Pad Thai jay", "rice noodle, tenderstem broccoli, tofu, courgette, french bean, peanut, beansprout and lime (v)", "£7.40"},
            {"Pad gai", "sen yai noodle, chicken, oak leaf and egg", "£8.60"},
            {"Dolly pad mee", "dolly noodle, prawn, fish cake, choi sum and chilli (s)", "£7.90"},
            {"Pad kwetio", "sen yai noodle, smoked chicken, prawn and holy basil (s)", "£8.30"},
            {"Smoked chicken", "vermicelli noodle, Chinese broccoli and egg", "£6.90"},
            {"h1. Stir-fry"},
            {"Chicken butternut squash", "cashew nut and dried chilli (s)", "£6.80"},
            {"Fried chicken", "lemongrass, chilli sauce, shallot and daikon", "£7.70"},
            {"Ginger beef", "Thai pepper, chilli and spring onion asparagus, (s)", "£8.60"},
            {"Black pepper beef", "roasted onion and chilli", "£9.10"},
            {"Cod fillet stir-fry", "Thai garlic, krachai, lime leaf and chilli (s)", "£9.90"},
            {"Pat king talay", "prawn, squid, scallop, Thai pepper, woodear, chilli and ginger", "£8.50"},
            {"Chilli prawn", "sweet basil and chilli (s)", "£8.50"},
            {"Coconut prawn", "green peppercorn, coconut tip and lime leaf", "£8.50"},
            {"Tofu and spinach", "yellow bean, shallot and chilli (v)", "£7.40"},
            {"h1. Grilled"},
            {"Char-grilled chicken", "spicy cucumber salad and sticky rice (s)", "£9.90"},
            {"Char-grilled rib-eye beef", "tamarind sauce", "£12.40"},
            {"Char-grilled duck", "Chinese broccoli and tamarind sauce", "£12.40"},
            {"h1. Rice"},
            {"Green curry fried rice", "char-grilled chicken (s)", "£8.90"},
            {"Asparagus fried rice", "sun blush tomato, apple and egg (v)", "£6.70"},
            {"Crabmeat egg fried rice", "spring onion", "£6.90"},
            {"Chilli prawn fried rice", "shiitake mushroom (s)", "£8.50"},
            {"h1. Curry"},
            {"Green chicken curry", "pea aubergine and corn (s)", "£8.20"},
            {"Green vegetable curry", "Thai aubergine, corn and coconut heart (s)", "£8.30"},
            {"Geng gari gai", "chicken, new potato and cucumber relish (s)", "£8.90"},
            {"Aromatic butternut pumpkin curry", "cucumber relish (v)", "£8.60"},
            {"Jungle curry", "grilled chicken, Thai aubergine, bamboo shoot and french bean (s)", "£7.90"},
            {"Prawn curry", "pineapple, lime leaf and chilli (s)", "£9.90"},
            {"Red lamb curry", "lychee, lime leaf and chilli (s)", "£9.90"},
            {"Mussaman duck curry", "with potato, peanut and onion", "£9.50"},
            {"Southern fish curry", "Tilapia with Thai lime leaf and chilli (s)", "£7.00"},
            {"h1. Sides"},
            {"Por-pia jay", "vegetable spring rolls (v)", "£3.90"},
            {"Goong tohd prawn", "breadcrumbs with chilli lime sauce", "£5.80"},
            {"Thai calamari", "ginger and peppercorn", "£6.50"},
            {"Chicken wings", "pandan leaf", "£4.60"},
            {"Pandan chicken", "garlic and coriander root wrapped in pandan leaf", "£5.50"},
            {"Chicken satay", "peanut sauce", "£4.95"},
            {"Morning glory", "yellow bean,Thai garlic and red chilli (s)", "£5.80"},
            {"Chinese broccoli", "garlic and shiitake (v)", "£6.60"},
            {"Phad phak", "French bean, broccoli, courgette, baby corn, cashew and pine nut(v)", ""},
            {"Coconut rice (v)", "", "£3.10"},
            {"Jasmine rice (v)", "", "£2.20"},
            {"Brown rice (v)", "", "£2.20"},
            {"Sticky rice (v)", "", "£3.10"},
            {"h1. Drinks"}
            };
        
        static final int HEADING = 0;
        static final int NAME = 1;
        static final int BODY = 2;
        
        static final int PRICE_CHILD = 1;       //The index of the price child
        static final int ACTION_CHILD = 2;      //The index of the actions child
        static final int DYNAMIC_CHILDREN = 1;  //How many children not in the array?
    
        private Activity activity;

        MenuListAdapter(Activity act) {
            activity = act;
        }
    
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
    	return items[groupPosition][childPosition+1];
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (groupPosition * 100) + childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	View retn = null;
    	if (childPosition == PRICE_CHILD)
    	{
    		retn = getTextView(NAME);
    		((TextView) retn).setText(getChild(groupPosition, childPosition).toString());

    	}
    	else if (childPosition != ACTION_CHILD)
    	{
    		retn = getTextView(BODY);
    		((TextView) retn).setText(getChild(groupPosition, childPosition).toString());
    	}
    	else //It's the buttons
    	{
    		Button button1 = getButton(activity.getResources().getString(R.string.order_button));
    		Button button2 = getButton(activity.getResources().getString(R.string.food_detail_button));
    	    LinearLayout horizontalLayout = new LinearLayout(activity); 
    	    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);     	 
    	    horizontalLayout.addView(button1);
    	    horizontalLayout.addView(button2);
    	    retn = horizontalLayout;
    	}
    	return retn;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
    	//Not the first member
    	int count = items[groupPosition].length-1;
    	if (count > 0)
    	{
    		//It's not a category
    		count += DYNAMIC_CHILDREN;
    	}
    	return count;
    }
   
    @Override
    public Object getGroup(int groupPosition) {
        return items[groupPosition][0];
    }

    @Override
    public int getGroupCount() {
        return items.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	//Create the text view for the headings and menu items
    	String text = getGroup(groupPosition).toString();
    	String headingText = parseForHeadingText(text);
    	View retn;
    	if (headingText != null)
    	{
    		//Not sure why the inflate approach didn't work
    		//retn = View.inflate(activity, R.id.refHeadingText, parent);
    		retn = getTextView(HEADING);
    		text = headingText;
    	}
    	else
    	{
    		//retn = View.inflate(activity, R.id.refNameText, parent);
    		retn = getTextView(NAME);
    	}
    	((TextView) retn).setText(text);
        return retn;
    }

    private String parseForHeadingText(String text) {
    	String strs[] = text.split("h1. ");
    	if (text.startsWith("h1. "))
    	{
    		return strs[1];
    	}
		return null;
	}
    
    private TextView getTextView(int style)
    {
        //AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
        //        320, AbsListView.LayoutParams.WRAP_CONTENT);
    	TextView textView = new TextView(activity);
    	//textView.setLayoutParams(lp);
    	// Center the text vertically
    	textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    	int col_id;
    	switch (style) {
    	case HEADING: 
    		col_id = R.color.highlight_grey;
    		break;
    	case NAME:
    		col_id = R.color.bright_white;
    		break;
    	default:
    		col_id = R.color.foreground_beige;
    	}
    	
    	textView.setBackgroundColor(activity.getResources().getColor(R.color.base_brown));
    	textView.setTextAppearance(activity, android.R.attr.textAppearanceMedium);
    	textView.setTextColor(activity.getResources().getColor(col_id));
    	textView.setTypeface(null,Typeface.BOLD);
    	// Set the text starting position
    	int left = 60;
    	int top = 20;
    	int right = 20;
    	int bottom = 20;
    	textView.setPadding(left,top,right,bottom);
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


	@Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    
    

}
