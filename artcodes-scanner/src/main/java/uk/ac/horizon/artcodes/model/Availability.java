/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.model;

public class Availability
{
	private Double lat;
	private Double lon;
	private String name;
	private Long start;
	private Long end;
	private String address;

	public Availability()
	{

	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public Long getEnd()
	{
		return end;
	}

	public void setEnd(Long end)
	{
		this.end = end;
	}

	public Double getLat()
	{
		return lat;
	}

	public void setLat(double lat)
	{
		this.lat = lat;
	}

	public Double getLon()
	{
		return lon;
	}

	public void setLon(double lon)
	{
		this.lon = lon;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Long getStart()
	{
		return start;
	}

	public void setStart(Long start)
	{
		this.start = start;
	}

	public void setLat(Double lat)
	{
		this.lat = lat;
	}

	public void setLon(Double lon)
	{
		this.lon = lon;
	}
}
