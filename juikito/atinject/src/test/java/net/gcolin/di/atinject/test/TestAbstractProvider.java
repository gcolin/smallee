package net.gcolin.di.atinject.test;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.SingletonProvider;
import net.gcolin.di.atinject.SupplierProvider;

public class TestAbstractProvider {

	private Object destroyInstance = null;
	
	@Test
	public void testBuilder() {
		Environment env = new Environment();
		SingletonProvider<String> p = new SingletonProvider<String>(String.class, String.class, String.class, String.class, env);
		p.setBuilder(new SupplierProvider<String>(String.class, () -> "hello", null, env) {
			@Override
			public void destroyInstance(Object o) {
				destroyInstance = o;
			}
		});
		Assert.assertNull(p.getNoCreate());
		Assert.assertEquals("hello", p.get());
		Assert.assertEquals("hello", p.getNoCreate());
		p.stop();
		Assert.assertEquals("hello", destroyInstance);
		
		p.setBuilder(new SupplierProvider<String>(String.class, () -> "hello", null, env) {
			@Override
			public void destroyInstance(Object o) {
				throw new UnsupportedOperationException();
			}
		});
		
		Assert.assertEquals("hello", p.get());
		try {
			p.stop();
			Assert.fail();
		} catch (Exception e) {
			// handle exception
		}
	}
	
}
