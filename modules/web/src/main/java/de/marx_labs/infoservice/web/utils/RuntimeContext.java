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
package de.marx_labs.infoservice.web.utils;


import de.marx_labs.utilities.configuration.BaseRuntimeContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class RuntimeContext extends BaseRuntimeContext {
	private static HashMap<String, HashMap<String, Object>> configuration = new HashMap<String, HashMap<String,Object>>();
	
	public static int getIntProperty (String key, int defaultValue) {
		if (properties.containsKey(key)) {
			return Integer.valueOf(properties.getProperty(key));
		}

		return defaultValue;
	}
	
	public static boolean getBooleanProperty (String key, boolean defaultValue) {
		if (properties.containsKey(key)) {
			return Boolean.parseBoolean(properties.getProperty(key));
		}

		return defaultValue;
	}
	
	public static void setConfiguration(String config, String key, Object value) {
		if (!configuration.containsKey(config)) {
			configuration.put(config, new HashMap<String, Object>());
		}
		configuration.get(config).put(key, value);
	}

	public static Object getConfiguration (String config, String key) {
		if (configuration.containsKey(config)) {
			return configuration.get(config).get(key);
		}
		return null;
	}

	
}
