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
package de.marx_labs.infoservice.services.geo.mapdb;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import de.marx_labs.infoservice.services.geo.IPLocationDB;
import de.marx_labs.infoservice.services.geo.Location;
import de.marx_labs.infoservice.services.geo.helper.ValidateIP;
import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Reader zum einlesen
 * 
 * "ip_start";"country_code";"country_name";"region_code";"region_name";"city";
 * "zipcode";"latitude";"longitude";"metrocode"
 * 
 * @author tmarx
 * 
 */
public class MaxmindIpLocationMapDB implements IPLocationDB {
	
    private static Logger LOGGER = LoggerFactory.getLogger(MaxmindIpLocationMapDB.class);
    
	ConcurrentNavigableMap<Long, Location> treeMap = null;
	Map<String, Location> locations;
	DB db = null;
	
	public MaxmindIpLocationMapDB() {
	}
	
	/* (non-Javadoc)
	 * @see de.marx.services.geo.IPLocationDB#open()
	 */
	@Override
	public void open(String db){
		this.db = DBMaker.newFileDB(new File(db)).transactionDisable().closeOnJvmShutdown().make();
		this.treeMap = this.db.getTreeMap("iplocatinos");
		this.locations = this.db.getHashMap("locations");
	}

	/* (non-Javadoc)
	 * @see de.marx.services.geo.IPLocationDB#close()
	 */
	@Override
	public void close() throws SQLException {
//		poolMgr.dispose();
	}

	/* (non-Javadoc)
	 * @see de.marx.services.geo.IPLocationDB#importCountry(java.lang.String)
	 */
	@Override
	public void importCountry(String path) {
		try {
			if (!path.endsWith("/")) {
				path += "/";
			}
			
			// import locations
			System.out.println("import locations");
			getLocations(path);
			
			BufferedReader br = new BufferedReader(new FileReader(path + "GeoLiteCity-Blocks.csv"));
			CSVReader reader = new CSVReader(br, ',', '\"', 2);
			
			int count = 0;
			String [] values;
			System.out.println("create location searchtree");
			while ((values = reader.readNext()) != null) {
				String ipfrom = values[0];
				String ipto = values[1];
				String locid = values[2];

				Location location = locations.get(locid);
				
				long ipFrom =  Long.valueOf(ipfrom);
				
				this.treeMap.put(ipFrom, location);
				
				count++;
				
				if (count % 10000 == 0) {
					this.db.commit();
					LOGGER.info(count + " Locations in searchtree");
				}
			}
			this.db.compact();
			LOGGER.info(count + " entries imported");
			
		} catch (IOException e) {
			LOGGER.error("", e);
		} catch (NumberFormatException e) {
            LOGGER.error("", e);
        } finally {
			this.db.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.marx.services.geo.IPLocationDB#searchIp(java.lang.String)
	 */
	@Override
	public Location searchIp(String ip) {
		try {
			
	        
	        long inetAton = ValidateIP.ip2long(ip);
            
			Map.Entry<Long, Location> entry = this.treeMap.lowerEntry(inetAton);

			if (entry != null) {
				return entry.getValue();
			}
			
	        
	        return Location.UNKNOWN;
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			
		}
		
		return null;
	}
	
	private void getLocations (String path) throws IOException {
		if (!path.endsWith("/")) {
			path += "/";
		}
		String filename = path + "GeoLiteCity-Location.csv";
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		CSVReader reader = new CSVReader(br, ',', '\"', 2);
		String [] values;
		int count = 0;
		while ((values = reader.readNext()) != null) {
			
			
			Location location = new Location();
			location.setCountry(values[1]);
			location.setRegionName(values[2]);
			location.setCity(values[3]);
			location.setPostalcode(values[4]);
			location.setLatitude(values[5]);
			location.setLongitude(values[6]);
			
			this.locations.put(values[0], location);
			
			count++;
			if (count % 10000 == 0) {
				this.db.commit();
				System.out.println(count + " locations importiert");
			}
		}
		
		this.db.compact();
	}
	
	private String mtrim (String text) {
		if (text.startsWith("\"")) {
			text = text.substring(1);
		}
		if (text.endsWith("\"")) {
			text = text.substring(0, text.length()-1);
		}
		return text;
	}
}
