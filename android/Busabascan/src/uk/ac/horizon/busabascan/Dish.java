package uk.ac.horizon.busabascan;

// This class knows all about a dish by name.

public class Dish {
	
	//return a dish based on the string.  We can only return dishes for 
	//types that we know about.
	
	static final int NAME_IDX = 0;
	static final int STAR_IDX = 1;
	static final int RATERS_IDX = 2;
	static final int DESC_IDX = 3;
	static final int PRICE_IDX = 4;
	static final int REVIEW_IDX = 5;
	static final int IMG_IDX = 6;

	static final String dishData[][] = {
		{"Char-grilled duck", "4.5", "3", 
			"Chinese broccoli and tamarind sauce","£12.40",
			"The combination of tamarind and rich juicy duck is spectacular.",
			"char-grilled-duck.png"},
        {"Salmon and green mango salad", "4", "7", 
			"lime leaf, peanut, chilli and nam pla dee sauce (s)","£7.90",
			"A suprising delicious combination.",
			"salmon_and_green_mango.png"},
        {"Thai calamari","4.5", "12", "ginger and peppercorn", "£6.50",
			"",""},
        {"Chicken wings","3.5", "4", "pandan leaf", "£4.60",
        	"",""},
        {"Pandan chicken","5", "23", "garlic and coriander root wrapped in pandan leaf", "£5.50",
        	"",""},

	};
	static final int image_ids[] = {
		R.drawable.char_grilled_duck
	};
	
	String name;
	float  stars;
	int    num_raters;
	String description;
	String price;
	String review;
	String image_name;
	int    image_id;
	
	Dish(String n, float st, int nr, String desc, String img, int iid)
	{
		name=n; stars=st; num_raters=nr; description=desc; image_name=img; 
		image_id = iid;
	}
	
	static Dish dishFactory(String dish)
	{
		Dish d = null;
		for (int i=0; i < dishData.length; i++)
		{
			if (dish.equals(dishData[i][NAME_IDX]))
			{
				//Construct the convenience object from the table
				d = new Dish(dish, 
						 Float.parseFloat(dishData[i][STAR_IDX]),
						 Integer.parseInt(dishData[i][RATERS_IDX]),
						 dishData[i][DESC_IDX],  dishData[i][IMG_IDX],
						 image_ids[i]);
				break;
			}
		}
		return d;
	}

	//Is the named dish one that we know about?
	static boolean knownDish(String dish){
		for (int i=0; i < dishData.length; i++)
		{
			if (dish.equals(dishData[i][NAME_IDX]))
			{
				return true;
			}
		}
		return false;
	}
	
	public float getStars()
	{
		return stars;
	}
	public int getRaters()
	{
	   return num_raters;
	}
	public String getDescription()
	{
	    return description;
	}
	public String getPrice()
	{
	    return price;
	}
	public String getReview()
	{
	    return price;
	}
	public String getImageName()
	{
	    return image_name;
	}
	public int getImageId()
	{
		return image_id;
	}
	
}
