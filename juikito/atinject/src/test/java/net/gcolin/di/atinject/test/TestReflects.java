package net.gcolin.di.atinject.test;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Reflects;

public class TestReflects {
	
	  class NoIntf {}
	  
	  interface Itf {}
	  
	  class WithItf implements Itf {}
	  
	  @Test
	  public void testGetInterface() {
		  Assert.assertEquals(NoIntf.class, Reflects.getInterface(NoIntf.class));
		  Assert.assertEquals(Itf.class, Reflects.getInterface(WithItf.class));
	  }
	  
	  public static class WithStatic {
		  
		  @Inject
		  static void hello() {};
		  
	  }
	  
	  @Test
	  public void testfindStaticMethods() {
		  Method[][] m = Reflects.findStaticMethods(WithStatic.class);
		  System.out.println(m);
		  Assert.assertEquals("hello", Reflects.findStaticMethods(WithStatic.class)[0][0].getName());
	  }
	
}
