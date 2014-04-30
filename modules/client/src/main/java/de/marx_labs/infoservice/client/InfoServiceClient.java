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

import com.alibaba.fastjson.JSON;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import de.marx_labs.infoservice.client.model.Info;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author thmarx
 */
public class InfoServiceClient {
	
	private String url;
	private AsyncHttpClient httpClient = null;
	
	private static final String ENCODING = "UTF-8";
	
	private static class Includes {
		public static final String LOCATION = "location";
		public static final String USERAGENT = "useragent";
		
		public static final String ALL = LOCATION + "," + USERAGENT;
	}
	
	public static class Builder {
		private String url;
		private Builder () {}
		
		public Builder url (String url) {
			this.url = url;
			return this;
		}
		
		public InfoServiceClient build () {
			return new InfoServiceClient(url);
		}
	}
	
	public static Builder builder () {
		return new Builder();
	}
	
	private InfoServiceClient (String url) {
		this.url = url;
		this.httpClient = new AsyncHttpClient();
		
		if (!this.url.endsWith("/")) {
			this.url += "/";
		}
	}
	
	public Info location (String ip) throws ExecutionException, InterruptedException, IOException {
		return ipInfoRequest(ip, null);
	}
	public Info userAgent (String userAgent) throws ExecutionException, InterruptedException, IOException {
		return ipInfoRequest(null, userAgent);
	}
	public Info all (String ip, String userAgent) throws ExecutionException, InterruptedException, IOException {
		return ipInfoRequest(ip, userAgent);
	}
	
	public void close () {
		this.httpClient.close();
	}
	
	private Info ipInfoRequest (String ip, String userAgent) throws ExecutionException, IOException, InterruptedException {
		StringBuilder uriBuilder = new StringBuilder();
		uriBuilder.append(this.url).append("ipinfo?");
		
		if (ip != null) {
			uriBuilder.append("&ip=").append(URLEncoder.encode(ip, ENCODING));
		}
		if (userAgent != null) {
			uriBuilder.append("&ua=").append(URLEncoder.encode(userAgent, ENCODING));
		}
		
		Future<Response> f = httpClient.prepareGet(uriBuilder.toString()).execute();
		Response r = f.get();
		
		return JSON.parseObject(r.getResponseBody(), Info.class);
	}
}
