/**
 * Mad-Advertisement Copyright (C) 2011-2013 Thorsten Marx <thmarx@gmx.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.marx_labs.infoservice.web.utils.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;


import de.marx_labs.ads.base.configuration.Configuration;
import de.marx_labs.infoservice.services.geo.IPLocationDB;
import de.marx_labs.infoservice.services.geo.mapdb.MaxmindIpLocationMapDB;
import de.marx_labs.infoservice.web.utils.Constants;

import de.marx_labs.infoservice.web.utils.caches.CachedUserAgentStringParser;
import de.marx_labs.infoservice.web.utils.caches.UpdatableCachedIpLocationDB;

import java.io.File;
import java.io.FileFilter;
import net.sf.uadetector.UserAgentStringParser;

public class DevelomentConfigModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(DevelomentConfigModule.class);

	private Configuration context;

	public DevelomentConfigModule(Configuration baseContext) {
		this.context = baseContext;
	}

	@Override
	protected void configure() {
	}

	@Provides
	@Singleton
	private UserAgentStringParser initUserAgentParser () {
		return new CachedUserAgentStringParser();
	}
	
	@Provides
	@Singleton
	private IPLocationDB initIpLocation() {
        IPLocationDB iploc = null;
//		iploc.open(context.getString(Constants.CONFIG.PROPERTIES.IPDB_DIR));
        
        File[] ipdbs = new File(context.getString(Constants.CONFIG.PROPERTIES.DATA_DIR)).listFiles(new FileFilter () {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && pathname.getName().startsWith(Constants.IPDB)) {
                    return true;
                }
                return false;
            }
            
        });
        long latestmodified = 0l;
        File latestDB = null;
        for (File db : ipdbs) {
            String name = db.getName();
            String modified = name.substring(name.indexOf("_")+1);
            long modLong = Long.parseLong(modified);
            if (modLong > latestmodified) {
                latestmodified = modLong;
                latestDB = db;
            }
        }
        
        if (latestDB != null) {
            logger.info("using ipdb" + latestDB.getAbsolutePath());
            iploc = new MaxmindIpLocationMapDB();
            
            String db = latestDB.getAbsolutePath() + "/db";
            iploc.open(db);
        }
        
		return new UpdatableCachedIpLocationDB(iploc);
	}

}
