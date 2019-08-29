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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.inject.Named;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.io.ByteArrayInputStream;
import net.gcolin.di.core.Key;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestKey {

  @Named("hello")
  String namedAnnotation;
  
  List<String> generic;
  
  @Test
  public void testClassQualifier() throws Exception {
    Key key = new Key();
    key.setType("Point");
    key.setQualifiers(new Annotation[]{TestKey.class.getDeclaredField("namedAnnotation").getAnnotation(Named.class)});
  
    check(key);
  }

  private void check(Key key) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream oo = new ObjectOutputStream(bout);
    oo.writeObject(key);
    oo.close();
    
    ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
    ObjectInputStream oi = new ObjectInputStream(bin);
    Key key2 = (Key) oi.readObject();
    oi.close();
    
    Assert.assertEquals(key, key2);
  }
  
  @Test
  public void testGenericType() throws Exception {
    Key key = new Key();
    key.setType(TestKey.class.getDeclaredField("generic").getGenericType().toString());
    check(key);
  }
  
}
