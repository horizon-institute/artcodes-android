package uk.ac.horizon.dtouch;

import java.net.MalformedURLException;
import java.net.URL;


public class DtouchMarkerWebServicesURL {
	private static final String BASE_URL = "http://data.horizon.ac.uk/v1";
	private static final String IMAGE_POSTFIX = "image";
	private static final String THUMBNAIL_POSTFIX = "thumb";
	private static final String URL1_POSTFIX = "url1";
	private static final String URL2_POSTFIX = "url2";
	private static final String URL3_POSTFIX = "url3";
	
	public static URL getMarkerPrimaryURL(String codeKey){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey));
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getUserMarkerURL(String codeKey, String userId){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey) + "/person/" + userId);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getMarkerImageURL(String codeKey){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey) + "/" + IMAGE_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getMarkerThumbnailURL(String codeKey){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey) + "/" + THUMBNAIL_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getMarkerURL1(String codeKey){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey) + "/" + URL1_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getMarkerURL2(String codeKey){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey) + "/" + URL2_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getMarkerURL3(String codeKey){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/" + encodeString(codeKey) + "/" + URL3_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getUserURL(String userID){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/person/" + userID);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getDishThumbnailURL(String dishName){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/dish/" +  encodeString(dishName) + "/" + THUMBNAIL_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getDishImageURL(String dishName){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/dish/" +  encodeString(dishName) + "/" + IMAGE_POSTFIX);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getDishURL(String dishName){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/dish/" + encodeString(dishName));
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	public static URL getUserDishURL(String dishName, String userId){
		URL url = null;
		try{
			url = new URL(BASE_URL + "/dish/" + encodeString(dishName) + "/person/" + userId);
		}catch(MalformedURLException e){
			
		}
		return url;
	}
	
	private static String encodeString(String stringToEncode){
		return stringToEncode.replaceAll(" ", "%20");
	}


	
}
