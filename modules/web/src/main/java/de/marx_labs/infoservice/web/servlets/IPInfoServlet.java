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
package de.marx_labs.infoservice.web.servlets;

import com.google.common.base.Strings;
import de.marx_labs.infoservice.web.utils.RuntimeContext;
import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marx_labs.infoservice.services.geo.IPLocationDB;
import de.marx_labs.infoservice.services.geo.Location;
import net.minidev.json.JSONObject;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;

/**
 *
 * @author tmarx
 */
@WebServlet(asyncSupported = true)
public class IPInfoServlet extends HttpServlet {

	public static final int CALLBACK_TIMEOUT = 60000;
	public static final int MAX_SIMULATED_TASK_LENGTH_MS = 5000;

	private IPLocationDB ipdb = null;
	private UserAgentStringParser uagentParser = null;

	/**
	 * create the executor
	 */
	public void init() throws ServletException {
		this.ipdb = RuntimeContext.injector().getInstance(IPLocationDB.class);
		this.uagentParser = RuntimeContext.injector().getInstance(UserAgentStringParser.class);
	}

	/**
	 * destroy the executor
	 */
	public void destroy() {
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// create the async context, otherwise getAsyncContext() will be null
		final AsyncContext ctx = request.startAsync();

		// set the timeout
		ctx.setTimeout(CALLBACK_TIMEOUT);

		// attach listener to respond to lifecycle events of this AsyncContext
		ctx.addListener(new AsyncListener() {
			/**
			 * complete() has already been called on the async context, nothing to do
			 */
			public void onComplete(AsyncEvent event) throws IOException {
			}

			/**
			 * timeout has occured in async task... handle it
			 */
			public void onTimeout(AsyncEvent event) throws IOException {
				log("onTimeout called");
				log(event.toString());
				ctx.getResponse().getWriter().write("TIMEOUT");
				ctx.complete();
			}

			/**
			 * THIS NEVER GETS CALLED - error has occured in async task... handle it
			 */
			public void onError(AsyncEvent event) throws IOException {
				log("onError called");
				log(event.toString());
				ctx.getResponse().getWriter().write("ERROR");
				ctx.complete();
			}

			/**
			 * async context has started, nothing to do
			 */
			public void onStartAsync(AsyncEvent event) throws IOException {
			}
		});

		execute(ctx);

	}

	private void execute(final AsyncContext ctx) {

		// exec.execute(new Runnable() {
		ctx.start(new Runnable() {
			public void run() {
				HttpServletResponse response = (HttpServletResponse) ctx.getResponse();
				HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
				try {

					String ip = (String) request.getParameter("ip");
					String useragent = (String) request.getParameter("ua");
					
					JSONObject result = new JSONObject();
					
                    if (!Strings.isNullOrEmpty(ip)) {
							Location location = ipdb.searchIp(ip);
							if (location != null) {
								JSONObject locationJson = new JSONObject();
								locationJson.put("city", location.getCity());
								locationJson.put("country", location.getCountry());
								locationJson.put("region", location.getRegionName());
								locationJson.put("latitude", location.getLatitude());
								locationJson.put("longitude", location.getLongitude());
								
								result.put("location", locationJson);
							}
						} 
						if (!Strings.isNullOrEmpty(useragent)) {
							ReadableUserAgent ua = uagentParser.parse(useragent);
							JSONObject useragentJson = new JSONObject();
							
							useragentJson.put("deviceCategory", ua.getDeviceCategory().getCategory().getName());
							useragentJson.put("family", ua.getFamily().getName());
							useragentJson.put("name", ua.getName());
							useragentJson.put("os", ua.getOperatingSystem().getName());
							useragentJson.put("type", ua.getTypeName());
							
							result.put("useragent", useragentJson);
						}
					
					// write
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("application/json");
					response.setContentLength(result.toJSONString().length());
					response.getOutputStream().print(result.toJSONString());
				} catch (IOException e) {
					log("Problem processing task", e);
				} finally {
					ctx.complete();
				}

			}
		});
	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "IPInfoServlet";
	}

}
