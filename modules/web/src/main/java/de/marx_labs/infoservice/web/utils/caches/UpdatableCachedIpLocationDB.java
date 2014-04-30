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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public final class UpdatableCachedIpLocationDB implements IPLocationDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatableCachedIpLocationDB.class);

    private IPLocationDB ipdb;
    private final Cache<String, Location> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public UpdatableCachedIpLocationDB(IPLocationDB ipdb) {
        this.ipdb = ipdb;
    }

    public void setDB(IPLocationDB db) {
        LOGGER.debug("updateing iplocation database");
        if (this.ipdb == null) {
            ipdb = db;
        } else {
            synchronized (UpdatableCachedIpLocationDB.this.ipdb) {
                try {
                    IPLocationDB temp = ipdb;
                    ipdb = db;
                    temp.close();
                } catch (SQLException ex) {
                    LOGGER.error("error updating ipdb", ex);
                }
            }
        }
    }

    @Override
    public void open(String db) {
        // do not open
    }

    @Override
    public void close() throws SQLException {
        if (ipdb != null) {
            ipdb.close();
        }
    }

    @Override
    public void importCountry(String path) {
        // do not import
    }

    @Override
    public Location searchIp(String ip) {
        if (ipdb == null) {
            return Location.UNKNOWN;
        }
        Location result = cache.getIfPresent(ip);
        if (result == null) {
            result = ipdb.searchIp(ip);
            cache.put(ip, result);
        }
        return result;
    }

}
