package net.gcolin.di.atinject.config;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

public class TestConfig {
	
	public static class A {
		
		@Config(name="hello")
		String hello;
		
		@Config(name="hello2")
		String hello2;
		
		@Config(name="hello3", defaultValue = "w")
		String hello3;
		
		@Config(name="anumber")
		int n;
		
		@Config(name="anumber")
		Integer n2;
		
		@Config(name="anumber")
		Long n3;
		
		@Config(name="anumber")
		long n4;
		
		@Config(name="anumber")
		Long n5;
		
		@Config(name="anumber")
		short n6;
		
		@Config(name="anumber")
		Short n7;
		
		@Config(name="anumber")
		double n8;
		
		@Config(name="afloat")
		Double n9;
		
		@Config(name="afloat")
		double n10;
		
		@Config(name="afloat")
		float n11;
		
		@Config(name="afloat")
		Float n12;
		
		@Config(name="afloat")
		List<Float> n13;
		
	}
	
	@Test
	public void test() {
		test0();
	}
	
	@Test
	public void testFile() {
		System.setProperty("di.config", "src/test/resources/config.properties");
		test0();
		System.setProperties(new Properties());
	}
	
	@Test
	public void testWrongFile() {
		System.setProperty("di.config", "src/test/resources/config3.properties");
		test0();
		System.setProperties(new Properties());
	}
	
	@Test
	public void testFileOverride() {
		System.setProperty("di.config", "src/test/resources/config2.properties");
		Environment env = new Environment();
		env.start();
		env.add(A.class);
		
		A a = env.get(A.class);
		Assert.assertEquals("worldoverride", a.hello);
		Assert.assertEquals("", a.hello2);
		Assert.assertEquals("w", a.hello3);
		System.setProperties(new Properties());
	}

	public void test0() {
		Environment env = new Environment();
		env.start();
		env.add(A.class);
		
		A a = env.get(A.class);
		Assert.assertEquals("world", a.hello);
		Assert.assertEquals("", a.hello2);
		Assert.assertEquals("w", a.hello3);
		Assert.assertEquals(10, a.n);
		Assert.assertEquals(Integer.valueOf(10), a.n2);
		Assert.assertEquals(Long.valueOf(10), a.n3);
		Assert.assertEquals(10, a.n4);
		Assert.assertEquals(Long.valueOf(10), a.n5);
		Assert.assertEquals(10, a.n6);
		Assert.assertEquals(Short.valueOf((short) 10), a.n7);
		Assert.assertEquals((double)10, a.n8, 0.01);
		Assert.assertEquals(1.2, a.n9, 0.01);
		Assert.assertEquals(1.2, a.n10, 0.01);
		Assert.assertEquals(1.2, a.n11, 0.01);
		Assert.assertEquals(1.2, a.n12, 0.01);
		Assert.assertNull(a.n13);
	}

}
