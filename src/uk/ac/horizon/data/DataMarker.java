package uk.ac.horizon.data;

public class DataMarker {
	
	public static final int YOU_TUBE = 1;
	public static final int MAIL = 4;
	public static final int WEBSITE = 5;
	
	private String mCode;
	private String mTitle;
	private String mUri;
	private int mServiceId;
	
	public DataMarker(String code, String title, String uri, int serviceId){
		setCode(code);
		setTitle(title);
		setUri(uri);
		setServiceId(serviceId);
	}
	
	public void setCode(String code){
		this.mCode = code;
	}
	
	public String getCode(){
		return mCode;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getUri() {
		return mUri;
	}

	public void setUri(String mUri) {
		this.mUri = mUri;
	}
	
	public int getServiceId() {
		return mServiceId;
	}

	public void setServiceId(int id) {
		this.mServiceId = id;
	}
}
