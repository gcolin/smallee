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

package net.gcolin.rest.ext.validation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ParameterNameProvider;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.server.AbstractResource;

/**
 * An ugly filter.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidationContainerRequestFilter
		implements ContainerRequestFilter, Function<AbstractResource, AbstractResource> {

	private Validator validator;

	public ValidationContainerRequestFilter() {
		try {
			validator = Validation.buildDefaultValidatorFactory().usingContext().parameterNameProvider(
					new ParameterNameProviderImpl(Validation.buildDefaultValidatorFactory().getParameterNameProvider()))
					.getValidator();
		} catch (ValidationException ex) {
			Logger.getLogger("net.gcolin.rest.ext.validation").log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	public Validator getValidator() {
		return validator;
	}

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		// not so clean filter use AbstractResource apply(AbstractResource t)
	}

	private static class ParameterNameProviderImpl implements ParameterNameProvider {

		private ParameterNameProvider delegate;
		private ParameterNameProviderItem[] items = {
				new ParameterNameProviderItem(an -> Reflect.getAnnotation(an, QueryParam.class),
						x -> ((QueryParam) x).value()),
				new ParameterNameProviderItem(an -> Reflect.getAnnotation(an, FormParam.class),
						x -> ((FormParam) x).value()),
				new ParameterNameProviderItem(an -> Reflect.getAnnotation(an, PathParam.class),
						x -> ((PathParam) x).value()) };

		public ParameterNameProviderImpl(ParameterNameProvider delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public List<String> getParameterNames(Constructor<?> paramConstructor) {
			return delegate.getParameterNames(paramConstructor);
		}

		@Override
		public List<String> getParameterNames(Method paramMethod) {
			Annotation[][] aa = paramMethod.getParameterAnnotations();
			List<String> defaultNames = delegate.getParameterNames(paramMethod);
			for (int i = 0, l = aa.length; i < l; i++) {
				Annotation[] an = aa[i];
				for (int j = 0; j < items.length; j++) {
					ParameterNameProviderItem item = items[j];
					Annotation annotation = item.getAnnotation.apply(an);
					if (annotation != null) {
						defaultNames.set(i, item.getName.apply(annotation));
						break;
					}
				}
			}
			return defaultNames;
		}

	}

	private static class ParameterNameProviderItem {

		private Function<Annotation[], Annotation> getAnnotation;
		private Function<Annotation, String> getName;

		public ParameterNameProviderItem(Function<Annotation[], Annotation> getAnnotation,
				Function<Annotation, String> getName) {
			super();
			this.getAnnotation = getAnnotation;
			this.getName = getName;
		}
	}

	@Override
	public AbstractResource apply(AbstractResource res) {
		BeanDescriptor bd = validator.getConstraintsForClass(res.getResourceClass());
		MethodDescriptor md = bd.getConstraintsForMethod(res.getResourceMethod().getName(),
				res.getResourceMethod().getParameterTypes());

		if (md != null && hasContraints(md)) {

			Supplier<Object> supplier = res.getInstance();

			res.setParamValidator(o -> {
				Set<ConstraintViolation<Object>> violations = validator.forExecutables()
						.validateParameters(supplier.get(), res.getResourceMethod(), o);
				if (!violations.isEmpty()) {
					throw new ConstraintViolationException(violations);
				}
			});
		}
		return res;
	}

	private boolean hasContraints(MethodDescriptor md) {
		for (ParameterDescriptor d : md.getParameterDescriptors()) {
			if (d.hasConstraints()) {
				return true;
			}
		}
		return false;
	}
}
