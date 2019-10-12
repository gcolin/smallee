package net.gcolin.di.atinject.test;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

public class TestSelfInject {

	public static class A {
		@Inject
		private Environment env;
	}
	
	public static class B extends A {
		
	}
	
	@Test
	public void test() {
		Environment env = new Environment();
		env.add(A.class);
		
		A a = env.get(A.class);
		Assert.assertEquals(env, a.env);
	}
}
