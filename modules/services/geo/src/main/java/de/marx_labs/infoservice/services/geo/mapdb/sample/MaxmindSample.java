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
package de.marx_labs.infoservice.services.geo.mapdb.sample;

import de.marx_labs.infoservice.services.geo.IPLocationDB;
import de.marx_labs.infoservice.services.geo.Location;
import de.marx_labs.infoservice.services.geo.mapdb.MaxmindIpLocationMapDB;




public class MaxmindSample {
	
public static void main(String[] args) throws Exception {
		
		IPLocationDB readerhsql = new MaxmindIpLocationMapDB();
		readerhsql.open("/www/import/data/ipdb");
		
		testLocation(readerhsql, "66.211.160.129");
		System.out.println("--------------------");
		testLocation(readerhsql, "213.83.37.145");
		System.out.println("--------------------");
		testLocation(readerhsql, "88.153.215.174");
		System.out.println("--------------------");
		testLocation(readerhsql, "85.22.92.225");
		System.out.println("--------------------");
		
		readerhsql.close();
	}

	private static void testLocation(IPLocationDB readerhsql, String ip) {
		long before = System.currentTimeMillis();
		Location loc = readerhsql.searchIp(ip);
		System.out.println("1: " + loc.getCountry() + " - " + loc.getRegionName() + " - " + loc.getCity());
		long after = System.currentTimeMillis();
		System.out.println((after - before) + "ms");
	}
}
