package net.gcolin.di.atinject.config;

import java.lang.reflect.Field;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.InjectionPoint;
import net.gcolin.di.core.InjectException;

public class ConfigInjectionPoint implements InjectionPoint {

	private Field field;
	private Object value;

	public ConfigInjectionPoint(Field field, Object value) {
		this.field = field;
		this.value = value;
		Reflect.enable(field);
	}

	@Override
	public void inject(Object o) {
		try {
			field.set(o, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new InjectException(ex);
		}
	}

}
