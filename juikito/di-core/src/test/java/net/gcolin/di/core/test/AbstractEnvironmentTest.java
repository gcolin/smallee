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

package net.gcolin.di.core.test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import net.gcolin.di.core.AbstractEnvironment;
import net.gcolin.di.core.InjectService;
import net.gcolin.di.core.Key;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

/**
 * An test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AbstractEnvironmentTest {

  @Retention(RUNTIME) @Target({TYPE})
  public @interface XmlType {}
  
  @Retention(RUNTIME)
  @Target({TYPE})
  public @interface XmlRootElement {}
  
  @XmlType
  @XmlRootElement
  @Ignore
  public static class A {}

  @XmlRootElement
  @Ignore
  public static class B {}

  @XmlType
  @Ignore
  public static class C {}

  private static class Env extends AbstractEnvironment<Object> {

    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType) {
      return annotationType.toString().contains("Xml");
    }

    @Override
    public boolean isInstanciable(Class<?> clazz) {
      return super.isInstanciable(clazz);
    }

    @Override
    public List<Annotation> findQualifiersList(Annotation[] annotations) {
      List<Annotation> list = super.findQualifiersList(annotations);
      return list.isEmpty() ? Collections.emptyList() : list;
    }
  }

  @Test
  public void testQualifier() {
    Env env = new Env();

    Assert.assertTrue(env.isInstanciable(String.class));
    Assert.assertFalse(env.isInstanciable(AbstractEnvironment.class));
    Key key = new Key();
    key.setQualifiers(new Annotation[0]);
    
    Assert.assertEquals(key, env.createKey(null, null, null));

    env.bind(String.class, "hello");
    env.bind(String.class, "world", env.findQualifiers(A.class.getAnnotations()));
    Assert.assertEquals(2, env.getBinding().size());
    Assert.assertEquals(0,
        env.findQualifiers(AbstractEnvironmentTest.class.getAnnotations()).length);

    Assert.assertEquals("hello",
        env.resolveBinding(env.createKey(String.class, String.class, null)));
    Assert.assertEquals("world", env.resolveBinding(env.createKey(String.class, String.class,
        env.findQualifiers(A.class.getAnnotations()))));
    Assert.assertNull(env.resolveBinding(env.createKey(String.class, String.class,
        env.findQualifiers(B.class.getAnnotations()))));
    Assert.assertNull(env.resolveBinding(env.createKey(String.class, String.class,
        env.findQualifiers(C.class.getAnnotations()))));

    InjectService service = env;

    A singleton = new A();
    service.bind(singleton);
    Assert.assertFalse(service.isMutable(A.class));
    Assert.assertTrue(singleton == env.findSupplier(A.class).get());
    Assert.assertNull(env.findSupplier(B.class));
    service.add(B.class, C.class);
    Assert.assertNotNull(env.findSupplier(B.class));
    service.remove(B.class, C.class);
    Assert.assertNull(env.findSupplier(B.class));
    Assert.assertNull(env.find("hello"));
    service.unbind(singleton);
    Assert.assertNull(env.findSupplier(A.class));
  }
}
