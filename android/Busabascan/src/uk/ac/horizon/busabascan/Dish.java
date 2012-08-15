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

	static final String dishData[][] = {
		{"Char-grilled duck", "4.5", "3", 
			"Chinese broccoli and tamarind sauce","£12.40",
			"The classic Busaba dishes are consistently interesting, and well executed at all branches: smoky duck breast with tart tamarind sauce"},
        {"Salmon and green mango salad", "4", "7", 
			"lime leaf, peanut, chilli and nam pla dee sauce (s)","£7.90",
			"A suprisingly delicious combination based on a Thai classic salad."},
        {"Thai calamari","4.5", "12", "ginger and peppercorn", "£6.50",
			"I love love love the Thai Calamari I get from Busaba Eathai. They are delicious. The thai calarami are kinda cooked according to the traditional Salt & Pepper Squid but this version has got a crunchier and sweeter coating."},
        {"Chicken wings","3.5", "4", "pandan leaf", "£4.60",
        	"OMG I didn't want to leave anything on my plate it was that nice; The chicken wings were great, unlike many other restaurants where they usually douse them in tons of sauce. "},
        {"Pandan chicken","5", "23", "garlic and coriander root wrapped in pandan leaf", "£5.50",
        	"A traditional street food from Bangkok, pandan chicken can be eaten as a snack or part of a meal with rice as an accompaniment. Try the recipe from Busaba Eathai for Taste of London"}

	};
	static final int image_ids[] = {
		R.drawable.char_grilled_duck,
		R.drawable.salmon_mango_salad,
		R.drawable.thai_calamari,
		R.drawable.chicken_wings,
		R.drawable.pandanchicken
	};
	
	String name;
	float  stars;
	int    num_raters;
	String description;
	String price;
	String review;
	int    image_id;
	
	Dish(String n, float st, int nr, String desc, String rev, int iid)
	{
		name=n; stars=st; num_raters=nr; description=desc; 
		review = rev; image_id = iid;
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
						 dishData[i][DESC_IDX], dishData[i][REVIEW_IDX],
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
	public Integer getRaters()
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
	    return review;
	}
	public int getImageId()
	{
		return image_id;
	}
	
}
