package net.gcolin.di.atinject.test;

import javax.inject.Named;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Binding;
import net.gcolin.di.atinject.Environment;

public class TestBindingString {

	@Test
	public void test() {
		Environment env = new Environment();
		Binding b = new Binding(String.class, env);
		Assert.assertEquals("Binding [clazz=class java.lang.String, qualifiers=[@" + Named.class.getName()
				+ "(value=java.lang.String)]]", b.named("java.lang.String").toString());

		b = new Binding(String.class, env);
		Assert.assertEquals("Binding [clazz=class java.lang.String, qualifiers=[@net.gcolin.di.atinject.Inject()]]",
				b.named("net.gcolin.di.atinject.Inject").toString());
	}

}
