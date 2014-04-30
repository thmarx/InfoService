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
package de.marx_labs.infoservice.web.utils.listener;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.marx_labs.ads.base.configuration.Configuration;
import de.marx_labs.ads.base.configuration.Environment;
import de.marx_labs.ads.base.configuration.properties.Properties2;
import de.marx_labs.infoservice.services.geo.IPLocationDB;
import de.marx_labs.infoservice.services.geo.mapdb.MaxmindIpLocationMapDB;
import de.marx_labs.infoservice.web.utils.Constants;
import de.marx_labs.infoservice.web.utils.RuntimeContext;
import de.marx_labs.infoservice.web.utils.caches.UpdatableCachedIpLocationDB;
import de.marx_labs.infoservice.web.utils.config.DevelomentConfigModule;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author thorsten
 */
public class StartupPlugIn implements ServletContextListener {

	private static final Logger logger = LoggerFactory
			.getLogger(StartupPlugIn.class);

    private MyWatchQueueReader ipLocationUpdater = null;
    
    Timer timer = new Timer();
    
	public void contextInitialized(ServletContextEvent event) {
		try {
			// Konfiguration einlesen
			String environment = event.getServletContext().getInitParameter(
					"environment");

			RuntimeContext.environment(Environment.forName(environment));

			String configDirectory = new File(".").getAbsolutePath(); // event.getServletContext().getInitParameter("configDirectory");

			if (System.getProperties().containsKey("mad.home")) {
				configDirectory = System.getProperty("mad.home");
			}

			if (!configDirectory.endsWith("/")) {
				configDirectory += "/";
			}

			System.setProperty("mad.home", configDirectory);

			configDirectory += "config/";

			// configure log4j
//			PropertyConfigurator.configure(Properties2.loadProperties(configDirectory + "log4j.properties"));
			PropertyConfigurator.configure(Properties2.loadProperties(StartupPlugIn.class.getClassLoader().getResource("/configuration/log4j.properties")));

			RuntimeContext.properties(Properties2.loadProperties(StartupPlugIn.class.getClassLoader().getResource("/configuration/config_" + RuntimeContext.environment().getName() + ".properties")));
			
			
			Configuration context = Configuration.newInstance(StartupPlugIn.class.getClassLoader().getResource("/configuration/config_" + RuntimeContext.environment().getName() + ".properties"));
			RuntimeContext.context(context);

			Injector injector = null;
			if (Environment.DEVELOPMENT.equals(RuntimeContext.environment())) {
				injector = Guice.createInjector(new DevelomentConfigModule(context));
			} else if (Environment.PRODUCTION.equals(RuntimeContext.environment())) {
				injector = Guice.createInjector(new DevelomentConfigModule(context));
				
			}
			RuntimeContext.injector(injector);

			updateDB();
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		try {
			// close connection
//			RuntimeContext.injector().getInstance(TrackingService.class).close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	
	private void updateDB () throws IOException {
        // first get the iplocationdb
        RuntimeContext.injector().getInstance(IPLocationDB.class);
        
		String watchDir = RuntimeContext.context().getString(Constants.CONFIG.PROPERTIES.IMPORT_DIR);
        logger.info("watching for changes: " + watchDir);
		Path toWatch = Paths.get(watchDir);
        if(toWatch == null) {
            throw new UnsupportedOperationException("Directory not found");
        }
  
        // make a new watch service that we can register interest in 
        // directories and files with.
        WatchService myWatcher = toWatch.getFileSystem().newWatchService();
        toWatch.register(myWatcher, StandardWatchEventKinds.ENTRY_CREATE);
        ipLocationUpdater = new MyWatchQueueReader(myWatcher);
        timer.schedule(ipLocationUpdater, 1000, 10000);
	}
	private static class MyWatchQueueReader extends TimerTask {
  
        /** the watchService that is passed in from above */
        private WatchService myWatcher;
        public MyWatchQueueReader(WatchService myWatcher) {
            this.myWatcher = myWatcher;
        }
  
        /**
         * In order to implement a file watcher, we loop forever 
         * ensuring requesting to take the next item from the file 
         * watchers queue.
         */
        @Override
        public void run() {
            try {
                // get the first event before looping
                WatchKey key = myWatcher.poll();
                while(key != null) {
                    // we have a polled event, now we traverse it and 
                    // receive all the states from it
                    for (WatchEvent event : key.pollEvents()) {
                        System.out.printf("Received %s event for file: %s\n",
                                          event.kind(), event.context() );
						if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
							Path path = (Path) event.context();
                            
                            String absoluteDB = path.toFile().getName();
                            String importDir = RuntimeContext.context().getString(Constants.CONFIG.PROPERTIES.IMPORT_DIR);
                            if (!importDir.endsWith("/")) {
                                importDir += "/";
                            }
                            importDir += absoluteDB;
                            logger.info("found iplocation data to import " + absoluteDB);
                            logger.info("import from " + importDir);
                            
                            String newDBDir = RuntimeContext.context().getString(Constants.CONFIG.PROPERTIES.DATA_DIR);
                            if (!newDBDir.endsWith("/")) {
                                newDBDir += "/";
                            }
                            newDBDir += Constants.IPDB + "_" + System.currentTimeMillis();
                            new File(newDBDir).mkdirs();
                            newDBDir += "/db";
                            logger.info("import to " + newDBDir);
                            
                            IPLocationDB iploc = new MaxmindIpLocationMapDB();
                            iploc.open(newDBDir);
                            iploc.importCountry(importDir);
                            iploc.open(newDBDir);
                            IPLocationDB iplocDB = RuntimeContext.injector().getInstance(IPLocationDB.class);
                            ((UpdatableCachedIpLocationDB)iplocDB).setDB(iploc);
                            logger.info("import done");
						}
                    }
                    key.reset();
                    key = myWatcher.poll();
                }
            } catch (Exception e) {
                logger.error("error watching filechanges", e);
            }
            System.out.println("Stopping thread");
        }
    }
}
