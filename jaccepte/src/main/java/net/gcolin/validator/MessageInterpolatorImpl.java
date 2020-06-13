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

package net.gcolin.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.validation.MessageInterpolator;

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.lang.CompositeException;
import net.gcolin.common.lang.LocaleSupplier;
import net.gcolin.common.reflect.Reflect;

/**
 * A MessageInterpolator implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MessageInterpolatorImpl implements MessageInterpolator {

	private static final String BUNDLE_NAME = "validation/ValidationMessages";
	private Map<Locale, ResourceBundle> resources = new HashMap<>();
	private LocaleSupplier[] localeProviders;

	public MessageInterpolatorImpl() {
		localeProviders = Collections2.safeFillServiceLoaderAsArray(MessageInterpolatorImpl.class.getClassLoader(),
				LocaleSupplier.class);
	}

	@Override
	public String interpolate(String messageTemplate, Context context) {
		for (int i = 0; i < localeProviders.length; i++) {
			Locale loc = localeProviders[i].get();
			if (loc != null) {
				return interpolate(messageTemplate, context, loc);
			}
		}
		return interpolate(messageTemplate, context, Locale.getDefault());
	}

	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) {

		String message = messageTemplate;

		StringBuilder newMsg = new StringBuilder();

		String prec = null;
		while (message.indexOf('{') != -1 && !message.equals(prec)) {
			prec = message;
			message = interpolate0(message, newMsg, context, locale);
			newMsg.setLength(0);
		}

		return message;
	}

	private String interpolate0(String message, StringBuilder newMsg, Context context, Locale locale) {
		int prec = 0;
		boolean in = false;
		for (int i = 0, l = message.length(); i < l; i++) {
			char ch = message.charAt(i);
			if (ch == '}' && in) {
				newMsg.append(replace(message.substring(prec, i), context, locale));
				prec = i + 1;
				in = false;
			} else if (ch == '{' && !in) {
				in = true;
				if (i != prec) {
					newMsg.append(message.substring(prec, i));
				}
				prec = i + 1;
			}
		}
		if (in) {
			throw new IllegalArgumentException("the expression is not valid. missing a }");
		}
		if (prec < message.length()) {
			newMsg.append(message.substring(prec));
		}
		return newMsg.toString();
	}

	private String replace(String messageTemplate, Context context, Locale locale) {
		ResourceBundle rb = resources.get(locale);
		if (rb == null) {
			rb = ResourceBundle.getBundle(BUNDLE_NAME, locale);
			resources.put(locale, rb);
		}
		String msg = messageTemplate;
		try {
			msg = rb.getString(messageTemplate);
		} catch (MissingResourceException ex) {
			Method method = Reflect.findMethod(context.getConstraintDescriptor().getAnnotation().annotationType(),
					messageTemplate, 0);
			if (method != null) {
				try {
					Object resp = method.invoke(context.getConstraintDescriptor().getAnnotation());
					return resp == null ? "null" : resp.toString();
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					if (ValidatorImpl.LOG.isDebugEnabled()) {
						ValidatorImpl.LOG.debug("message not found: " + messageTemplate,
								new CompositeException(Arrays.asList(ex, e1)));
					}
				}
			} else {
				if (ValidatorImpl.LOG.isDebugEnabled()) {
					ValidatorImpl.LOG.debug("message not found: " + messageTemplate, ex);
				}
				return "{" + msg + "}";
			}
		}

		return msg;
	}

}
