/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
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

package uk.ac.horizon.artcodes.model;

import java.util.Date;

public class Availability {
	private Date start;
	private Date end;
	private Location location;

	public Availability() {

	}

	public String getAddress() {
		if(location == null) {
			return null;
		}
		return location.address;
	}

	public Long getEnd() {
		if(end == null) {
			return null;
		}
		return end.getTime();
	}

	public void setEnd(Long end) {
		if(end != null) {
			this.end = new Date(end);
		} else {
			this.end = null;
		}
		//notifyPropertyChanged(BR.end);
		if (start != null && end != null && end < start.getTime()) {
			setStart(end);
		}
	}

	public Double getLat() {
		if(location == null) {
			return null;
		}
		return location.coordinates[1];
	}

	public void setLocation(double lat, double lon, String name, String address) {
		location = new Location("point", name, address, new Double[]{lon, lat});
	}

	public Double getLon() {
		if(location == null) {
			return null;
		}
		return location.coordinates[0];
	}

	public String getName() {
		if(location == null) {
			return null;
		}
		return location.name;
	}

	public Long getStart() {
		if(start == null) {
			return null;
		}
		return start.getTime();
	}

	public void setStart(Long start) {
		if(start != null) {
			this.start = new Date(start);
		} else {
			this.start = null;
		}
		//notifyPropertyChanged(BR.start);
		if (start != null && end != null && end.getTime() < start) {
			setEnd(start);
		}
	}

	public void clearLocation() {
		location = null;
	}
}
