/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
