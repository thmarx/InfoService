/**
 * Mad-Advertisement
 * Copyright (C) 2011-2013 Thorsten Marx <thmarx@gmx.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.marx_labs.infoservice.services.geo;

import java.io.Serializable;

public class Location implements Serializable {

	public static final Location UNKNOWN = new Location("", "", "");

	private String regionName = "";
	private String country = "";
	private String city = "";
	private String postalcode = "";

	private String latitude = "";
	private String longitude = "";

	public Location () {
		
	}
	
	public Location(String country, String regionName, String city) {
		this.country = country;
		this.regionName = regionName;
		this.city = city;
	}

	public Location(String country, String regionName, String city, String latitude, String lonitude) {
		this(country, regionName, city);
		this.latitude = latitude;
		this.longitude = lonitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getCountry() {
		return country;
	}

	public String getRegionName() {
		return regionName;
	}

	public String getCity() {
		return city;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Location other = (Location) obj;

		return this.city.equals(other.city) && this.country.equals(other.country)
				&& this.regionName.equals(other.regionName) && this.getPostalcode().equals(other.getPostalcode()) && this.latitude.equals(other.latitude)
				&& this.longitude.equals(other.longitude);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + (this.city != null ? this.city.hashCode() : 0);
		hash = 19 * hash + (this.country != null ? this.country.hashCode() : 0);
		hash = 19 * hash + (this.regionName != null ? this.regionName.hashCode() : 0);
		hash = 19 * hash + (this.getPostalcode() != null ? this.getPostalcode().hashCode() : 0);
		hash = 19 * hash + (this.latitude != null ? this.latitude.hashCode() : 0);
		hash = 19 * hash + (this.longitude != null ? this.longitude.hashCode() : 0);
		return hash;
	}

	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the postalcode
	 */
	public String getPostalcode() {
		return postalcode;
	}

	/**
	 * @param postalcode the postalcode to set
	 */
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
