/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.common.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code NumberUtil} help parsing numbers.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public final class NumberUtil {

	private static final String ERROR = "parsing error";
	private static final Logger LOG = LoggerFactory.getLogger(NumberUtil.class);

	private NumberUtil() {
	}

	/**
	 * Convert a string to long.
	 * 
	 * @param nb a string
	 * @return {@code Long} or {@code null}
	 */
	public static Long parseLong(String nb) {
		try {
			return Long.parseLong(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return null;
		}
	}

	/**
	 * Convert a string to long.
	 * 
	 * @param nb           a string
	 * @param defaultValue default value
	 * @return {@code long} or defaultValue
	 */
	public static long parseLong(String nb, long defaultValue) {
		try {
			return Long.parseLong(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return defaultValue;
		}
	}

	/**
	 * Convert a string to short.
	 * 
	 * @param nb a string
	 * @return {@code Short} or {@code null}
	 */
	public static Short parseShort(String nb) {
		try {
			return Short.parseShort(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return null;
		}
	}

	/**
	 * Convert a string to short.
	 * 
	 * @param nb           a string
	 * @param defaultValue default value
	 * @return {@code short} or defaultValue
	 */
	public static short parseShort(String nb, short defaultValue) {
		try {
			return Short.parseShort(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return defaultValue;
		}
	}

	/**
	 * Convert a string to float.
	 * 
	 * @param nb a string
	 * @return {@code Float} or {@code null}
	 */
	public static Float parseFloat(String nb) {
		try {
			return Float.parseFloat(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return null;
		}
	}

	/**
	 * Convert a string to float.
	 * 
	 * @param nb           a string
	 * @param defaultValue default value
	 * @return {@code float} or defaultValue
	 */
	public static float parseFloat(String nb, float defaultValue) {
		try {
			return Float.parseFloat(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return defaultValue;
		}
	}

	/**
	 * Convert a string to integer.
	 * 
	 * @param nb a string
	 * @return {@code Integer} or {@code null}
	 */
	public static Integer parseInt(String nb) {
		try {
			return Integer.parseInt(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return null;
		}
	}

	/**
	 * Convert a string to integer.
	 * 
	 * @param nb           a string
	 * @param defaultValue default value
	 * @return {@code int} or defaultValue
	 */
	public static int parseInt(String nb, int defaultValue) {
		try {
			return Integer.parseInt(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return defaultValue;
		}
	}

	/**
	 * Convert a string to double removing all the char that are not number before
	 * parsing.
	 * 
	 * @param nb a string
	 * @return {@code Double} or {@code null}
	 */
	public static Double extractDouble(String nb) {
		try {
			return Double.parseDouble(nb.replaceAll("[^0-9^\\.]", ""));
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return null;
		}
	}

	/**
	 * Convert a string to double.
	 * 
	 * @param nb a string
	 * @return {@code Double} or {@code null}
	 */
	public static Double parseDouble(String nb) {
		try {
			return Double.parseDouble(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return null;
		}
	}

	/**
	 * Convert a string to double.
	 * 
	 * @param nb           a string
	 * @param defaultValue default value
	 * @return {@code double} or defaultValue
	 */
	public static double parseDouble(String nb, double defaultValue) {
		try {
			return Double.parseDouble(nb);
		} catch (Exception ex) {
			LOG.trace(ERROR, ex);
			return defaultValue;
		}
	}

}
