package net.gcolin.di.atinject.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import net.gcolin.common.io.Io;
import net.gcolin.common.lang.NumberUtil;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.InjectionPoint;
import net.gcolin.di.atinject.InjectionPointBuilder;

public class ConfigInjectionPointBuilder implements InjectionPointBuilder {

	private Map<String, String> properties = new HashMap<>();

	public ConfigInjectionPointBuilder(Environment env) {
		InputStream in = env.getClassLoader().getResourceAsStream("config.properties");
		if (in != null) {
			read(in, env);
		}

		String configPath = System.getProperty("di.config");
		if (configPath != null) {
			File file = new File(configPath);
			if (!file.exists()) {
				env.getLog().log(Level.WARNING, "Cannot find configuration file: {0}", configPath);
			} else {
				try {
					read(new FileInputStream(file), env);
				} catch (FileNotFoundException e) {
					env.getLog().log(Level.SEVERE, "cannot load file " + configPath, e);
				}
			}
		}
	}

	private void read(InputStream in, Environment env) {
		try {
			Properties props = new Properties();
			props.load(in);
			for (final String name : props.stringPropertyNames()) {
				properties.put(name, props.getProperty(name));
			}
		} catch (IOException e) {
			env.getLog().log(Level.SEVERE, "cannot load configuration file", e);
		} finally {
			Io.close(in);
		}
	}

	@Override
	public InjectionPoint create(Field field, Environment env) {
		if (field.isAnnotationPresent(Config.class)) {
			Config config = field.getAnnotation(Config.class);
			String v = properties.get(config.name());
			if (v == null) {
				v = config.defaultValue();
			}
			if (field.getType() == String.class) {
				return new ConfigInjectionPoint(field, v);
			} else if (field.getType() == int.class || field.getType() == Integer.class) {
				return new ConfigInjectionPoint(field, NumberUtil.parseInt(v, 0));
			} else if (field.getType() == float.class || field.getType() == Float.class) {
				return new ConfigInjectionPoint(field, NumberUtil.parseFloat(v, 0));
			} else if (field.getType() == double.class || field.getType() == Double.class) {
				return new ConfigInjectionPoint(field, NumberUtil.parseDouble(v, 0));
			} else if (field.getType() == short.class || field.getType() == Short.class) {
				return new ConfigInjectionPoint(field, NumberUtil.parseShort(v, (short) 0));
			} else if (field.getType() == long.class || field.getType() == Long.class) {
				return new ConfigInjectionPoint(field, NumberUtil.parseLong(v, 0));
			} else {
				env.getLog().log(Level.WARNING, "cannot cast config properties to type {0} in {0}", new Object[] {field.getType(), field});
			}
		}
		return null;
	}

	@Override
	public InjectionPoint create(Method method, Environment env) {
		return null;
	}

}
