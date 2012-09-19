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
		{"Som tam", "3.5", "6", 
			"A light spicy papaya salad","£6.90",
			"Sweet, sour and spicy with lime juice, nuts and shrimp.  Recommended to accompany Pandan Chicken or Thai calamari."},		
		{"Som yam", "3.5", "3", 
			"Green mango and grapefruit salad","£6.70",
			"A sweet and salty salad with balanced flavours and a great aromatic crunch from toasted coconut.  Goes well with Noodles, rice and curries."},		
		{"Grilled aubergine", "4", "8", 
			"A Refreshing aubergine salad","£6.60",
			"Smoky BBQ flavours combine with a sourness and the unique texture of the white fungus in this vegan dish."},		
		{"Tom kha chicken", "3.5", "12", 
			"Coconut noodle soup","£6.70",
			"Galangal, lemongrass and grilled chicken combine with shitake mushroom to give a soup with flavour and texture.  Goes well with Por-pia jay and Goong tohd prawn."},		
		{"Tom yam talay", "5.0", "6", 
			"Spicy seafood soup","£6.70",
			"Plump prawns, clams and baby squid rest on vermicelli noodles in this mouthwatering variation of the classic Tom Tam Goong."},		
		{"Tom yam goong", "4.5", "8", 
			"Spicy prawn soup","£6.70",
			"This classic spicy prawn soup with coriander root is a favourite in the Kitchen."},		
		{"Pad Thai", "4.0", "6", 
			"Thai Noodle","£7.40",
			"This classic prawn and tofu noodle dish is a traditional favourite and not to be missed."},		
		{"Sen chan pad Thai", "5.0", "6", 
			"Crab and Green Mango Thai Noodle","£9.10",
			"A spicy version of Pad Thai with the addition of Crab meat and Green Mango.  Great with Calamari or Pandan Chicken."},		
		{"Pad Thai jay", "3.5", "4", 
			"Rice noodle with stir fried vegetables","£7.40",
			"A vegetarian Pad Thai with soy sauce replacing fish sauce.  Try with Por-pai jay and Som tam."},		
		{"Chicken butternut squash", "4.0", "18", 
			"Stir fried chicken, butternut squash, cashew nuts","£6.80",
			"One of our most popular dishes with a spicy sweet balance.  It's been on the menu since Busaba opened.  Try with Jasmin or Coconut rice."},		
		{"Fried chicken", "4.5", "11", 
			"Deep fried chicken","£7.70",
			"Mouthwatering Thai Street food at its best.  Eat with rice and any stir fry or grill dish."},		
		{"Ginger beef", "5.0", "11", 
			"Marinated beef","£8.60",
			"Woodear mushroom and beef set off by beautifully with fresh ginger."},		
		{"Char-grilled chicken", "4.0", "7", 
			"Char-grilled chicken leg","£9.90",
			"Served with spicy chok chai dressing, cucumber relish and sticky rice this dish is a meal in itself.  It's gilled in a Thai oven at a high temperature to lock in the flavours of the meat."},		
		{"Char-grilled rib-eye beef", "4.5", "8", 
			"Beef marinated in Yaki Niku sauce","£9.90",
			"Succulent beef with a sweet spicy tamarind sauce.  Traditionally recommended accompaniment is Som tam salad."},		
		{"Char-grilled duck", "5.0", "3", 
			"Chinese broccoli and tamarind sauce","£12.40",
			"The classic Busaba dishes are consistently interesting, and well executed at all branches: smoky duck breast with tart tamarind sauce"},
        {"Salmon and green mango salad", "4", "7", 
			"lime leaf, peanut, chilli and nam pla dee sauce (s)","£7.90",
			"A surprisingly delicious combination based on a Thai classic salad."},
        {"Green curry fried rice", "4.5", "7", 
			"Fried rice with chicken thigh","£8.90",
			"Great with Thai calamari or Por-pia jay.  This dish can be a meal in itself."},
        {"Asparagus fried rice", "3.5", "6", 
			"Fried rice with asparagus","£6.70",
			"This simple vegetarian dish can be eaten as a meal or makes a wonderful accompaniment to curries and stir fry dishes."},
        {"Crabmeat egg fried rice", "4.0", "9", 
			"Egg fried rice with crabmeat","£6.90",
			"This dish has a natural sweetness from the premium crab meat which compliments Goong tohd prawn wonderfully."},
        {"Green chicken curry", "4.0", "12", 
			"Classic Thai Green Curry","£8.20",
			"Creamy coconut milk based green curry with grilled chicken, sweet basil, pea aubergine and sweet corn."},
        {"Green vegetable curry", "3.5", "12", 
			"Vegetarian version of the classic Thai Green Curry","£8.30",
			"Creamy coconut milk based green curry with French bean, pea aubergine and bamboo shoot."},
        {"Geng gari gai", "4.5", "11", 
			"Spicy chicken curry with potato","£8.90",
			"Served with cucumber relish on the side."},
        {"Thai calamari","4.5", "12", "ginger and peppercorn", "£6.50",
			"I love love love the Thai Calamari I get from Busaba Eathai. They are delicious. The thai calarami are kinda cooked according to the traditional Salt & Pepper Squid but this version has got a crunchier and sweeter coating."},
        {"Chicken wings","3.5", "4", "pandan leaf", "£4.60",
        	"OMG I didn't want to leave anything on my plate it was that nice; The chicken wings were great, unlike many other restaurants where they usually douse them in tons of sauce. "},
        {"Pandan chicken","5", "23", "garlic and coriander root wrapped in pandan leaf", "£5.50",
        	"A traditional street food from Bangkok, pandan chicken can be eaten as a snack or part of a meal with rice as an accompaniment. Try the recipe from Busaba Eathai for Taste of London"}

	};
	static final int image_ids[] = {
		R.drawable.som_tam,
		R.drawable.som_yam,
		R.drawable.grilled_aubergine,
		R.drawable.tom_kha,
		R.drawable.tom_yam_talay,
		R.drawable.tom_yam_goong,
		R.drawable.pad_thai,
		R.drawable.sen_chan_pad_thai,
		R.drawable.pad_thai_jay,
		R.drawable.chicken_butternut,
		R.drawable.fried_chicken,
		R.drawable.ginger_beef,
		R.drawable.char_grilled_chicken,
		R.drawable.char_rib_eye,
		R.drawable.char_grilled_duck,
		R.drawable.salmon_mango_salad,
		R.drawable.green_fried_rice,
		R.drawable.asparagus_rice,
		R.drawable.crab_rice,
		R.drawable.green_chicken_curry,
		R.drawable.green_veg_curry,
		R.drawable.geng_gari_gai,
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
