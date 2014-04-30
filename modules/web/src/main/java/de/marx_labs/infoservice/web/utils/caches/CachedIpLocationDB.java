/*
 * Copyright 2014 marx.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.marx_labs.infoservice.web.utils.caches;

import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.marx_labs.infoservice.services.geo.IPLocationDB;
import de.marx_labs.infoservice.services.geo.Location;

import java.sql.SQLException;

/**
 *
 * @author marx
 */
public final class CachedIpLocationDB implements IPLocationDB {

	private final IPLocationDB ipdb;
	private final Cache<String, Location> cache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build();

	public CachedIpLocationDB (IPLocationDB ipdb) {
		this.ipdb = ipdb;
	}
	
	@Override
	public void open(String db) {
		ipdb.open(db);
	}

	@Override
	public void close() throws SQLException {
		ipdb.close();
	}

	@Override
	public void importCountry(String path) {
		ipdb.importCountry(path);
	}

	@Override
	public Location searchIp(String ip) {
		Location result = cache.getIfPresent(ip);
		if (result == null) {
			result = ipdb.searchIp(ip);
			cache.put(ip, result);
		}
		return result;
	}

	

}
