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
package net.gcolin.di.atinject.test;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestGeneric2 {
	
   public static class Dao<T> {}
	
	public static class StringDao extends Dao<String> {}
	
	public static class LongDao extends Dao<Long> {}
	
	public static class A {
		
		@Inject
		Dao<String> stringDao;
		
		@Inject
		Dao<Long> longDao;
		
	}
	
	@Test
	public void test() {
		Environment env = new Environment();
		env.add(StringDao.class, LongDao.class, A.class);
		env.start();
		
		A a = env.get(A.class);
		Assert.assertEquals(LongDao.class, a.longDao.getClass());
		Assert.assertEquals(StringDao.class, a.stringDao.getClass());
	}

}
