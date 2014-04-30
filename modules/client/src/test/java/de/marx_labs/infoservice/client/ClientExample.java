/*
 * Copyright 2014 thmarx.
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

package de.marx_labs.infoservice.client;

import de.marx_labs.infoservice.client.InfoServiceClient;
import de.marx_labs.infoservice.client.model.Info;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thmarx
 */
public class ClientExample {
	public static void main (String...args) {
		InfoServiceClient client = InfoServiceClient.builder().url("http://localhost:9090/").build();
		
		try {
			Info ipInfo = client.location("84.119.154.20");
			
			long before = System.currentTimeMillis();
			ipInfo = client.location("84.119.104.20");
			long after = System.currentTimeMillis();
			System.out.println("location took " + (after-before) + "ms");
			
			assert ipInfo != null;
			assert ipInfo.getLocation() != null;
			assert ipInfo.getUserAgent() == null;
			
			
			before = System.currentTimeMillis();
			ipInfo = client.userAgent("Mozilla/4.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
			after = System.currentTimeMillis();
			System.out.println("userAgent took " + (after-before) + "ms");
			
			assert ipInfo != null;
			assert ipInfo.getLocation() == null;
			assert ipInfo.getUserAgent() != null;
			
			
			before = System.currentTimeMillis();
			ipInfo = client.all("84.119.104.20","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
			after = System.currentTimeMillis();
			System.out.println("all took " + (after-before) + "ms");
			
			assert ipInfo != null;
			assert ipInfo.getLocation() != null;
			assert ipInfo.getUserAgent() != null;
			
			System.out.println("Land = " + ipInfo.getLocation().getCountry());
			System.out.println("Region = " + ipInfo.getLocation().getRegion());
			
			System.out.println("Device = " + ipInfo.getUserAgent().getType());
			
		} catch (ExecutionException ex) {
			Logger.getLogger(ClientExample.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(ClientExample.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ClientExample.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			client.close();
		}
		
	}
}
